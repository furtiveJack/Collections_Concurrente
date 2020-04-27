package fr.umlv.structconc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("static-method")
public class VectorizedTest {
    private static Stream<Arguments> provideIntArrays() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum)));
    }

    private static Stream<Arguments> provideIntArraysForMinMax() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, -1000, 1000).toArray())
                .map(array -> Arguments.of(
                        array,
                        List.of(
                                Arrays.stream(array).reduce(Integer.MAX_VALUE, Integer::min),
                                Arrays.stream(array).reduce(Integer.MIN_VALUE, Integer::max)
                        ).stream().mapToInt(i -> i).toArray()
                    )
                );
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sum(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumLoop(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sumReduceLane(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumReduceLane(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sumLaneWise(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumLanewise(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void subLaneWise(int[] array, int expected) {
        assertEquals((-1) * expected, Vectorized.differenceLanewise(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArraysForMinMax")
    public void minmaxLanewisePositiveArray(int[] array, int[] expected) {
        var res = Vectorized.minmax(array);
        assertEquals(expected[0], res[0]);
        assertEquals(expected[1], res[1]);
    }
}