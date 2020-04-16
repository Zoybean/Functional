/*
 * Copyright 2019 Zoey Hewll
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

import functional.algebraic.utils.Voids;
import functional.combinator.Combinators;
import functional.throwing.ThrowingConsumer;
import functional.throwing.ThrowingFunction;
import functional.throwing.ThrowingRunnable;
import functional.throwing.ThrowingSupplier;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Result is a container representing the result of an operation that may throw an exception;
 * It provides pattern matching to handle control flow when extracting the data,
 * and operations to mutate or sequence results.
 * This allows checked exceptions to be encapsulated in functional code,
 * and exception handling in the form of pattern matching.
 *
 * @param <V> The value alternative type.
 * @param <E> The error alternative type.
 * @author Zoey Hewll
 * @author Eleanor McMurtry
 */
public class Result<V, E extends Exception> implements ThrowingSupplier<V, E>
{
    /**
     * The Either type used internally to hold the alternative values.
     */
    private final Either<E, V> either;

    /**
     * Constructor.
     * Makes a Result from the given Either.
     *
     * @param either The Either value used to maintain internal structure.
     */
    private Result(Either<E, V> either)
    {
        this.either = either;
    }

    /**
     * Performs an operation which may throw a checked exception, and encapsulates the result.<br>
     * If the operation succeeds, the Result will contain the returned value.<br>
     * If the operation fails, the Result will contain the thrown checked exception.
     * <p>
     * <p>Conceptually the inverse of {@link #get}:<br>
     * {@code r <==> of(r::get)}<br>
     * {@code f.get() <==> of(f).get()}<br>
     *
     * @param f   the operation to perform
     * @param <E> the type of checked exception which may be thrown
     * @param <V> the type of value which may be returned
     * @return A Result representing the outcome of the operation.
     */
    public static <V, E extends Exception> Result<V, E> of(ThrowingSupplier<? extends V, ? extends E> f)
    {
        try
        {
            return value(f.get());
        }
        catch (RuntimeException e)
        {
            // This ensures that only checked exceptions are caught by the following clause.
            throw e;
        }
        catch (Exception e)
        {
            @SuppressWarnings("unchecked") final E ex = (E) e;
            return error(ex);
        }
    }

    /**
     * Performs an operation which may throw a checked exception, and encapsulates the result.
     * If the operation succeeds, the Result will contain nothing (Void).
     * If the operation fails, the Result will contain the thrown checked exception.
     * <p>
     * <p>Conceptually the inverse of {@link #get}.
     * {@code r <==> of(r::get)}<br>
     * {@code f.run() <~~> of(f).get()}<br>
     *
     * @param v   the operation to perform
     * @param <E> the type of checked exception which may be thrown
     * @return A Result representing the outcome of the operation.
     */
    public static <E extends Exception> Result<Void, E> of(ThrowingRunnable<? extends E> v)
    {
        return of(Voids.convertUnsafe(v));
    }

    /**
     * Perform an operation which may throw a runtime exception, and encapsulate the result.
     * If the operation succeeds, the Result will contain the returned value.
     * If the operation fails, the Result will contain the thrown runtime exception.
     * <p>
     * <p>Conceptually the inverse of {@link #get}.
     * {@code r <==> ofRuntime(r.get())}<br>
     * {@code f.get() <==> ofRuntime(f).get()}<br>
     *
     * @param v   the operation to perform
     * @param <V> the type of value which may be returned
     * @return A Result representing the outcome of the operation.
     */
    public static <V> Result<V, RuntimeException> ofRuntime(Supplier<? extends V> v)
    {
        try
        {
            return value(v.get());
        }
        catch (RuntimeException e)
        {
            return error(e);
        }
    }

    /**
     * Perform an operation which may throw a runtime exception, and encapsulate the result.
     * If the operation succeeds, the Result will contain nothing (Void).
     * If the operation fails, the Result will contain the thrown runtime exception.
     * <p>
     * <p>Conceptually the inverse of {@link #get}.
     * {@code r <==> ofRuntime(r.get())}<br>
     * {@code f() <==> ofRuntime(f).get()}<br>
     *
     * @param v the operation to perform
     * @return A Result representing the outcome of the operation.
     */
    public static Result<Void, RuntimeException> ofRuntime(Runnable v)
    {
        return ofRuntime(Voids.convert(v));
    }

    /**
     * Convert an operation that may throw a checked exception into a supplier that returns a result.
     *
     * @param v   The supplier to convert
     * @param <E> The checked exception type
     * @param <V> The operation's return type
     * @return The converted operation, which returns a result instead of throwing an exception.
     */
    public static <V, E extends Exception> Supplier<Result<V, E>> convertSupplier(ThrowingSupplier<? extends V, ? extends E> v)
    {
        return () -> of(v);
    }

    /**
     * Convert an operation that may throw a checked exception into a supplier that returns a result.
     *
     * @param v   The operation to convert
     * @param <E> The checked exception type
     * @return The converted operation, which returns a result instead of throwing an exception.
     */
    public static <E extends Exception> Supplier<Result<Void, E>> convertRunnable(ThrowingRunnable<? extends E> v)
    {
        return () -> of(v);
    }

    /**
     * Convert a function that may throw a checked exception into a function that returns a result.
     *
     * @param v   The function to convert
     * @param <T> The function's parameter type
     * @param <V> The function's return type
     * @param <E> The checked exception type
     * @return The converted function, which returns a result instead of throwing an exception.
     */
    public static <T, V, E extends Exception> Function<T, Result<V, E>> convertFunction(ThrowingFunction<? super T, ? extends V, ? extends E> v)
    {
        return (T t) -> of(() -> v.apply(t));
    }

    /**
     * Convert an operation that may throw a checked exception into a function that returns a result.
     *
     * @param v   The operation to convert
     * @param <T> The consumer's parameter type
     * @param <E> The checked exception type
     * @return The converted function, which returns a result instead of throwing an exception.
     */
    public static <T, E extends Exception> Function<T, Result<Void, E>> convertConsumer(ThrowingConsumer<? super T, ? extends E> v)
    {
        return (T t) -> of(() -> v.accept(t));
    }

    /**
     * Transposes a Maybe of a Result into a Result of a Maybe.
     * @param m   A maybe of a result.
     * @param <E> The error alternative type.
     * @param <V> The value alternative type.
     * @return
     */
    public static <V, E extends Exception> Result<Option<V>, E> transpose(
            Option<? extends Result<? extends V, ? extends E>> m)
    {
        return m.matchThen(
                (Result<? extends V, ? extends E> r) -> r.matchThen(
                        (V v) -> value(Option.just(v)),
                        (E e) -> error(e)
                ),
                () -> value(Option.nothing())
        );
    }

    /**
     * Transposes a Result of a Maybe into a Maybe of a Result.
     * @param r   A result of a maybe.
     * @param <E> The error alternative type.
     * @param <V> The value alternative type.
     * @return
     */
    public static <V, E extends Exception> Option<Result<V, E>> transpose(Result<? extends Option<? extends V>, ? extends E> r)
    {
        return r.matchThen(
                (Option<? extends V> m) -> m.matchThen(
                        (V v) -> Option.just(value(v)),
                        () -> Option.nothing()
                ),
                (E e) -> Option.just(error(e))
        );
    }

    /**
     * Converts a nested Result (aka Result of a Result) into a single Result.
     *
     * @param r   The result to join
     * @param <E> the error alternative type.
     * @param <V> the value alternative type.
     * @return A unified Result
     */
    static <V, E extends Exception> Result<V, E> join(Result<? extends Result<? extends V, ? extends E>, ? extends E> r)
    {
        return cast(r.matchThen(
                Combinators::id,
                Result::error
        ));
    }

    /**
     * Returns a Result containing the provided exception.
     *
     * @param e   The exception to contain
     * @param <E> The type of the contained exception
     * @param <V> The unused value type
     * @return A Result containing the provided exception.
     */
    public static <V, E extends Exception> Result<V, E> error(E e)
    {
        return new Result<>(Either.left(e));
    }

    /**
     * Returns a Result containing the provided value.
     *
     * @param v   The value to contain
     * @param <E> The unused error type
     * @param <V> The type of the contained value
     * @return A Result containing the provided exception.
     */
    public static <V, E extends Exception> Result<V, E> value(V v)
    {
        return new Result<>(Either.right(v));
    }

    /**
     * Returns an equivalent Result with more generic type parameters.
     *
     * @param r   The Result to convert
     * @param <E> The new error type
     * @param <V> The new value type
     * @return An equivalent Either with more generic type parameters.
     */
    static <V, E extends Exception> Result<V, E> cast(Result<? extends V, ? extends E> r)
    {
        return new Result<>(Either.cast(r.either));
    }

    /**
     * Match on the result, applying the function pertaining to the contained type, and returning the result.
     * Both functions must return the same type.
     *
     * @param vf  The function to apply to the result if it is a {@link #value}.
     * @param ef  The function to apply to the result if it is an {@link #error}.
     * @param <T> The return type of the functions.
     * @return The value returned by the matched function.
     */
    public <T> T matchThen(Function<? super V, ? extends T> vf, Function<? super E, ? extends T> ef)
    {
        return either.matchThen(ef, vf);
    }

    /**
     * Match on the result, performing the operation pertaining to the contained type.
     *
     * @param vf The operation to perform if the result is a {@link #value}.
     * @param ef The operation to perform if the result is an {@link #error}.
     */
    public void match(Consumer<? super V> vf, Consumer<? super E> ef)
    {
        either.match(ef, vf);
    }

    /**
     * Applies a function that can take any Object to both the error and the result.
     * Useful for e.g. Systemm.out::println.
     *
     * @param f the function to apply
     */
    public void collapse(Consumer<Object> f) {
        either.collapse(f);
    }

    /**
     * Applies a function that can take any Object to both the error and the result.
     * Useful for e.g. Systemm.out::println.
     *
     * @param f   the function to apply
     * @param <T> the function's return type
     * @return the result of applying the function to the contained value
     */
    public <T> T collapseThen(Function<Object, ? extends T> f)
    {
        return either.collapseThen(f);
    }

    /**
     * Returns the value in this Result, or a default if there is an error.
     *
     * @param defaultValue the default value
     * @return the value or default
     */
    public V orElse(V defaultValue) {
        return either.matchThen(
            __ -> defaultValue,
            val -> val
        );
    }

    /**
     * Produce a Result of another type from the value in this result, or propagate the error.
     * Equivalent to haskell's Monadic bind {@code (this >>=)}.
     *
     * @param f the function to bind
     * @param <T> The function's return type
     * @return the result of the bound computation
     */
    public <T> Result<T, E> andThen(Function<? super V, ? extends Result<? extends T, ? extends E>> f)
    {
        return join(map(f));
    }

    /**
     * Equivalent to haskell's Monadic bind {@code (this >>=)}, applied to a throwing function.
     *
     * @param f the throwing function to bind
     * @param <T> The function's return type
     * @return the result of the bound computation
     */
    public <T> Result<T, E> andThenT(ThrowingFunction<? super V, ? extends T, ? extends E> f)
    {
        return andThen(convertFunction(f));
    }

    /**
     * Equivalent to haskell's Monadic bind {@code (this >>=)}, applied to a throwing consumer.
     *
     * @param f
     * @return
     */
    public Result<Void, E> andDoT(ThrowingConsumer<? super V, ? extends E> f)
    {
        return andThen(convertConsumer(f));
    }

    /**
     * Produce a new Result, ignoring the value in this one and propagating the error.
     * Equivalent to haskell {@code (this *>)}.
     *
     * @param f
     * @return
     * @param <T> The supplier's return type
     */
    public <T> Result<T, E> and(Supplier<? extends Result<? extends T, ? extends E>> f)
    {
        return andThen((__) -> f.get());
    }

    /**
     * Equivalent to haskell {@code (this *>)}.
     *
     * @param f
     * @return
     * @param <T> The supplier's return type
     */
    public <T> Result<T, E> andT(ThrowingSupplier<? extends T, ? extends E> f)
    {
        return and(convertSupplier(f));
    }

    /**
     * Perform the given action and use its Result's value.
     * returning {@code this} if there was no error, otherwise the resulting error.
     * <p>
     * Equivalent to haskell {@code (this *>)}
     *
     * @param f
     * @return
     */
    public Result<Void, E> andT(ThrowingRunnable<? extends E> f)
    {
        return and(convertRunnable(f));
    }

    /**
     * Equivalent to haskell {@code (this <*)}.
     *
     * @param r
     * @return
     * @param <F> The supplier's error type
     */
    public <F extends Exception> Result<V, F> or(Supplier<? extends Result<? extends V, ? extends F>> r)
    {
        return matchThen(
                (V v) -> value(v),
                (E e) -> cast(r.get())
        );
    }

    /**
     * @param r
     * @param <F>
     * @return
     */
    public <F extends Exception> Result<V, F> or(Result<? extends V, ? extends F> r) {
        return or(() -> r);
    }

    /**
     * Equivalent to haskell {@code (this <*)}.
     *
     * @param f
     * @return
     * @param <F> The supplier's error type
     */
    public <F extends Exception> Result<V, F> orT(ThrowingSupplier<? extends V, ? extends F> f)
    {
        return or(convertSupplier(f));
    }

    /**
     * Perform an operation on the value inside this result, or do nothing if there is an error.
     * @param op the operation
     * @return the unaltered Result
     */
    public Result<V, E> ifOk(Consumer<? super V> op) {
        match(op, Combinators::noop);
        return this;
    }

    /**
     * Perform an operation on the error inside this result, or do nothing if there is no error.
     * @param op the operation
     * @return the unaltered Result
     */
    public Result<V, E> ifErr(Consumer<? super E> op) {
        match(Combinators::noop, op);
        return this;
    }


    /**
     * Apply the given function to the contained value and discard the Result's value,
     * returning {@code this} if there was no error, otherwise the resulting error.
     * <p>
     * Equivalent to haskell {@code (this <*) . (this >>=)}
     *
     * @param f the function to apply
     * @return
     */
    public Result<V, E> peek(Function<? super V, ? extends Result<Void, ? extends E>> f)
    {
        return andThen((V v) -> f.apply(v).set(v));
    }

    /**
     * Apply the given consumer to the contained value and discard the returned value,
     * returning {@code this} if there was no error, otherwise the resulting error.

     * @param f the consumer to apply
     * @return
     */
    public Result<V, E> peekT(ThrowingConsumer<? super V, ? extends E> f)
    {
        return peek(convertConsumer(f));
    }

    /**
     * Equivalent to haskell {@code (this *>)}
     *
     * @param value
     * @return
     * @param <T> The result's value type
     */
    public <T> Result<T, E> and(Result<? extends T, ? extends E> value)
    {
        return and(() -> value);
    }

    /**
     * Equivalent to haskell {@code (this *>) . pure}
     *
     * @param value
     * @return
     * @param <T> The value type
     */
    public <T> Result<T, E> set(T value)
    {
        return and(value(value));
    }

    /**
     * Apply the function to the contained value, if it is present, and return the modified Result.
     *
     * @param f   The function to apply to the contained value
     * @param <T> The return type of the function
     * @return The modified Result.
     */
    public <T> Result<T, E> map(Function<? super V, ? extends T> f)
    {
        return matchThen(
                (V v) -> value(f.apply(v)),
                (E e) -> error(e)
        );
    }

    /**
     * Maps a function of errors to the error inside this Result, or does nothing if there is none.
     * @param f the function to apply
     * @param <F> the resulting error type
     * @return the mapped Result
     */
    public <F extends Exception> Result<V, F> mapError(Function<? super E, ? extends F> f)
    {
        return matchThen(
                (V v) -> value(v),
                (E e) -> error(f.apply(e))
        );
    }

    /**
     * Applies a function to turn an error into a value if there is an error, and returns the resulting value
     * or the value inside this Result.
     * @param f the function
     * @return
     */
    public V convertError(Function<? super E, ? extends V> f)
    {
        return matchThen(Combinators::id, f);
    }

    /**
     * Apply the function to the contained value and the supplied value, if present, and return the modified Result.
     *
     * @param f The bifunction to apply to the contained value
     * @return The modified Result.
     */
    public <U,R> Result<R, E> bimap(Result<? extends U, ? extends E> r, BiFunction<? super V, ? super U, ? extends R> f)
    {
        return andThen(
                (V v) -> r.map((Function<U, R>)
                            (U u) -> f.apply(v, u)));
    }

    /**
     * Unsafely unwraps the result into a returned value or thrown exception.
     * If the result is a value, return it.<br>
     * If it is an exception, throw it.<br>
     *
     * <p>Conceptually the inverse of {@link #of} and {@link #ofRuntime}:<br>
     * {@code r <==> of(r.get())}<br>
     * {@code f() <==> of(f()).get()}<br>
     *
     * @return the contained value
     * @throws E the contained exception
     */
    public V get() throws E
    {
        return either.unsafeMatchThen(
                Combinators::toss,
                Combinators::id
        );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Result.class, either.hashCode());
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Result)
        {
            Result<?,?> r = (Result<?,?>) o;
            return either.equals(r.either);
        }
        return false;
    }

    public String toString()
    {
        return matchThen(
                v -> "Result.value(" + v + ')',
                e -> "Result.error(" + e + ')'
        );
    }

}
