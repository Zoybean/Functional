/*
 * Copyright 2019, 2020 Zoey Hewll
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package functional.algebraic;

import functional.combinator.Combinators;
import functional.throwing.ThrowingConsumer;
import functional.throwing.ThrowingFunction;
import functional.throwing.ThrowingRunnable;
import functional.throwing.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Type Option represents an optional value: every Option is either Some and contains a value, or None, and does not.
 * Option types have a number of uses:
 * <p><ul>
 * <li>    Initial values
 * <li>    Return values for functions that are not defined over their entire input range (partial functions)
 * <li>    Return value for otherwise reporting simple errors, where None is returned on error
 * <li>    Optional fields
 * <li>    Optional function arguments
 * <li>    Nullable values
 * </ul><p>
 * Options provide a mechanism for pattern matching to query the presence of a value and take action,
 * always accounting for the None case.
 *
 * @param <V> The type of the optional value
 * @author Zoey Hewll
 * @author Eleanor McMurtry
 */
public abstract class Option<V> implements Iterable<V>
{
    /**
     * Effectively final, singleton instance.
     */
    private static None<?> NONE;

    /**
     * Private constructor to seal the type.
     */
    private Option() {}

    /**
     * Decide control flow based on the structure of this,
     * returning a result or throwing a checked exception.
     *
     * @see #matchThen
     *
     * @param some the operation to perform on the contained value if there is one
     * @param none the operation to perform if there is no contained value
     * @param <T>  the type of the value returned by the supplied operations
     * @param <E>  the exception type which may be thrown by one or both operations
     * @return the result of the matched operation
     * @throws E the exception thrown by the supplied operations
     */
    public abstract <T, E extends Exception> T unsafeMatchThen(ThrowingFunction<? super V, ? extends T, ? extends E> some, ThrowingSupplier<? extends T, ? extends E> none) throws E;

    /**
     * Decide control flow based on the structure of this,
     * optionally throwing a checked exception.
     *
     * @see #match
     *
     * @param some the operation to perform on the contained value if there is one
     * @param none the operation to perform if there is no contained value
     * @param <E>  the exception type which may be thrown by one or both operations
     * @throws E the exception thrown by the supplied operations
     */
    public abstract <E extends Exception> void unsafeMatch(ThrowingConsumer<? super V, ? extends E> some, ThrowingRunnable<? extends E> none) throws E;

    /**
     * Decide control flow by opening the structure of this,
     * returning the result of the chosen branch.
     *
     * @param some the operation to perform on the contained value if there is one
     * @param none the operation to perform if there is no contained value
     * @param <T>  the type of the value returned by the supplied operations
     * @return the result of the matched operation
     */
    public <T> T matchThen(Function<? super V, ? extends T> some, Supplier<? extends T> none)
    {
        return unsafeMatchThen(
                some::apply,
                none::get);
    }

    /**
     * Decide control flow based on the structure of this.
     *
     * @param some the operation to perform on the contained value if there is one
     * @param none the operation to perform if there is no contained value
     */
    public void match(Consumer<? super V> some, Runnable none)
    {
        unsafeMatch(
                some::accept,
                none::run);
    }

    /**
     * Applies the mapping function if there is a contained value, returning the result in an Option.
     * This is guaranteed to produce an Option of the same structure.
     *
     * @param f   the function to apply to the contained value
     * @param <T> the type of the function's return value
     * @return the modified Option
     */
    public abstract <T> Option<T> map(Function<? super V, ? extends T> f);

    public <T> T mapOr(Function<? super V, ? extends T> f, T value)
    {
        return mapOrElse(f, () -> value);
    }

    public <T> T mapOrElse(Function<? super V, ? extends T> f, Supplier<? extends T> s)
    {
        // equivalent to map(f).unwrapOrElse(s), but for type reasons java needs it expanded
        return map(f).matchThen(
                Combinators::id,
                s
        );
    }

    /**
     * Returns None if this is None, otherwise returns o.
     * @param o the other Option
     * @return the provided Option if both are present
     */
    public abstract Option<V> and(Option<? extends V> o);

    /**
     * Returns this if it contains a value, otherwise returns o.
     * @param o the other Option
     * @return the first present value or none
     */
    public abstract Option<V> or(Option<? extends V> o);


    /**
     * Returns this if it is a Some, otherwise computes an option from the given Supplier.
     * @param s the supplier to compute the other Option
     * @return the first present value or none
     */
    public Option<V> orElse(Supplier<? extends Option<? extends V>> s)
    {
        return cast(matchThen(
                Option::some,
                s
        ));
    }


    /**
     * Returns Some if exactly one of this, o is Some, otherwise returns None.
     * @param o the other Option
     * @return the unique present value, or None
     */
    public abstract Option<V> xor(Option<? extends V> o);

    /**
     * Returns None if this is None, otherwise calls f with the wrapped value and returns the result.
     *
     * Some languages call this operation flatmap.
     *
     * @param f   the function to apply to the contained value
     * @param <T> the type of the function's optional return value
     * @return the returned Option, or None if this is None
     */
    public abstract <T> Option<T> andThen(Function<? super V, ? extends Option<? extends T>> f);

    /**
     * Returns None if this is None, otherwise calls predicate with the wrapped value and returns:
     * <p><ul>
     * <li>    Some(t) if predicate returns true (where t is the wrapped value), and
     * <li>    None if predicate returns false.
     * </ul><p>
     * @param pred the predicate to test against any contained value
     * @return a Some if this is Some and the value passes the predicate, otherwise None
     */
    public Option<V> filter(Predicate<V> pred)
    {
        return andThen(
                v -> pred.test(v)
                        ? some(v)
                        : none());
    }

    /**
     * Returns the contained value, if it exists, otherwise raise an exception.
     *
     * @return The contained value, if it exists
     * @throws NoSuchElementException if nothing is contained
     */
    public V unwrap() throws NoSuchElementException
    {
        return expect("called `Option::unwrap()` on a `None` value");
    }

    /**
     * Returns the contained value, or the supplied default if it does not exist.
     *
     * @param value the default value to use
     * @return the contained value, or the supplied default if it does not exist
     */
    public V unwrapOr(V value)
    {
        return unwrapOrElse(() -> value);
    }

    /**
     * Returns the contained value, if it exists, otherwise raise an exception with the provided message.
     *
     * @param message the exception message
     * @return The contained value, if it exists
     * @throws NoSuchElementException if nothing is contained
     */
    public V expect(String message) throws NoSuchElementException
    {
        return expect(new NoSuchElementException(message));
    }

    /**
     * Returns the contained value, if it exists, otherwise raise the provided exception.
     *
     * @param <T> the type of the exception
     * @param t the exception to throw
     * @return the contained value, if it exists
     * @throws T if nothing is contained
     */
    public abstract <T extends Throwable> V expect(T t) throws T;

    /**
     * Returns the contained value, or computes one if it does not exist.
     *
     * @param supplier the supplier of the value to use
     * @return the contained value, or the computed value if it does not exist
     */
    public V unwrapOrElse(Supplier<? extends V> supplier)
    {
        return matchThen(
                Combinators::id,
                supplier
        );
    }

    /**
     * Returns true if this is a Some value.
     * @return true if this is a Some value, false otherwise
     */
    public boolean isSome()
    {
        return matchThen(
                (V __) -> true,
                () -> false);
    }

    /**
     * Returns true if this is a None value.
     * @return true if this is a None value, false otherwise
     */
    public boolean isNone()
    {
        return matchThen(
                (V __) -> false,
                () -> true);
    }

    @NotNull
    @Override
    public Iterator<V> iterator()
    {
        return new OptionIterator();
    }

    /**
     * Consumes the value inside this Option, if there is one.
     * @param consumer the consuming operation
     */
    public void consume(Consumer<? super V> consumer)
    {
        match(consumer, Combinators::none);
    }

    /**
     * Converts a nullable value into an equivalent Option, returning None if the value is null, and some(value) otherwise.
     *
     * @param value the value to wrap
     * @return none() if the value is null, and some(value) otherwise
     * @param <V> The type of the optional value
     */
    public static <V> Option<V> of(@Nullable V value)
    {
        return value == null
                ? none()
                : some(value);
    }

    /**
     * Turns an Optional into an Option, turning Empty into None, and a present value into some(value).
     *
     * @param value the optional to transform
     * @return an Option mirroring the Optional parameter
     * @param <V> The type of the optional value
     */
    public static <V> Option<V> of(Optional<? extends V> value)
    {
        return cast(value.map(Option::some).orElse(none()));
    }

    /**
     * Wraps the value in an Option.
     *
     * @param value The value to wrap
     * @param <V>   The type of the contained value
     * @return The value wrapped in an Option
     */
    public static <V> Option<V> some(V value)
    {
        return new Some<>(value);
    }

    /**
     * Returns the singleton None instance.
     *
     * @param <V> The type of the non-existent contained value
     * @return The singleton None instance
     */
    public static <V> Option<V> none()
    {
        if (NONE == null)
        {
            NONE = new None<>();
        }
        @SuppressWarnings("unchecked") None<V> none = (None<V>) NONE;
        return none;
    }

    /**
     * Upcast the container by upcasting the contained type.
     *
     * @param o   The Option to cast
     * @param <V> The type to cast to
     * @return an Option of the upcast type
     */
    static <V> Option<V> cast(Option<? extends V> o)
    {
        return o.matchThen(
                Option::some,
                Option::none
        );
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public abstract String toString();

    /**
     * The class representing an absent value.
     *
     * @param <V> unused
     */
    private static class None<V> extends Option<V>
    {
        @Override
        public boolean equals(Object o)
        {
            return o instanceof Option.None;
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(None.class);
        }

        @Override
        public String toString()
        {
            return "Option.none()";
        }

        @Override
        public <T, E extends Exception> T unsafeMatchThen(ThrowingFunction<? super V, ? extends T, ? extends E> some, ThrowingSupplier<? extends T, ? extends E> none) throws E
        {
            return none.get();
        }

        @Override
        public <E extends Exception> void unsafeMatch(ThrowingConsumer<? super V, ? extends E> some, ThrowingRunnable<? extends E> none) throws E
        {
            none.run();
        }

        @Override
        public <T> Option<T> map(Function<? super V, ? extends T> f)
        {
            return none();
        }

        @Override
        public Option<V> and(Option<? extends V> o)
        {
            return this;
        }

        @Override
        public Option<V> or(Option<? extends V> o)
        {
            return cast(o);
        }

        @Override
        public Option<V> xor(Option<? extends V> o) {
            return cast(o);
        }

        @Override
        public <T> Option<T> andThen(Function<? super V, ? extends Option<? extends T>> f)
        {
            return none();
        }

        @Override
        public <T extends Throwable> V expect(T t) throws T
        {
            throw t;
        }

    }

    /**
     * The class representing a present value.
     *
     * @param <V> The type of the contained value.
     */
    private static class Some<V> extends Option<V>
    {
        /**
         * The contained value.
         */
        final V value;

        /**
         * Construct an Option with the provided value.
         *
         * @param value The value to wrap.
         */
        Some(V value)
        {
            this.value = value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Option.Some)
            {
                Some<?> j = (Some<?>) o;
                return Objects.equals(value, j.value);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(Some.class, value);
        }

        @Override
        public String toString()
        {
            return "Option.some(" + value + ')';
        }

        @Override
        public <T, E extends Exception> T unsafeMatchThen(ThrowingFunction<? super V, ? extends T, ? extends E> some, ThrowingSupplier<? extends T, ? extends E> none) throws E
        {
            return some.apply(value);
        }

        @Override
        public <E extends Exception> void unsafeMatch(ThrowingConsumer<? super V, ? extends E> some, ThrowingRunnable<? extends E> none) throws E
        {
            some.accept(value);
        }

        @Override
        public <T> Option<T> map(Function<? super V, ? extends T> f)
        {
            return some(f.apply(value));
        }

        @Override
        public Option<V> and(Option<? extends V> o)
        {
            return cast(o);
        }

        @Override
        public Option<V> or(Option<? extends V> o)
        {
            return this;
        }

        @Override
        public Option<V> xor(Option<? extends V> o) {
            return o.matchThen(
                    v -> none(),
                    () -> this
            );
        }

        @Override
        public <T> Option<T> andThen(Function<? super V, ? extends Option<? extends T>> f)
        {
            return cast(f.apply(value));
        }

        @Override
        public <T extends Throwable> V expect(T t)
        {
            return value;
        }
    }

    /**
     * Iterator for Option values.
     */
    private class OptionIterator implements Iterator<V>
    {
        /**
         * The next value if there is one.
         */
        Option<V> next;

        /**
         * Construct an iterator over the enclosing Option.
         */
        OptionIterator()
        {
            next = Option.this;
        }

        @Override
        public boolean hasNext()
        {
            return next.isSome();
        }

        @Override
        public V next() throws NoSuchElementException
        {
            V val = next.unwrap();
            next = none();
            return val;
        }
    }
}
