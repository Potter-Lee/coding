package online.xswl.pl.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * True or False, it's a question
 *
 * @author PL
 */
public abstract class Tester {

    /* logic */

    /**
     * require all element match the test method
     * <pre>
     *     allMatch(Coding::notBlank, "1", "", "3"); // false
     *     allMatch(i -> i > 3, 1, 4, 5); // false
     *     allMatch(i -> i > 0, 1, 4, 5); // true
     * </pre>
     *
     * @throws IllegalArgumentException on any null parameter
     *
     * @see #allMatch(Predicate, Object[])
     *
     */
    public static <T> boolean allMatch(Predicate<T> test, Iterable<T> iterable) {
        if (test == null || iterable == null) {
            throw new IllegalArgumentException();
        }
        for (T one : iterable) {
            if (!test.test(one)) {
                return false;
            }
        }
        return true;
    }

    /**
     * require all element match the test method
     * <pre>
     *     allMatch(Coding::notBlank, "1", "", "3"); // false
     *     allMatch(i -> i > 3, 1, 4, 5); // false
     *     allMatch(i -> i > 0, 1, 4, 5); // true
     * </pre>
     *
     * @throws IllegalArgumentException on any null parameter
     */
    @SafeVarargs
    public static <T> boolean allMatch(Predicate<T> test, T... array) {
        return allMatch(test, Arrays.stream(array)::iterator);
    }


    @SafeVarargs
    public static <T, F> Predicate<T> allFieldMatch(Predicate<F> test, Function<T, F>... mappers) {
        return t -> allMatch(test, stream(mappers).map(mapper -> mapper.apply(t))::iterator);
    }

    public static <T> boolean anyMatch(Predicate<T> test, Iterable<T> iterable) {
        if (test == null || iterable == null) {
            throw new IllegalArgumentException();
        }
        for (T one : iterable) {
            if (test.test(one)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <T> boolean anyMatch(Predicate<T> test, T... array) {
        return anyMatch(test, Arrays.stream(array)::iterator);
    }

    public static <T extends Iterable<E>, E> Predicate<T> anyMatch(Predicate<E> test) {
        return collection -> anyMatch(test, collection);
    }


    public static <T> boolean nonMatch(Predicate<T> test, Iterable<T> iterable) {
        return !anyMatch(test, iterable);
    }

    @SafeVarargs
    public static <T> boolean nonMatch(Predicate<T> test, T... array) {
        return nonMatch(test, Arrays.stream(array)::iterator);
    }

    public static <T extends Iterable<E>, E> Predicate<T> nonMatch(Predicate<E> test) {
        return collection -> nonMatch(test, collection);
    }

    @SafeVarargs
    public static <T, F> Predicate<T> nonFieldMatch(Predicate<F> test, Function<T, F>... mappers) {
        return t -> nonMatch(test, stream(mappers).map(mapper -> mapper.apply(t))::iterator);
    }

    @SafeVarargs
    public static <T> boolean anyIs(T theOne, T... in) {
        if (in == null) {
            throw new IllegalArgumentException();
        }
        for (T one : in) {
            if (one == theOne) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <T> boolean anyMatch(T theOne, T... in) {
        if (in == null) {
            throw new IllegalArgumentException();
        }
        for (T one : in) {
            if (Objects.equals(one, theOne)) {
                return true;
            }
        }
        return false;
    }

    /* null */

    public static <T> boolean isNull(T o) {
        return o == null;
    }

    public static <T> boolean notNull(T o) {
        return o != null;
    }

    public static <T> boolean nonNull(T o) {
        return o != null;
    }

    /* CharSequence */

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean notEmpty(CharSequence s) {
        return s != null && s.length() > 0;
    }

    public static boolean hasText(CharSequence s) {
        if (s == null) {
            return false;
        }
        int strLen = s.length();
        if (strLen == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * "null" and "undefined" are "blank" on business since wrong input from client logic.
     */
    public static boolean isBlank(String s) {
        return !hasText(s) || "null".equalsIgnoreCase(s) || "undefined".equalsIgnoreCase(s);
    }

    /**
     * "null" and "undefined" are "blank" on business since wrong input from client logic.
     */
    public static boolean notBlank(String s) {
        return !isBlank(s);
    }


}
