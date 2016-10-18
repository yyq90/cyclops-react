package com.aol.cyclops;

import java.util.List;

import org.pcollections.AmortizedPQueue;
import org.pcollections.ConsPStack;
import org.pcollections.HashTreePBag;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.OrderedPSet;
import org.pcollections.PBag;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PQueue;
import org.pcollections.PSet;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.PMapX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.types.mixins.TupleWrapper;

//@UtilityClass
public class Reducers {
    private static <T> PQueue<T> queueOf(final T... values) {
        PQueue<T> result = AmortizedPQueue.empty();
        for (final T value : values) {
            result = result.plus(value);
        }
        return result;

    }

    public static <T> Reducer<PQueueX<T>> toPQueueX() {
        return Reducer.<PQueueX<T>> of(PQueueX.empty(), (final PQueueX<T> a) -> b -> a.plusAll(b), (final T x) -> PQueueX.singleton(x));
    }

    public static <T> Reducer<POrderedSetX<T>> toPOrderedSetX() {
        return Reducer.<POrderedSetX<T>> of(POrderedSetX.<T> empty(), (final POrderedSetX<T> a) -> b -> a.plusAll(b),
                                            (final T x) -> POrderedSetX.singleton(x));
    }

    public static <T> Reducer<PSetX<T>> toPSetX() {
        return Reducer.<PSetX<T>> of(PSetX.empty(), (final PSetX<T> a) -> b -> a.plusAll(b), (final T x) -> PSetX.singleton(x));
    }

    public static <T> Reducer<PStackX<T>> toPStackX() {
        return Reducer.<PStackX<T>> of(PStackX.empty(), (final PStackX<T> a) -> b -> a.plusAll(b), (final T x) -> PStackX.singleton(x));
    }

    public static <T> Reducer<PVectorX<T>> toPVectorX() {
        return Reducer.<PVectorX<T>> of(PVectorX.empty(), (final PVectorX<T> a) -> b -> a.plusAll(b), (final T x) -> PVectorX.singleton(x));
    }

    public static <T> Reducer<PBagX<T>> toPBagX() {
        return Reducer.<PBagX<T>> of(PBagX.empty(), (final PBagX<T> a) -> b -> a.plusAll(b), (final T x) -> PBagX.singleton(x));
    }

    private static <T> PQueue<T> queueSingleton(final T value) {
        PQueue<T> result = AmortizedPQueue.empty();
        result = result.plus(value);
        return result;
    }

    public static <T> Reducer<PQueue<T>> toPQueue() {
        return Reducer.<PQueue<T>> of(AmortizedPQueue.empty(), (final PQueue<T> a) -> b -> a.plusAll(b), (final T x) -> queueSingleton(x));
    }

    public static <T> Reducer<POrderedSet<T>> toPOrderedSet() {
        return Reducer.<POrderedSet<T>> of(OrderedPSet.empty(), (final POrderedSet<T> a) -> b -> a.plusAll(b),
                                           (final T x) -> OrderedPSet.singleton(x));
    }

    public static <T> Reducer<PBag<T>> toPBag() {
        return Reducer.<PBag<T>> of(HashTreePBag.empty(), (final PBag<T> a) -> b -> a.plusAll(b), (final T x) -> HashTreePBag.singleton(x));
    }

    public static <T> Reducer<PSet<T>> toPSet() {
        return Reducer.<PSet<T>> of(HashTreePSet.empty(), (final PSet<T> a) -> b -> a.plusAll(b), (final T x) -> HashTreePSet.singleton(x));
    }

    public static <T> Reducer<PVector<T>> toPVector() {
        return Reducer.<PVector<T>> of(TreePVector.empty(), (final PVector<T> a) -> b -> a.plusAll(b), (final T x) -> TreePVector.singleton(x));
    }

    public static <T> Reducer<PStack<T>> toPStack() {
        return Reducer.<PStack<T>> of(ConsPStack.empty(), (final PStack<T> a) -> b -> a.plusAll(a.size(), b), (final T x) -> ConsPStack.singleton(x));
    }

    public static <T> Reducer<PStack<T>> toPStackReversed() {
        return Reducer.<PStack<T>> of(ConsPStack.empty(), (final PStack<T> a) -> b -> a.plusAll(b), (final T x) -> ConsPStack.singleton(x));
    }

    public static <K, V> Reducer<PMap<K, V>> toPMap() {
        return Reducer.<PMap<K, V>> of(HashTreePMap.empty(), (final PMap<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return HashTreePMap.singleton((K) w.get(0), (V) w.get(1));
        });
    }

    public static <K, V> Reducer<PMapX<K, V>> toPMapX() {
        return Reducer.<PMapX<K, V>> of(PMapX.empty(), (final PMapX<K, V> a) -> b -> a.plusAll(b), (in) -> {
            final List w = ((TupleWrapper) () -> in).values();
            return PMapX.singleton((K) w.get(0), (V) w.get(1));
        });
    }

    public static Monoid<String> toString(final String joiner) {
        return Monoid.of("", (a, b) -> a + joiner + b);
    }

    public static Monoid<Integer> toTotalInt() {
        return Monoid.of(0, a -> b -> a + b);
    }

    public static Reducer<Integer> toCountInt() {

        return Reducer.of(0, a -> b -> a + 1, (x) -> 1);
    }

    public static Monoid<Double> toTotalDouble() {
        return Monoid.of(0.0, (a, b) -> a + b);
    }

    public static Reducer<Double> toCountDouble() {
        return Reducer.of(0.0, a -> b -> a + 1, (x) -> Double.valueOf("" + x));
    }

}
