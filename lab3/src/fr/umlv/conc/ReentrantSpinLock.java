package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ReentrantSpinLock {
    private volatile int lock;
    private /*volatile*/ Thread ownerThread;
    private static final VarHandle HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVarHandle(ReentrantSpinLock.class, "lock", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public void lock() {
        var current = Thread.currentThread();                  // on recup le thread courant
        while (true) {
            if (HANDLE.compareAndSet(this, 0, 1)) {     // si lock == 0, on utilise un CAS pour le mettre à 1
                ownerThread = current;                         // on sauvegarde la thread qui possède le lock dans ownerThread.
                return;
            }
            if (ownerThread == current) {                      // sinon on regarde si la thread courante est ownerThread,
                lock++;                                        // si oui alors on incrémente lock.
                return;
            }
            Thread.onSpinWait();                               //  et il faut une boucle pour retenter le CAS après avoir appelé onSpinWait()
        }
    }

    public void unlock() {
        if (ownerThread != Thread.currentThread()) {        // si la thread courante est != ownerThread, on pète une exception
            throw new IllegalStateException();
        }
        var lock = this.lock; //volatile read
        if (lock == 1) {                                    // si lock == 1,
            ownerThread = null;                             // on remet ownerThread à null
            this.lock = 0; //volatile write
            return;
        }
        this.lock = lock - 1; //volatile write              // on décrémente lock
    }

    public static void main(String[] args) throws InterruptedException {
        var runnable = new Runnable() {
            private int counter;
            private final ReentrantSpinLock spinLock = new ReentrantSpinLock();

            @Override
            public void run() {
                for(var i = 0; i < 1_000_000; i++) {
                    spinLock.lock();
                    try {
                        spinLock.lock();
                        try {
                            counter++;
                        } finally {
                            spinLock.unlock();
                        }
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

/*
 Si on enlève le volatile sur ownerThread, il faut s'assurer d'écrire dans ownerThread avant d'écrire dans le lock
 et de lire ownerThread avant d'écrire le lock volatile

 Pas besoin de déclarer ownerThread volatile -> gain de perf
    -> quand on fait une écriture volatile du lock, on s'assure que tous les champs de l'objet ont été lus et mis a jour
    dans la ram
    C'est pourquoi il faut updater ownerThread avant de faire l'écrire volatile

    Si on lit un volatile, on a la garantit que les autres champs seront lus en ram ensuite

    L'écriture volatile empeche la réorganisation des instructions

 */