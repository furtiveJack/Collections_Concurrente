package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class SpinLock {
    private static final VarHandle HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVarHandle(SpinLock.class, "token", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private volatile boolean token;

    public void lock() {
        while (!HANDLE.compareAndSet(this, false, true)) {
            Thread.onSpinWait();
        }
    }

    public void unlock() {
        token = false; // volatile write
    }

    public boolean tryLock() {
        return HANDLE.compareAndSet(this, false, true);
    }

    public static void main(String[] args) throws InterruptedException {
        var runnable = new Runnable() {
            private int counter;
            private final SpinLock spinLock = new SpinLock();

            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    spinLock.lock();
                    try {
                        counter++;
                    } finally {
                        spinLock.unlock();
                    }
                }
            }
        };
        var t1 = new Thread(runnable);
        var t2 = new Thread(runnable);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("counter " + runnable.counter);
    }
}

/**
 * Exercice 1
 * <p>
 * 1) Réentrant signifie que quand l'on est dans une fonction, personne d'autre n'a le droit d'être également (pas un
 * autre thread ni le même thread)
 * <p>
 * 2) Si la classe est thread-safe, on s'attend à ce que le counter arrive à 2_000_000
 * Etant donné que les méthodes lock et unlock ne sont pas implémentées, la code du Runnable n'est pas threadsafe

 */