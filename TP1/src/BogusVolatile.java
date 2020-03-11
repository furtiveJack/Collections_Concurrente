
public class BogusVolatile {
    private volatile boolean stop;

    private void runCounter() {
        var localCounter = 0;
        while (!stop) {
            localCounter++;
        }
        System.out.println(localCounter);
    }

    private void stop() {
        stop = true;
    }

    public static void main(String[] args) throws InterruptedException {
        var bogus = new BogusVolatile();
        var thread = new Thread(bogus::runCounter);
        thread.start();
        Thread.sleep(100);
        bogus.stop();
        thread.join();
    }
}
