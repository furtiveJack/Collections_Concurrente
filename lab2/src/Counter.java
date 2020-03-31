import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Counter {
    private volatile int counter;
    private final static VarHandle COUNTER_HANDLE;
    static {
        var lookup = MethodHandles.lookup();
        try {
            COUNTER_HANDLE = lookup.findVarHandle(Counter.class, "counter", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public int nextValue() {
        while (true) {
            int current = counter;
            if (COUNTER_HANDLE.compareAndSet(this, current, current+1)) {
                return current;
            }
        }
    }
}
