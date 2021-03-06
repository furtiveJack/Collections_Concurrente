package fr.uge.conc.exam;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class LockFreeTwoRandomNumberGenerator {
    private long x;
    private final static VarHandle HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVarHandle(LockFreeTwoRandomNumberGenerator.class, "x", long.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public LockFreeTwoRandomNumberGenerator(long seed) {
        if (seed == 0) {
            throw new IllegalArgumentException("seed == 0");
        }
        x = seed;
    }

    public long next() {  // Marsaglia's XorShift
        while (true) {
            var y = (long) HANDLE.getVolatile(this);
            var old = y;
            y ^= y >>> 12;
            y ^= y << 25;
            y ^= y >>> 27;
            var w = (long) HANDLE.getAndSet(this, y);
            if (w == old) {
                return y * 2685821657736338717L;
            }
        }
    }

    public static void main(String[] args) {
        var rng = new LockFreeTwoRandomNumberGenerator(1);
        for (var i = 0; i < 5_000 ; i++) {
            System.out.println(rng.next());
        }
    }
}
/*
    1. Si un thread se fait déschéduler au milieu d'un appel à next, alors un second thread peut faire appel à next,
    modifier la valeur de x et la renvoyer, puis le premier thread reprend la main et ne prend pas en compte les
    modifications effectuées par le second thread, et il les écrase.
 */