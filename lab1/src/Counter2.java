import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter2 {
    private final AtomicInteger counter = new AtomicInteger();

    public int nextInt() {
        return counter.getAndIncrement();
    }

    public static void main(String[] args) throws InterruptedException{
        var counter = new Counter2();
        var threads = new ArrayList<Thread>();
        for (var i = 0 ; i < 4 ; ++i) {
            var thread = new Thread( () -> {
                for (var j = 0 ; j < 100_000 ; ++j) {
                    counter.nextInt();
                }
            });
            thread.start();
            threads.add(thread);
        }

        for (var thread : threads) {
            thread.join();
        }

        System.out.println(counter.nextInt());
    }
}
