package fr.uge.conc.exam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class COWList<T> {
    private T[] array;

    @SuppressWarnings("unchecked")
    public COWList() {
        array = (T[]) new Object[0];
    }

    public void add(T elem) {
        Objects.requireNonNull(elem);
        var size = array.length;
        array = Arrays.copyOf(array, size + 1);
        array[size] = elem;
    }

    public int size() {
        return array.length;
    }

    public static void main(String[] args) throws InterruptedException {
        var list = new COWList<Integer>();
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
/**
 * 3. Le code n'est pas thread safe, car dans la méthode add, un thread peut etre déschédulé juste après avoir
 *    récupéré la taille du tableau. Un autre thread peut alors faire une copie du tableau et rajouter un élément.
 *    Quand le premier thread reprend la main, il va ignorer la modification faite par le second, et fera
 *    donc une copie tronquée du vrai tableau.
 *
 */
