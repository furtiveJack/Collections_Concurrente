import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ForkJoinCollections {
    private static class ReduceTask<T, V> extends RecursiveTask<V> {

        private final Spliterator<T> spliterator;
        private final V initialValue;
        private final BiFunction<T,V,V> acc;
        private final BiFunction<V,V,V> combiner;
        private final int threshold;

        public ReduceTask(Spliterator<T> spliterator, V initialValue, BiFunction<T, V, V> acc,
                          BiFunction<V, V, V> combiner, int threshold) {
            Objects.requireNonNull(spliterator);
            Objects.requireNonNull(initialValue);
            this.spliterator = spliterator;
            this.initialValue = initialValue;
            this.acc = acc;
            this.threshold = threshold;
            this.combiner = combiner;
        }

        @Override
        protected V compute() {
            var size = spliterator.estimateSize();
            if (size == Long.MAX_VALUE) {
                return null;
            }
            if (size < threshold) {
                var box = new Object() {
                    private V acc = initialValue;
                };
                while (spliterator.tryAdvance(e -> box.acc = acc.apply(e, box.acc)));
                return box.acc;
            }
            var t1 = new ReduceTask<>(spliterator.trySplit(), initialValue, acc, combiner, threshold);
            t1.fork();
            var t2 = new ReduceTask<>(spliterator, initialValue, acc, combiner, threshold);
            var result2 = t2.compute();
            var result1 = t1.join();
            return combiner.apply(result1, result2);
        }
    }
    public static <V, T> V forkJoinReduce(Collection<T> collection, int threshold, V initialValue,
                                          BiFunction<T, V, V> accumulator, BiFunction<V, V, V> combiner) {

        return forkJoinReduce(collection.spliterator(), threshold, initialValue, accumulator, combiner);
    }

    private static <V, T> V forkJoinReduce(Spliterator<T> spliterator, int threshold, V initialValue,
                                           BiFunction<T, V, V> accumulator, BiFunction<V, V, V> combiner) {
        var task = new ReduceTask<>(spliterator, initialValue, accumulator, combiner, threshold);
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(task);
    }

    public static void main(String[] args) {
        // sequential
        System.out.println(IntStream.range(0, 10_000).sum());

        // fork/join
        var list = IntStream.range(0, 10_000).boxed().collect(Collectors.toList());
        var result = forkJoinReduce(list, 1_000, 0, (acc, value) -> acc + value,
                (acc1, acc2) -> acc1 + acc2);
        System.out.println(result);
    }
}

/*
 Spliterator:

 Methods :  estimateSize : essaye d'estimer la taille (peut ne pas réussir -> renvoit valeur incorrecte)
            tryAdvance: parcourt les éléments
            trySplit : essaye de découper en deux (peut renvoyer null)


*/