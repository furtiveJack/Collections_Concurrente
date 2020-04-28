package fr.uge.conc.exam;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleBiFunction;

public class SummaryIndex {
    private static class Task extends RecursiveTask<Double> {
        private final int from;
        private final int to;
        private final ToDoubleBiFunction<Integer, Integer> op;
        private final DoubleBinaryOperator compute;

        Task(int from, int to, ToDoubleBiFunction<Integer, Integer> op, DoubleBinaryOperator compute) {
            this.from = from;
            this.to = to;
            this.op = op;
            this.compute = compute;
        }

        @Override
        protected Double compute() {
            if (to - from <= 100) {
                return op.applyAsDouble(from, to);
            }
            var middle = (from + to) / 2;
            Task t1 = new Task(from, middle, op, compute);
            t1.fork();
            Task t2 = new Task(middle, to, op, compute);
            var res2 = t2.compute();
            var res1 = t1.join();
            return  compute.applyAsDouble(res1, res2);
        }
    }
    private static class Entry {
        private double average;
        private int cursor;
        private final double[] data;

        private Entry(int dataLength) {
            this.average = Double.NaN;
            double[] data = new double[dataLength];
            Arrays.fill(data, Double.NaN);
            this.data = data;
        }
    }

    private final Entry[] entries;

    public SummaryIndex(int entryLength, int dataLength) {
        var entries = new Entry[entryLength];
        for (var i = 0; i < entries.length; i++) {
            entries[i] = new Entry(dataLength);
        }
        this.entries = entries;
    }

    public void add(int entryIndex, double value) {
        var entry = entries[entryIndex];
        var cursor = entry.cursor;
        entry.data[cursor] = value;
        entry.cursor = (cursor + 1) % entry.data.length;
    }

    public double average(int entryIndex) {  // pas utilisée dans l'exercice
        return entries[entryIndex].average;
    }

    public double sumSummary() {
        var sum = 0.0;
        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];
            var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
            var average = stats.getAverage();

            entry.average = average;
            if (!Double.isNaN(average)) {
                sum += stats.getSum();
            }
        }
        return sum;
    }

    public double sequentialSumSummary(int from, int to) {
        var sum = 0.0;
        for (var i = from ; i < to ; ++i) {
            var entry = entries[i];
            var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
            var average = stats.getAverage();

            entry.average = stats.getAverage();
            if (! Double.isNaN(average)) {
                sum += stats.getSum();
            }
        }
        return sum;
    }

    public double averageSummary() {
        var sum = 0.0;
        var count = 0L;
        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];
            var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
            var average = stats.getAverage();
            entry.average = average;
            if (!Double.isNaN(average)) {
                sum += stats.getSum();
                count += stats.getCount();
            }
        }
        return sum / count;
    }

    public double sequentialAverageSummary(int from, int to) {
        var sum = 0.0;
        var count = 0L;
        for(var i = from ; i < to ; i++) {
            var entry = entries[i];
            var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
            var average = stats.getAverage();
            entry.average = average;
            if (!Double.isNaN(average)) {
                sum += stats.getSum();
                count += stats.getCount();
            }
        }
        return sum / count;
    }

    double parallelSumSummary() {
        var task = new Task( 0, entries.length, this::sequentialSumSummary, Double::sum);
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(task);
    }

    double parallelAverageSummary() {
        var task = new Task(0, entries.length, this::sequentialAverageSummary, (d1, d2) -> (d1 + d2) / 2);
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(task);
    }

    public static void main(String[] args) {
        var length = 20_000;
        var summaryIndex = new SummaryIndex(length, 200);

        var random = new Random(0);
        for (var i = 0; i < 10_000_000; i++) {
            summaryIndex.add(i % length, random.nextInt(1000));
        }
        System.out.println("Sum summary : ");
        System.out.println(summaryIndex.sumSummary());
        System.out.println(summaryIndex.sequentialSumSummary(0, length));
        System.out.println(summaryIndex.parallelSumSummary());

        System.out.println("Average summary : ");
        System.out.println(summaryIndex.averageSummary());
        System.out.println(summaryIndex.sequentialAverageSummary(0, length));
        System.out.println(summaryIndex.parallelAverageSummary());
    }
}