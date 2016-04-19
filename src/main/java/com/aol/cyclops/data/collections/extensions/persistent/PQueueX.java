package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.AmortizedPQueue;
import org.pcollections.PQueue;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PQueueX<T> extends PQueue<T>, PersistentCollectionX<T> {

    public static <T> PQueueX<T> of(T... values) {
        PQueue<T> result = empty();
        for(T value : values){
            result = result.plus(value);
        }
        
        return new PQueueXImpl<>(result);
    }

    public static <T> PQueueX<T> empty() {
        return new PQueueXImpl<>(AmortizedPQueue.empty());
    }

    public static <T> PQueueX<T> singleton(T value) {
        return PQueueX.<T>empty().plus(value);
    }

    /**
     * Construct a PQueueX from an Publisher
     * 
     * @param publisher
     *            to construct PQueueX from
     * @return PQueueX
     */
    public static <T> PQueueX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPQueueX();
    }
    public static <T> PQueueX<T> fromIterable(Iterable<T> iterable) {
        if (iterable instanceof PQueueX)
            return (PQueueX) iterable;
        if (iterable instanceof PQueue)
            return new PQueueXImpl<>((PQueue) (iterable));
        PQueue<T> res =  empty();
        Iterator<T> it = iterable.iterator();
        while (it.hasNext())
            res = res.plus(it.next());

        return new PQueueXImpl<>(res);
    }

    public static <T> PQueueX<T> fromCollection(Collection<T> stream) {
        if (stream instanceof PQueueX)
            return (PQueueX) (stream);
        if (stream instanceof PQueue)
            return new PQueueXImpl<>((PQueue) (stream));
        return PQueueX.<T>empty().plusAll(stream);
    }

    public static <T> PQueueX<T> fromStream(Stream<T> stream) {
        return Reducers.<T>toPQueueX().mapReduce(stream);
    }

    /**
     * Combine two adjacent elements in a PQueueX using the supplied
     * BinaryOperator This is a stateful grouping & reduction operation. The
     * output of a combination may in turn be combined with it's neighbor
     * 
     * <pre>
     * {@code 
     *  PQueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }
     * </pre>
     * 
     * @param predicate
     *            Test to see if two neighbors should be joined
     * @param op
     *            Reducer to combine neighbors
     * @return Combined / Partially Reduced PQueueX
     */
    default PQueueX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (PQueueX<T>) PersistentCollectionX.super.combine(predicate, op);
    }

    @Override
    default PQueueX<T> toPQueueX() {
        return this;
    }

    @Override
    default <R> PQueueX<R> unit(Collection<R> col) {
        return fromCollection(col);
    }

    @Override
    default <R> PQueueX<R> unit(R value) {
        return singleton(value);
    }

    @Override
    default <R> PQueueX<R> unitIterator(Iterator<R> it) {
        return fromIterable(() -> it);
    }

    @Override
    default <R> PQueueX<R> emptyUnit() {
        return empty();
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    default PQueue<T> toPSet() {
        return this;
    }

    default <X> PQueueX<X> from(Collection<X> col) {
        return  fromCollection(col);
    }

    default <T> Reducer<PQueue<T>> monoid() {
        return Reducers.toPQueue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#plus(java.lang.Object)
     */
    @Override
    public PQueueX<T> plus(T e);

    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#plusAll(java.util.Collection)
     */
    @Override
    public PQueueX<T> plusAll(Collection<? extends T> list);

    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#minus(java.lang.Object)
     */
    @Override
    public PQueueX<T> minus(Object e);

    /*
     * (non-Javadoc)
     * 
     * @see org.pcollections.PSet#minusAll(java.util.Collection)
     */
    @Override
    public PQueueX<T> minusAll(Collection<?> list);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * reverse()
     */
    @Override
    default PQueueX<T> reverse() {
        return (PQueueX<T>) PersistentCollectionX.super.reverse();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * filter(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> filter(Predicate<? super T> pred) {
        return (PQueueX<T>) PersistentCollectionX.super.filter(pred);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * map(java.util.function.Function)
     */
    @Override
    default <R> PQueueX<R> map(Function<? super T, ? extends R> mapper) {
        return (PQueueX<R>) PersistentCollectionX.super.map(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * flatMap(java.util.function.Function)
     */
    @Override
    default <R> PQueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (PQueueX<R>) PersistentCollectionX.super.flatMap(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * limit(long)
     */
    @Override
    default PQueueX<T> limit(long num) {
        return (PQueueX<T>) PersistentCollectionX.super.limit(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * skip(long)
     */
    @Override
    default PQueueX<T> skip(long num) {
        return (PQueueX<T>) PersistentCollectionX.super.skip(num);
    }

    default PQueueX<T> takeRight(int num) {
        return (PQueueX<T>) PersistentCollectionX.super.takeRight(num);
    }

    default PQueueX<T> dropRight(int num) {
        return (PQueueX<T>) PersistentCollectionX.super.dropRight(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * takeWhile(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> takeWhile(Predicate<? super T> p) {
        return (PQueueX<T>) PersistentCollectionX.super.takeWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * dropWhile(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> dropWhile(Predicate<? super T> p) {
        return (PQueueX<T>) PersistentCollectionX.super.dropWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * takeUntil(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> takeUntil(Predicate<? super T> p) {
        return (PQueueX<T>) PersistentCollectionX.super.takeUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * dropUntil(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> dropUntil(Predicate<? super T> p) {
        return (PQueueX<T>) PersistentCollectionX.super.dropUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * trampoline(java.util.function.Function)
     */
    @Override
    default <R> PQueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (PQueueX<R>) PersistentCollectionX.super.trampoline(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * slice(long, long)
     */
    @Override
    default PQueueX<T> slice(long from, long to) {
        return (PQueueX<T>) PersistentCollectionX.super.slice(from, to);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> PQueueX<T> sorted(Function<? super T, ? extends U> function) {
        return (PQueueX<T>) PersistentCollectionX.super.sorted(function);
    }

    default PQueueX<ListX<T>> grouped(int groupSize) {
        return (PQueueX<ListX<T>>) PersistentCollectionX.super.grouped(groupSize);
    }

    default <K, A, D> PQueueX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
        return (PQueueX) PersistentCollectionX.super.grouped(classifier, downstream);
    }

    default <K> PQueueX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
        return (PQueueX) PersistentCollectionX.super.grouped(classifier);
    }

    default <U> PQueueX<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (PQueueX) PersistentCollectionX.super.zip(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> PQueueX<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (PQueueX<R>) PersistentCollectionX.super.zip(other, zipper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * permutations()
     */
    @Override
    default PQueueX<ReactiveSeq<T>> permutations() {

        return (PQueueX<ReactiveSeq<T>>) PersistentCollectionX.super.permutations();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * combinations(int)
     */
    @Override
    default PQueueX<ReactiveSeq<T>> combinations(int size) {

        return (PQueueX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations(size);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * combinations()
     */
    @Override
    default PQueueX<ReactiveSeq<T>> combinations() {

        return (PQueueX<ReactiveSeq<T>>) PersistentCollectionX.super.combinations();
    }

    default PQueueX<ListX<T>> sliding(int windowSize) {
        return (PQueueX<ListX<T>>) PersistentCollectionX.super.sliding(windowSize);
    }

    default PQueueX<ListX<T>> sliding(int windowSize, int increment) {
        return (PQueueX<ListX<T>>) PersistentCollectionX.super.sliding(windowSize, increment);
    }

    default PQueueX<T> scanLeft(Monoid<T> monoid) {
        return (PQueueX<T>) PersistentCollectionX.super.scanLeft(monoid);
    }

    default <U> PQueueX<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (PQueueX<U>) PersistentCollectionX.super.scanLeft(seed, function);
    }

    default PQueueX<T> scanRight(Monoid<T> monoid) {
        return (PQueueX<T>) PersistentCollectionX.super.scanRight(monoid);
    }

    default <U> PQueueX<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
        return (PQueueX<U>) PersistentCollectionX.super.scanRight(identity, combiner);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * plusInOrder(java.lang.Object)
     */
    @Override
    default PQueueX<T> plusInOrder(T e) {

        return (PQueueX<T>) PersistentCollectionX.super.plusInOrder(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * cycle(int)
     */
    @Override
    default PQueueX<T> cycle(int times) {

        return (PQueueX<T>) PersistentCollectionX.super.cycle(times);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    default PQueueX<T> cycle(Monoid<T> m, int times) {

        return (PQueueX<T>) PersistentCollectionX.super.cycle(m, times);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * cycleWhile(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> cycleWhile(Predicate<? super T> predicate) {

        return (PQueueX<T>) PersistentCollectionX.super.cycleWhile(predicate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * cycleUntil(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> cycleUntil(Predicate<? super T> predicate) {

        return (PQueueX<T>) PersistentCollectionX.super.cycleUntil(predicate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> PQueueX<Tuple2<T, U>> zipStream(Stream<? extends U> other) {

        return (PQueueX) PersistentCollectionX.super.zipStream(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> PQueueX<Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (PQueueX<Tuple2<T, U>>) PersistentCollectionX.super.zip(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> PQueueX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {

        return (PQueueX) PersistentCollectionX.super.zip3(second, third);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zip4(java.util.stream.Stream, java.util.stream.Stream,
     * java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> PQueueX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third, Stream<T4> fourth) {

        return (PQueueX<Tuple4<T, T2, T3, T4>>) PersistentCollectionX.super.zip4(second, third, fourth);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * zipWithIndex()
     */
    @Override
    default PQueueX<Tuple2<T, Long>> zipWithIndex() {

        return (PQueueX<Tuple2<T, Long>>) PersistentCollectionX.super.zipWithIndex();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * distinct()
     */
    @Override
    default PQueueX<T> distinct() {

        return (PQueueX<T>) PersistentCollectionX.super.distinct();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * sorted()
     */
    @Override
    default PQueueX<T> sorted() {

        return (PQueueX<T>) PersistentCollectionX.super.sorted();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * sorted(java.util.Comparator)
     */
    @Override
    default PQueueX<T> sorted(Comparator<? super T> c) {

        return (PQueueX<T>) PersistentCollectionX.super.sorted(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * skipWhile(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> skipWhile(Predicate<? super T> p) {

        return (PQueueX<T>) PersistentCollectionX.super.skipWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * skipUntil(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> skipUntil(Predicate<? super T> p) {

        return (PQueueX<T>) PersistentCollectionX.super.skipUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * limitWhile(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> limitWhile(Predicate<? super T> p) {

        return (PQueueX<T>) PersistentCollectionX.super.limitWhile(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * limitUntil(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> limitUntil(Predicate<? super T> p) {

        return (PQueueX<T>) PersistentCollectionX.super.limitUntil(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * intersperse(java.lang.Object)
     */
    @Override
    default PQueueX<T> intersperse(T value) {

        return (PQueueX<T>) PersistentCollectionX.super.intersperse(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * shuffle()
     */
    @Override
    default PQueueX<T> shuffle() {

        return (PQueueX<T>) PersistentCollectionX.super.shuffle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * skipLast(int)
     */
    @Override
    default PQueueX<T> skipLast(int num) {

        return (PQueueX<T>) PersistentCollectionX.super.skipLast(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * limitLast(int)
     */
    @Override
    default PQueueX<T> limitLast(int num) {

        return (PQueueX<T>) PersistentCollectionX.super.limitLast(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * onEmpty(java.lang.Object)
     */
    @Override
    default PQueueX<T> onEmpty(T value) {

        return (PQueueX<T>) PersistentCollectionX.super.onEmpty(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PQueueX<T> onEmptyGet(Supplier<? extends T> supplier) {

        return (PQueueX<T>) PersistentCollectionX.super.onEmptyGet(supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PQueueX<T> onEmptyThrow(Supplier<? extends X> supplier) {

        return (PQueueX<T>) PersistentCollectionX.super.onEmptyThrow(supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * shuffle(java.util.Random)
     */
    @Override
    default PQueueX<T> shuffle(Random random) {

        return (PQueueX<T>) PersistentCollectionX.super.shuffle(random);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * ofType(java.lang.Class)
     */
    @Override
    default <U> PQueueX<U> ofType(Class<? extends U> type) {

        return (PQueueX<U>) PersistentCollectionX.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * filterNot(java.util.function.Predicate)
     */
    @Override
    default PQueueX<T> filterNot(Predicate<? super T> fn) {

        return (PQueueX<T>) PersistentCollectionX.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * notNull()
     */
    @Override
    default PQueueX<T> notNull() {

        return (PQueueX<T>) PersistentCollectionX.super.notNull();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * removeAll(java.util.stream.Stream)
     */
    @Override
    default PQueueX<T> removeAll(Stream<? extends T> stream) {

        return (PQueueX<T>) PersistentCollectionX.super.removeAll(stream);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * removeAll(java.lang.Iterable)
     */
    @Override
    default PQueueX<T> removeAll(Iterable<? extends T> it) {

        return (PQueueX<T>) PersistentCollectionX.super.removeAll(it);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * removeAll(java.lang.Object[])
     */
    @Override
    default PQueueX<T> removeAll(T... values) {

        return (PQueueX<T>) PersistentCollectionX.super.removeAll(values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * retainAll(java.lang.Iterable)
     */
    @Override
    default PQueueX<T> retainAll(Iterable<? extends T> it) {

        return (PQueueX<T>) PersistentCollectionX.super.retainAll(it);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * retainAll(java.util.stream.Stream)
     */
    @Override
    default PQueueX<T> retainAll(Stream<? extends T> seq) {

        return (PQueueX<T>) PersistentCollectionX.super.retainAll(seq);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * retainAll(java.lang.Object[])
     */
    @Override
    default PQueueX<T> retainAll(T... values) {

        return (PQueueX<T>) PersistentCollectionX.super.retainAll(values);
    }

 
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * cast(java.lang.Class)
     */
    @Override
    default <U> PQueueX<U> cast(Class<? extends U> type) {

        return (PQueueX<U>) PersistentCollectionX.super.cast(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#
     * patternMatch(java.lang.Object, java.util.function.Function)
     */
    @Override
    default <R> PQueueX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {

        return (PQueueX<R>) PersistentCollectionX.super.patternMatch(case1, otherwise);
    }

    @Override
    default <C extends Collection<? super T>> PQueueX<C> grouped(int size, Supplier<C> supplier) {

        return (PQueueX<C>) PersistentCollectionX.super.grouped(size, supplier);
    }

    @Override
    default PQueueX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {

        return (PQueueX<ListX<T>>) PersistentCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default PQueueX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (PQueueX<ListX<T>>) PersistentCollectionX.super.groupedStatefullyWhile(predicate);
    }

    @Override
    default PQueueX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {

        return (PQueueX<ListX<T>>) PersistentCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> PQueueX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {

        return (PQueueX<C>) PersistentCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> PQueueX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {

        return (PQueueX<C>) PersistentCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default PQueueX<T> removeAll(Seq<? extends T> stream) {

        return (PQueueX<T>) PersistentCollectionX.super.removeAll(stream);
    }

    @Override
    default PQueueX<T> retainAll(Seq<? extends T> stream) {

        return (PQueueX<T>) PersistentCollectionX.super.retainAll(stream);
    }

}
