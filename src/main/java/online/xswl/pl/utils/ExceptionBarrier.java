package online.xswl.pl.utils;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Reduce the scope of an exception and prevent it from spreading to code that still works
 *
 * @author PL
 */
public abstract class ExceptionBarrier extends Transformer {


    @FunctionalInterface
    public interface ExceptionLoggingConsumer {
        void logging(Throwable ex);
    }

    /**
     * 安全执行一段逻辑
     *
     * @param runnableEX          要执行的逻辑，兼容检查时异常
     * @param throwableConsumerEX 如何处理执行过程中发生的异常
     * @param <EX>                异常类型
     */
    @SuppressWarnings("unchecked")
    public static <EX extends Throwable> void execute(RunnableEX<EX> runnableEX, ThrowableConsumerEX<EX> throwableConsumerEX) {
        assert throwableConsumerEX != null;
        try {
            runnableEX.run();
        } catch (Throwable e) {
            throwableConsumerEX.accept((EX) e);
        }
    }

    /**
     * 安全的获取数据
     *
     * @param supplierEX          获取数据的函数，兼容检查时异常
     * @param orElseGet           如果获取过程中发生异常或获取到的数据为null，替代的获取方案
     * @param throwableConsumerEX 如何处理执行过程中发生的异常
     * @param <EX>                异常类型
     */
    public static <T, EX extends Throwable> T getOrElseGet(SupplierEX<T, EX> supplierEX, Supplier<T> orElseGet, ThrowableConsumerEX<EX> throwableConsumerEX) {
        return getOpt(supplierEX, throwableConsumerEX).orElseGet(orElseGet);
    }

    /**
     * 安全的获取数据
     *
     * @param supplierEX          获取数据的函数，兼容检查时异常
     * @param orElse              如果获取过程中发生异常或获取到的数据为null，替代的值
     * @param throwableConsumerEX 如何处理执行过程中发生的异常
     * @param <EX>                异常类型
     */
    public static <T, EX extends Throwable> T getOrElse(SupplierEX<T, EX> supplierEX, T orElse, ThrowableConsumerEX<EX> throwableConsumerEX) {
        return getOpt(supplierEX, throwableConsumerEX).orElse(orElse);
    }

    public static <T, EX extends Throwable> T getOrNull(SupplierEX<T, EX> supplierEX, ThrowableConsumerEX<EX> throwableConsumerEX) {
        return getOpt(supplierEX, throwableConsumerEX).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static <T, EX extends Throwable> Optional<T> getOpt(SupplierEX<T, EX> supplierEX, ThrowableConsumerEX<EX> throwableConsumerEX) {
        assert throwableConsumerEX != null;
        T result = null;
        try {
            result = supplierEX.get();
        } catch (Throwable e) {
            throwableConsumerEX.accept((EX) e);
        }
        return Optional.ofNullable(result);
    }

    @SuppressWarnings("unchecked")
    public static <T, EX extends Throwable> Opt<T> getOpt_(SupplierEX<T, EX> supplierEX, ThrowableConsumerEX<EX> throwableConsumerEX) {
        assert throwableConsumerEX != null;
        T result = null;
        try {
            result = supplierEX.get();
        } catch (Throwable e) {
            throwableConsumerEX.accept((EX) e);
        }
        return Opt.of(result);
    }


    /**
     * 执行逻辑，如果发生异常，记录但不抛出异常
     *
     * @param runnableEX 执行的逻辑
     */
    public static void loggingExecute(RunnableEX<?> runnableEX) {
        execute(runnableEX, manager().exceptionLoggingConsumer::logging);
    }

    /**
     * 执行逻辑，如果发生异常，忽略之
     *
     * @param runnableEX 执行的逻辑
     */
    public static void silentExecute(RunnableEX<?> runnableEX) {
        execute(runnableEX, ex -> {});
    }

    /**
     * 执行逻辑，如果发生异常，抛出
     *
     * @param runnableEX 执行的逻辑
     */
    public static void throwingExecute(RunnableEX<?> runnableEX) {
        execute(runnableEX, WrappedException::wrapThrow);
    }

    public static Runnable loggingRunnable(RunnableEX<?> runnableEX) {
        return () -> execute(runnableEX, manager().exceptionLoggingConsumer::logging);
    }

    public static Runnable silentRunnable(RunnableEX<?> runnableEX) {
        return () -> execute(runnableEX, ex -> {});
    }

    public static Runnable throwingRunnable(RunnableEX<?> runnableEX) {
        return () -> execute(runnableEX, WrappedException::wrapThrow);
    }

    /**
     * 获取数据，如果发生异常，记录但不抛出异常;如果未能获取到的数据或数据为null，返回null
     *
     * @param supplierEX 如何获取数据
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T loggingGetOrNull(SupplierEX<T, ?> supplierEX) {
        return loggingGetOpt(supplierEX).orElse(null);
    }

    public static <T> Supplier<T> loggingSupplier(SupplierEX<T, ?> supplierEX) {
        return () -> loggingGetOpt(supplierEX).orElse(null);
    }


    /**
     * 获取数据，如果发生异常，记录但不抛出异常;如果未能获取到的数据或数据为null，返回替代函数获取到的数据
     *
     * @param supplierEX 如何获取数据
     * @param orElseGet  替代函数
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T loggingGetOrElseGet(SupplierEX<T, ?> supplierEX, Supplier<T> orElseGet) {
        return loggingGetOpt(supplierEX).orElseGet(orElseGet);
    }

    /**
     * 获取数据，如果发生异常，记录但不抛出异常;如果未能获取到的数据或数据为null，返回替代值
     *
     * @param supplierEX 如何获取数据
     * @param orElse     替代值
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T loggingGetOrElse(SupplierEX<T, ?> supplierEX, T orElse) {
        return loggingGetOpt(supplierEX).orElse(orElse);
    }

    public static <T> Optional<T> loggingGetOpt(SupplierEX<T, ?> supplierEX) {
        return getOpt(supplierEX, manager().exceptionLoggingConsumer::logging);
    }

    public static <T> Opt<T> loggingGetOpt_(SupplierEX<T, ?> supplierEX) {
        return getOpt_(supplierEX, manager().exceptionLoggingConsumer::logging);
    }

    /**
     * 获取数据，如果发生异常，忽略之；如果未能获取到数据或数据为null，返回null
     *
     * @param supplierEX 如何获取数据
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T silentGetOrNull(SupplierEX<T, ?> supplierEX) {
        return silentGetOpt(supplierEX).orElse(null);
    }

    public static <T> Supplier<T> silentSupplier(SupplierEX<T, ?> supplierEX) {
        return () -> silentGetOpt(supplierEX).orElse(null);
    }

    /**
     * 获取数据，如果发生异常，忽略之；如果未能获取到数据或数据为null，返回替代函数获取的值
     *
     * @param supplierEX 如何获取数据
     * @param orElseGet  替代函数
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T silentGetOrElseGet(SupplierEX<T, ?> supplierEX, Supplier<T> orElseGet) {
        return silentGetOpt(supplierEX).orElseGet(orElseGet);
    }

    /**
     * 获取数据，如果发生异常，忽略之；如果未能获取到数据或数据为null，返回替代值
     *
     * @param supplierEX 如何获取数据
     * @param orElse     替代值
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T silentGetOrElse(SupplierEX<T, ?> supplierEX, T orElse) {
        return silentGetOpt(supplierEX).orElse(orElse);
    }

    public static <T> Optional<T> silentGetOpt(SupplierEX<T, ?> supplierEX) {
        return getOpt(supplierEX, t -> {});
    }

    public static <T> Opt<T> silentGetOpt_(SupplierEX<T, ?> supplierEX) {
        return getOpt_(supplierEX, t -> {});
    }

    /**
     * 获取数据，如果发生异常，抛出异常
     * <p>(该方法多用于吞掉不易发生的检查时异常)</p>
     *
     * @param supplierEX 如何获取数据
     * @param <T>        数据类型
     * @return 数据
     * @throws WrappedException 当发生异常时，抛出的异常（将检查时异常包装成运行时异常）
     */
    public static <T> T throwingGet(SupplierEX<T, ?> supplierEX) {
        return getOrElse(supplierEX, null, WrappedException::wrapThrow);
    }

    public static <T> Supplier<T> throwingSupplier(SupplierEX<T, ?> supplierEX) {
        return () -> getOrElse(supplierEX, null, WrappedException::wrapThrow);
    }

    /**
     * @see #loggingGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends List<E>, E> T loggingGetList(SupplierEX<T, ?> supplierEX) {
        return loggingGetOrElseGet(supplierEX, () -> (T) new ArrayList<>(0));
    }

    /**
     * @see #silentGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends List<E>, E> T silentGetList(SupplierEX<T, ?> supplierEX) {
        return silentGetOrElseGet(supplierEX, () -> (T) new ArrayList<>(0));
    }

    /**
     * @see #loggingGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Set<E>, E> T loggingGetSet(SupplierEX<T, ?> supplierEX) {
        return loggingGetOrElseGet(supplierEX, () -> (T) new LinkedHashSet<>(0));
    }

    /**
     * @see #silentGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Set<E>, E> T silentGetSet(SupplierEX<T, ?> supplierEX) {
        return silentGetOrElseGet(supplierEX, () -> (T) new LinkedHashSet<>(0));
    }

    /**
     * @see #loggingGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Map<K, V>, K, V> T loggingGetMap(SupplierEX<T, ?> supplierEX) {
        return loggingGetOrElseGet(supplierEX, () -> (T) new LinkedHashMap<>(0));
    }

    /**
     * @see #silentGetOrElseGet(SupplierEX, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Map<K, V>, K, V> T silentGetMap(SupplierEX<T, ?> supplierEX) {
        return silentGetOrElseGet(supplierEX, () -> (T) new LinkedHashMap<>(0));
    }

    /**
     * @see #loggingGetOrElse(SupplierEX, Object)
     */
    public static String loggingGetStr(SupplierEX<String, ?> supplierEX) {
        return loggingGetOrElse(supplierEX, "");
    }

    /**
     * @see #silentGetOrElse(SupplierEX, Object)
     */
    public static String silentGetStr(SupplierEX<String, ?> supplierEX) {
        return silentGetOrElse(supplierEX, "");
    }

    public static <T> Ensurer<T> def(T def) {
        return Ensurer.of(def);
    }

    public static <T> Ensurer<T> def(T def, Predicate<T> check) {
        return Ensurer.of(def, check);
    }

    public static <T> T ensure(T o, T def) {
        return o != null ? o : def;
    }

    public static <T> T ensure(T o, Supplier<T> def) {
        return o != null ? o : def.get();
    }

    public static <T> T ensureThen(T o, Supplier<T> def, Consumer<T> then) {
        return o != null ? o : fluent(def.get(), then);
    }

    public static <T> T ensure(T o, Predicate<T> check, T def) {
        return check.test(o) ? o : def;
    }

    public static <T> T ensure(T o, Predicate<T> check, Supplier<T> def) {
        return check.test(o) ? o : def.get();
    }

    public static <T> T ensureThen(T o, Predicate<T> check, Supplier<T> def, Consumer<T> then) {
        return check.test(o) ? o : fluent(def.get(), then);
    }

    public static <T extends ValueValidator> T ensure(T o, T def) {
        return Checker.isValueValid(o) ? o : def;
    }

    public static <T extends ValueValidator> T ensure(T o, Supplier<T> def) {
        return Checker.isValueValid(o) ? o : def.get();
    }

    public static <T extends ValueValidator> T ensureThen(T o, Supplier<T> def, Consumer<T> then) {
        return Checker.isValueValid(o) ? o : fluent(def.get(), then);
    }

    public static <T> T ensureValueValid(T o, T def) {
        return Checker.isValueValid(o) ? o : def;
    }

    public static <T> T ensureValueValid(T o, Supplier<T> def) {
        return Checker.isValueValid(o) ? o : def.get();
    }

    public static <T> T ensureValueValidThen(T o, Supplier<T> def, Consumer<T> then) {
        return Checker.isValueValid(o) ? o : fluent(def.get(), then);
    }

    public static String ensure(String origin) {
        return origin != null ? origin : "";
    }

    public static String ensureThen(String origin, Consumer<String> then) {
        return origin != null ? origin : fluent("", then);
    }

    public static boolean ensure(Boolean origin) {
        return origin != null ? origin : false;
    }

    public static boolean ensureThen(Boolean origin, Consumer<Boolean> then) {
        return origin != null ? origin : fluent(false, then);
    }

    public static byte ensure(Byte origin) {
        return origin != null ? origin : 0;
    }

    public static byte ensureThen(Byte origin, Consumer<Byte> then) {
        return origin != null ? origin : fluent((byte) 0, then);
    }

    public static short ensure(Short origin) {
        return origin != null ? origin : 0;
    }

    public static short ensureThen(Short origin, Consumer<Short> then) {
        return origin != null ? origin : fluent((short) 0, then);
    }

    public static int ensure(Integer origin) {
        return origin != null ? origin : 0;
    }

    public static int ensureThen(Integer origin, Consumer<Integer> then) {
        return origin != null ? origin : fluent(0, then);
    }

    public static long ensure(Long origin) {
        return origin != null ? origin : 0L;
    }

    public static long ensureThen(Long origin, Consumer<Long> then) {
        return origin != null ? origin : fluent(0L, then);
    }

    public static float ensure(Float origin) {
        return origin != null ? origin : 0;
    }

    public static float ensureThen(Float origin, Consumer<Float> then) {
        return origin != null ? origin : fluent(0F, then);
    }

    public static double ensure(Double origin) {
        return origin != null ? origin : 0D;
    }

    public static double ensureThen(Double origin, Consumer<Double> then) {
        return origin != null ? origin : fluent(0D, then);
    }

    public static BigDecimal ensure(BigDecimal origin) {
        return origin != null ? origin : BigDecimal.ZERO;
    }

    public static BigDecimal ensureThen(BigDecimal origin, Consumer<BigDecimal> then) {
        return origin != null ? origin : fluent(BigDecimal.ZERO, then);
    }

    public static <E> List<E> ensure(List<E> origin) {
        return origin != null ? origin : new ArrayList<>();
    }

    public static <E> List<E> ensureThen(List<E> origin, Consumer<List<E>> then) {
        return origin != null ? origin : fluent(new ArrayList<>(), then);
    }

    public static <E> Set<E> ensure(Set<E> origin) {
        return origin != null ? origin : new HashSet<>();
    }

    public static <E> Set<E> ensureThen(Set<E> origin, Consumer<Set<E>> then) {
        return origin != null ? origin : fluent(new HashSet<>(), then);
    }

    public static <K, V> Map<K, V> ensure(Map<K, V> origin) {
        return origin != null ? origin : new HashMap<>();
    }

    public static <K, V> Map<K, V> ensureThen(Map<K, V> origin, Consumer<Map<K, V>> then) {
        return origin != null ? origin : fluent(new HashMap<>(), then);
    }


    /**
     * 安全的获取集合的stream，如果集合为null，返回empty；返回的stream会预先过滤掉null元素
     *
     * @param collection 集合
     * @param <T>        集合类型
     * @return stream
     */
    public static <T> Stream<T> stream(Collection<T> collection) {
        return collection != null ? collection.stream().filter(Objects::nonNull) : Stream.empty();
    }

    /**
     * 安全的获取数组的stream，如果数组为null，返回empty；返回的stream会预先过滤掉null元素
     *
     * @param array 数组
     * @param <T>   数组元素类型
     * @return stream
     */
    public static <T> Stream<T> stream(T[] array) {
        return array != null ? Arrays.stream(array).filter(Objects::nonNull) : Stream.empty();
    }

    /**
     * 安全的得到一个Map.Entry的stream
     *
     * @param map Map
     * @param <K> Map的键类型
     * @param <V> Map的值类型
     * @return Map.Entry的stream或者当Map为null时返回一个空的stream
     */
    public static <K, V> Stream<Entry<K, V>> stream(Map<K, V> map) {
        return map != null ? map.entrySet().stream().filter(Objects::nonNull) : Stream.empty();
    }


    /**
     * 安全的获取集合的stream，如果集合为null，返回empty；返回的stream会预先过滤掉null元素
     *
     * @param collection 集合
     * @param <T>        集合类型
     * @return stream
     */
    public static <T> StreamEx<T> streamEx(Collection<T> collection) {
        return collection != null ? StreamEx.of(collection).filter(Objects::nonNull) : StreamEx.empty();
    }

    /**
     * 安全的获取数组的stream，如果数组为null，返回empty；返回的stream会预先过滤掉null元素
     *
     * @param array 数组
     * @param <T>   数组元素类型
     * @return stream
     */
    public static <T> StreamEx<T> streamEx(T[] array) {
        return array != null ? StreamEx.of(array).filter(Objects::nonNull) : StreamEx.empty();
    }

    /**
     * 安全的得到一个Map.Entry的stream
     *
     * @param map Map
     * @param <K> Map的键类型
     * @param <V> Map的值类型
     * @return Map.Entry的stream或者当Map为null时返回一个空的stream
     */
    public static <K, V> EntryStream<K, V> streamEx(Map<K, V> map) {
        return map != null ? EntryStream.of(map).filter(Objects::nonNull) : EntryStream.empty();
    }



    public static int length(String s) {
        return s != null ? s.length() : 0;
    }
    public static <T> int length(T[] array) {
        return array != null ? array.length : 0;
    }

    public static <E> int size(Collection<E> collection) {
        return collection != null ? collection.size() : 0;
    }

    public static <K, V> int size(Map<K, V> map) {
        return map != null ? map.size() : 0;
    }

    public static <E> E first(List<E> list) {
        return size(list) > 0 ? list.get(0) : null;
    }

    public static <E> E first(Iterable<E> iterable) {
        if (iterable != null) {
            Iterator<E> iterator = iterable.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }

    public static <E> Optional<E> firstOpt(List<E> list) {
        return Optional.ofNullable(first(list));
    }

    public static <E> Optional<E> firstOpt(Iterable<E> iterable) {
        return Optional.ofNullable(first(iterable));
    }

    public static <E> Opt<E> firstOpt_(List<E> list) {
        return Opt.of(first(list));
    }

    public static <E> Opt<E> firstOpt_(Iterable<E> iterable) {
        return Opt.of(first(iterable));
    }

    public static <E> E last(List<E> list) {
        int size = size(list);
        return size > 0 ? list.get(size - 1) : null;
    }

    public static <E> E last(Iterable<E> iterable) {
        E last = null;
        if (iterable != null) {
            Iterator<E> iterator = iterable.iterator();
            if (iterator.hasNext()) {
                last = iterator.next();
            }
        }
        return last;
    }

    public static <E> Optional<E> lastOpt(List<E> list) {
        return Optional.ofNullable(last(list));
    }

    public static <E> Optional<E> lastOpt(Iterable<E> iterable) {
        return Optional.ofNullable(last(iterable));
    }

    public static <E> Opt<E> lastOpt_(List<E> list) {
        return Opt.of(last(list));
    }

    public static <E> Opt<E> lastOpt_(Iterable<E> iterable) {
        return Opt.of(last(iterable));
    }

    public static <T, R> R nonNullThen(T origin, Function<T, R> then) {
        return origin == null ? null : then.apply(origin);
    }

    /**
     * 强转并执行，强转失败会打印日志
     *
     * @param s    原数据
     * @param then 强转后执行的动作
     * @param <S>  原类型（需要是目标类型的父类）
     * @param <T>  强转的目标类型
     * @param <R>  执行的结果
     * @return 如果强转失败，会返回null，并打印日志
     */
    @SuppressWarnings("unchecked")
    public static <S, T extends S, R> R castThen(S s, Function<T, R> then) {
        return loggingGetOrNull(() -> s != null ? then.apply((T) s) : null);
    }

    /**
     * 强转并执行，强转失败不打印日志
     *
     * @param s    原数据
     * @param then 强转后执行的动作
     * @param <S>  原类型（需要是目标类型的父类）
     * @param <T>  强转的目标类型
     * @param <R>  执行的结果
     * @return 如果强转失败，会返回null，不打印日志
     */
    @SuppressWarnings("unchecked")
    public static <S, T extends S, R> R castOkThen(S s, Function<T, R> then) {
        return silentGetOrNull(() -> s != null ? then.apply((T) s) : null);
    }



}
