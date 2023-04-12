package online.xswl.pl.function;

import online.xswl.pl.utils.Tester;

import java.util.function.Function;
import java.util.function.Predicate;



/**
 * quick draw functions
 *
 * @author PL
 */
public abstract class Fn {


    public static <T extends Iterable<E>, E> Predicate<T> allMatch(Predicate<E> test) {
        return collection -> Tester.allMatch(test, collection);
    }

    @SafeVarargs
    public static <T, F> Predicate<T> anyFieldMatch(Predicate<F> test, Function<T, F>... mappers) {
        return t -> Tester.anyMatch(test, stream(mappers).map(mapper -> mapper.apply(t))::iterator);
    }
}
