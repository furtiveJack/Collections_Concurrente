
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LockFreeStringList {
    static final class Entry {
        final String element;
        volatile Entry next;

        Entry(String element) {
            this.element = element;
        }
    }
    private static final VarHandle NEXT_HANDLE, TAIL_HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            NEXT_HANDLE = lookup.findVarHandle(Entry.class, "next", Entry.class);
            TAIL_HANDLE = lookup.findVarHandle(LockFreeStringList.class, "tail", Entry.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private final Entry head;
    private volatile Entry tail;

    public LockFreeStringList() {
        tail = head = new Entry(null); // fake first entry
    }

    public void addLast(String element) {
        var entry = new Entry(element);
        var oldTail = tail;
        var last = oldTail;
        for (;;) {
            var next = last.next;
            if (next == null) {
                if (NEXT_HANDLE.compareAndSet(last, null, entry)) {
                    TAIL_HANDLE.compareAndSet(this, oldTail, entry);
                    tail = entry;
                    return;
                }
                next = tail;
            }
            last = next;
        }
    }

    public int size() {
        var count = 0;
        for (var e = head.next; e != null; e = e.next) {
            count++;
        }
        return count;
    }

    private static Runnable createRunnable(LockFreeStringList list, int id) {
        return () -> {
            for (var j = 0; j < 10_000; j++) {
                list.addLast(id + " " + j);
            }
        };
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        var threadCount = 5;
        var list = new LockFreeStringList();
        var tasks = IntStream.range(0, threadCount)
                .mapToObj(id -> createRunnable(list, id))
                .map(Executors::callable)
                .collect(Collectors.toList());
        var executor = Executors.newFixedThreadPool(threadCount);
        var futures = executor.invokeAll(tasks);
        executor.shutdown();
        for(var future : futures) {
            future.get();
        }
        System.out.println(list.size());
    }
}
