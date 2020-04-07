import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Reducer {
    private static class ReduceTask extends RecursiveTask<Integer> {
        private final int[] array;
        private final int initial;
        private final int start;
        private final int end;
        private final IntBinaryOperator op;

        ReduceTask(int[] array, int initial, IntBinaryOperator op, int start, int end) {
            Objects.requireNonNull(array);
            Objects.requireNonNull(op);
            this.array = array;
            this.op = op;
            this.initial = initial;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start < 1024) {           // small enough
                return Arrays.stream(array, start, end).reduce(initial, op);
            }
            var middle = (start + end) / 2;
            ReduceTask t1 = new ReduceTask(array, initial, op, start, middle);
            t1.fork();
            ReduceTask t2 = new ReduceTask(array, initial, op, middle, end);
            var result2 = t2.compute();
            var result1 = t1.join();
            return op.applyAsInt(result1, result2);
        }
    }
    public static int sum(int[] array) {
//        var sum = 0;
//        for (var value : array) {
//            sum += value;
//        }
//        return sum;
        return parallelReduceWithStream(array, 0, Integer::sum);
    }

    public static int max(int[] array) {
//        var max = Integer.MIN_VALUE;
//        for (var value : array) {
//            max = Math.max(max, value);
//        }
//        return max;
        return parallelReduceWithStream(array, Integer.MIN_VALUE, Math::max);
    }

    public static int reduce(int[] array, int initial, IntBinaryOperator op) {
        var acc = initial;
        for( var value : array) {
            acc = op.applyAsInt(acc, value);
        }
        return acc;
    }

    public static int reduceWithStream(int[] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).reduce(initial, op);
    }

    public static int parallelReduceWithStream(int[] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).parallel().reduce(initial, op);
    }

    public static int parallelReduceWithForkJoin(int[] array, int initial, IntBinaryOperator op) {
        var task = new ReduceTask(array, initial, op, 0, array.length);
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(task);
    }

    public static <T> T sequentialReduce(Spliterator<T> spliterator, T initial, BinaryOperator<T> op) {
//        var acc = initial;
//        class Box {
//            private T acc = initial;
//        }
//        var box = new Box();
        var box = new Object() {
            private T acc = initial;
        };
        while (spliterator.tryAdvance(e -> {
            box.acc = op.apply(box.acc, e);
        }));
        return box.acc;
    }


    public static void main(String[] args) {
        int nb = 1_000_000;
        var rand = new Random(0);
        var array = rand.ints(nb, 0, 1000).toArray();
        System.out.println(sum(array));
        System.out.println(max(array));

        System.out.println(parallelReduceWithForkJoin(array, 0, Integer::sum));
        System.out.println(parallelReduceWithForkJoin(array, Integer.MIN_VALUE, Math::max));
    }
}
/*
Question 3:
    1/ Avec ThreadPoolExecutor, si on fait des appels bloquants on peut arrêter toutes les threads du pool et on a un
    deadlock entre la sousmission d'une nouvelle tache qui attend qu'une thread soit dispo et toutes les threads
    en attente que la tache que l'on doit soumettre soit finie
    lorsque l'on fait un join() dans une RecursiveTask, on enlève la tache qui appel le join() du ForkJoinPool
     et on la remet lorsque la tache qui fait le calcul sur lequel on attend a fini son calcul, comme cela pas de deadlock

    ForkjoinPool : fait meme chose qu'un ThreadpooExecutor sauf qu'il est capable de s'arreter et attendre la fin
    de l'exécution d'une autre tache (join)

    2/ On utilise la méthode ForkJoinPool.commonPool()

    3/ On utilise la méthode ForkJoinPool.invoke()

 */
