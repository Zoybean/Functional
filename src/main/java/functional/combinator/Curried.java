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

package functional.combinator;

import java.util.function.*;

/**
 * Utility class with curried and currying-related combinators.
 * @author Zoey Hewll
 */
public final class Curried
{
    /**
     * Prevents construction of this class.
     */
    private Curried(){}

    /**
     * Curried form of Combinators::constant.
     */
    public static <A, B> Function<B, A> constant(A a)
    {
        return Curried.<A, B, A>curry(Combinators::constant).apply(a);
    }

    /**
     * Curried form of Combinators::compose.
     */
    public static <A, B, C> Function<Function<B, C>, Function<A, C>> compose(Function<A, B> ab)
    {
        return Curried.<Function<A, B>, Function<B, C>, Function<A, C>>curry(Combinators::compose).apply(ab);
    }

    /**
     * Curries the result of {@link Combinators#flip}.
     */
    public static <A, B, C> Function<B, Function<A, C>> flip(BiFunction<A, B, C> f)
    {
        return curry(Combinators.flip(f));
    }

    /**
     * Curried form of {@link Curried#flip(BiFunction)}.
     */
    public static <A, B, C> Function<B, Function<A, C>> flip(Function<A, Function<B, C>> f)
    {
        return flip(uncurry(f));
    }

    /**
     * Returns an equivalent function-to-function that takes one argument at a time.
     * @param f   the function to split
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the eventual return type
     * @return an equivalent function that takes one argument at a time
     */
    public static <A, B, C> Function<A, Function<B, C>> curry(BiFunction<A, B, C> f)
    {
        return (A a) -> (B b) -> f.apply(a, b);
    }

    /**
     * Returns an equivalent function with the first argument fixed.
     * @param f   the function to partially apply
     * @param a   the value of the argument to fix
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the eventual return type
     * @return an equivalent function that takes one fewer argument
     */
    public static <A, B, C> Function<B, C> curryWith(BiFunction<A, B, C> f, A a)
    {
        return curry(f).apply(a);
    }

    /**
     * Returns an equivalent function that takes both arguments at once.
     * @param f   the function to join
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the eventual return type
     * @return an equivalent function that takes both arguments at once
     */
    public static <A, B, C> BiFunction<A, B, C> uncurry(Function<A, Function<B, C>> f)
    {
        return (A a, B b) -> f.apply(a).apply(b);
    }

    /**
     * Returns an equivalent function-to-consumer that takes one argument at a time.
     * @param f   the consumer to split
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @return an equivalent consumer that takes one argument at a time
     */
    public static <A, B> Function<A, Consumer<B>> curryConsumer(BiConsumer<A, B> f)
    {
        return (A a) -> (B b) -> f.accept(a, b);
    }

    /**
     * Returns an equivalent consumer with the first argument fixed.
     * @param f   the function to partially apply
     * @param a   the value of the argument to fix
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @return an equivalent consumer that takes one fewer argument
     */
    public static <A, B> Consumer<B> curryConsumerWith(BiConsumer<A, B> f, A a)
    {
        return curryConsumer(f).apply(a);
    }

    /**
     * Returns an equivalent consumer that takes both arguments at once.
     * @param f   the consumer to join
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @return an equivalent consumer that takes both arguments at once
     */
    public static <A, B> BiConsumer<A, B> uncurryConsumer(Function<A, Consumer<B>> f)
    {
        return (A a, B b) -> f.apply(a).accept(b);
    }
}
