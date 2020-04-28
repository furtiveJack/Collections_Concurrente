package fr.uge.conc.exam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class SynchronizedCOWList<T> {
    private T[] array;
    private final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public SynchronizedCOWList() {
        array = (T[]) new Object[0];
    }

    public void add(T elem) {
        Objects.requireNonNull(elem);
        synchronized (lock) {
            var size = array.length;
            array = Arrays.copyOf(array, size + 1);
            array[size] = elem;
        }
    }

    public int size() {
        synchronized (lock) {
            return array.length;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        var list = new SynchronizedCOWList<Integer>();
        var nbThreads = 4;
        var random = new Random(0);
        var threads = new ArrayList<Thread>(nbThreads);
        for (var i = 0 ; i < nbThreads ; ++i) {
            threads.add(new Thread( () -> {
                for (var j = 0 ; j < 2_500 ; ++j) {
                    list.add(random.nextInt(1_000));
                }
            }));
        }
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("Size : " + list.size());
    }
}
