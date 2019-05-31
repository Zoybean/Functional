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

package functional.throwing;

import java.util.Objects;

/**
 * Represents a fallible function that accepts one argument and produces a result.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of exceptions thrown by the function
 * @author Zoey Hewll
 * @see java.util.function.BiFunction
 */

public interface ThrowingBiFunction<T, U, R, E extends Exception>
{
    /**
     * Applies this function to the given argument.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws E if the function throws an exception
     */
    R apply(T t, U u) throws E;

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> ThrowingBiFunction<T, U, V, E> andThen(ThrowingFunction<? super R, ? extends V, ? extends E> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
