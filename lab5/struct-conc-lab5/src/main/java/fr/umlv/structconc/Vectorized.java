package fr.umlv.structconc;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class Vectorized {

    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

    public static int sumLoop(int[] array) {
        var sum = 0;
        for (var value : array) {
            sum += value;
        }
        return sum;
    }

    public static int sumReduceLane(int[] array) {
        var i = 0;
        var sum = 0;
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit ; i += SPECIES.length()) {
            var v = IntVector.fromArray(SPECIES, array, i);
            sum += v.reduceLanes(VectorOperators.ADD);
        }

        for (; i < array.length ; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static int sumLanewise(int[] array) {
        var result = IntVector.zero(SPECIES);
        var i = 0;
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit ; i += SPECIES.length()) {
            var v = IntVector.fromArray(SPECIES, array, i);
            result = result.add(v);
        }
        var sum = result.reduceLanes(VectorOperators.ADD);
        for (; i < array.length ; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static int differenceLanewise(int[] array) {
        if (array.length == 0) {
            return 0;
        }
        var i = 0;
        var result = IntVector.zero(SPECIES);
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit ; i += SPECIES.length()) {
            var v = IntVector.fromArray(SPECIES, array, i);
            result = result.lanewise(VectorOperators.SUB, v);
        }
        var sum = result.reduceLanes(VectorOperators.ADD);
        for (; i < array.length ; ++i) {
            sum -= array[i];
        }
        return sum;
    }

    public static int[] minmax(int[] array) {
        int[] result = new int[2];
        var max = IntVector.zero(SPECIES).broadcast(Integer.MIN_VALUE);
        var min = IntVector.zero(SPECIES).broadcast(Integer.MAX_VALUE);
        var limit = array.length - (array.length % SPECIES.length());
        var i = 0;

        for (; i < limit ; i += SPECIES.length()) {
            var v = IntVector.fromArray(SPECIES, array, i);
            max = max.max(v);
            min = min.min(v);
        }
        result[0] = min.reduceLanes(VectorOperators.MIN);
        result[1] = max.reduceLanes(VectorOperators.MAX);
        for (; i < array.length ; ++i) {
            if (result[0] > array[i]) {
                result[0] = array[i];
            }
            if (result[1] < array[i]) {
                result[1] = array[i];
            }
        }
        return result;
    }
}

/*
ForkJoin/Vector etc : But = calcul parallèle
Fork join
-> prend le calcul et le combine sur plusieurs coeurs
Vector
-> pour chaque coeur, accélère le calcul
fork/join: repartir la tache sur plusiuers coeurs, SIMD faire plusieurs calculs sur 1 seul coeur


Question 4:
On gagne un peu en performance, mais pas autant qu'on voudrait.
Si on veut vraiment tirer parti des Vectors, il faut utiliser un vector aussi pour la sum.
 */