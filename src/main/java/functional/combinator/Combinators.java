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

package functional.combinator;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class with common combinators.
 * @author Zoey Hewll
 */
public final class Combinators
{
    /**
     * Prevents construction of this class.
     */
    private Combinators(){}

    /**
     * Returns the argument.
     * @param a   the value to return
     * @param <A> the type of the value to return
     * @return the argument.
     */
    public static <A> A id(A a)
    {
        return a;
    }

    /**
     * Returns the first argument and discards the second.
     * @param a   the value to return
     * @param <A> the type of the value to return
     * @param __  the unused argument
     * @param <B> the type of the unused argument
     * @return the first argument.
     */
    public static <A, B> A constant(A a, B __)
    {
        return a;
    }

    /**
     * Discards the argument. No-op.
     * @param __  the unused argument
     * @param <A> the type of the unused argument
     */
    public static <A> void noop(A __) {}

    /**
     * Does nothing to nothing. No-op.
     */
    public static void none() {}

    /**
     * Composes two functions, such that the second is applied to the result of the first.
     * @param ab  a function from A to B
     * @param bc  a function from B to C
     * @param <A> the type of the input to the first function
     * @param <B> the intermediate type
     * @param <C> the type of the output from the second function
     * @return a function that applies the two given functions in sequence
     */
    public static <A, B, C> Function<A, C> compose(Function<A, B> ab, Function<B, C> bc)
    {
        return (A a) -> bc.apply(ab.apply(a));
    }

    /**
     * Reverses the parameters of a function that has two parameters.
     * @param f   the function to modify
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the result
     * @return A function that has its parameters reversed
     */
    public static <A, B, C> BiFunction<B, A, C> flip(BiFunction<A, B, C> f)
    {
        return (B b, A a) -> f.apply(a, b);
    }

    /**
     * Modifies the function's type, such that the parameter type is more specific and/or the return type is less specific.
     * @param f   the function to modify
     * @param <A> the new parameter type, a subclass of the original parameter type
     * @param <B> the new return type, a superclass of the original return type
     * @return an equivalent function that has different bounds on its types
     */
    public static <A, B> Function<A, B> cast(Function<? super A, ? extends B> f)
    {
        return f::apply;
    }

    /**
     * Throws the given Throwable.
     * @param t   the throwable to throw
     * @param <T> the type of the throwable
     * @param <A> the unused return type
     * @return never
     * @throws T unconditionally
     */
    public static <T extends Throwable, A> A toss(T t) throws T
    {
        throw t;
    }
}
