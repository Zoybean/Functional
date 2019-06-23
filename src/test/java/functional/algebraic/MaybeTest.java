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

import functional.throwing.ThrowingConsumer;
import functional.throwing.ThrowingFunction;
import functional.throwing.ThrowingRunnable;
import functional.throwing.ThrowingSupplier;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static functional.algebraic.Maybe.*;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class MaybeTest
{
    public static Error error = new AssertionError("Wrong branch taken");
    public static IOException expected = new IOException("This is the right branch");

    @Test
    public void bindTestJust()
    {
        int x = 0;
        Function<Integer, Maybe<Integer>> f = i -> just(i + 1);

        assertEquals(f.apply(x), just(x).andThen(f));
        assertTrue(just(x).andThen(f).isJust());
    }

    @Test
    public void bindTestJustNothing()
    {
        int x = 0;
        Function<Object, Maybe<Object>> f = i -> nothing();

        assertEquals(f.apply(x), just(x).andThen(f));
        assertEquals(nothing(), just(x).andThen(f));
    }

    @Test
    public void bindTestNothing()
    {
        Function<Integer, Maybe<Integer>> g = i -> just(i + 1);

        Maybe<Integer> nothing = Maybe.nothing();
        assertEquals(nothing(), nothing.andThen(g));
    }

    @Test
    public void fromMaybeTest()
    {
        assertTrue(Maybe.<Boolean>just(true).orElse(false));
        assertFalse(Maybe.<Boolean>nothing().orElse(false));
    }

    @Test
    public void getTestJust()
    {
        assertTrue(just(true).get());
    }

    @Test(expected = IllegalStateException.class)
    public void getTestNothing()
    {
        nothing().get();
    }

    @Test
    public void orElseTest()
    {
        assertEquals(
                true,
                just(true).orElse(false));
        assertEquals(
                false,
                nothing().orElse(false));
    }

    @Test
    public void consumeTest()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        just(true).consume(v -> changed.set(v));
        nothing().consume(v -> toss(error));

        assertTrue(changed.get());
    }

    @Test
    public void isJustTest()
    {
        assertTrue(just(0).isJust());
        assertFalse(nothing().isJust());
    }

    @Test
    public void mapTest()
    {
        Function<Integer, Integer> f = x -> x + 1;

        int x = 0;
        Maybe<Integer> nothing = nothing();

        assertEquals(
                just(f.apply(x)),
                just(x).map(f));

        assertEquals(
                nothing,
                nothing.map(f));
    }

    @Test
    public void matchTestProducingJust()
    {
        Function<Integer, Boolean> some= i -> true;
        Supplier<Boolean> none = () -> false;

        assertTrue(just(0).matchThen(some, none));
    }

    @Test
    public void matchTestProducingNothing()
    {
        Function<Integer, Boolean> some= i -> true;
        Supplier<Boolean> none = () -> false;
        Maybe<Integer> nothing = nothing();

        assertFalse(nothing.matchThen(some, none));
    }

    @Test
    public void matchTestVoidJust()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        Consumer<Boolean> some = changed::set;
        Runnable none = () -> toss(error);

        just(true).match(some, none);
        assertTrue(changed.get());
    }

    @Test
    public void matchTestVoidNothing()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        Consumer<Integer> some = i -> toss(error);
        Runnable none = () -> changed.set(true);
        Maybe<Integer> nothing = nothing();

        nothing.match(some, none);
        assertTrue(changed.get());
    }

    @Test
    public void ofTestNullable()
    {
        assertEquals(nothing(), Maybe.of((Integer)null));
        assertEquals(just(0), Maybe.of(0));
    }

    @Test
    public void ofTestOptional()
    {
        assertEquals(nothing(), Maybe.of(Optional.empty()));
        assertEquals(just(0), Maybe.of(Optional.of(0)));
    }

    @Test
    public void unsafeMatchTestProducingJust() throws IOException
    {
        ThrowingFunction<Integer, Boolean, IOException> some = i -> true;
        ThrowingSupplier<Boolean, IOException> none = () -> toss(error);

        assertTrue(just(0).unsafeMatchThen(some, none));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingJustError() throws IOException
    {
        ThrowingFunction<Integer, ?, IOException> some = i -> toss(expected);
        ThrowingSupplier<?, IOException> none = () -> toss(error);

        just(0).unsafeMatchThen(some, none);
    }

    @Test
    public void unsafeMatchTestProducingNothing() throws IOException
    {
        ThrowingFunction<Integer, Boolean, IOException> some= i -> toss(error);
        ThrowingSupplier<Boolean, IOException> none = () -> false;
        Maybe<Integer> nothing = nothing();

        assertFalse(nothing.unsafeMatchThen(some, none));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingNothingError() throws IOException
    {
        ThrowingFunction<Integer, ?, IOException> some = i -> toss(error);
        ThrowingSupplier<?, IOException> none = () -> toss(expected);
        Maybe<Integer> nothing = nothing();

        nothing.unsafeMatchThen(some, none);
    }

    @Test
    public void unsafeMatchTestVoidJust() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        ThrowingConsumer<Boolean, IOException> some = b -> changed.set(b);
        ThrowingRunnable<IOException> none = () -> toss(error);

        just(true).unsafeMatch(some, none);
        assertTrue(changed.get());
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidJustError() throws IOException
    {
        ThrowingConsumer<Integer, IOException> some = i -> toss(expected);
        ThrowingRunnable<IOException> none = () -> toss(error);

        just(0).unsafeMatch(some, none);
    }

    @Test
    public void unsafeMatchTestVoidNothing() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        ThrowingConsumer<Integer, IOException> some = i -> toss(error);
        ThrowingRunnable<IOException> none = () -> changed.set(true);
        Maybe<Integer> nothing = nothing();

        nothing.unsafeMatch(some, none);
        assertTrue(changed.get());
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidNothingError() throws IOException
    {
        ThrowingConsumer<Integer, IOException> some = i -> toss(error);
        ThrowingRunnable<IOException> none = () -> toss(expected);
        Maybe<Integer> nothing = nothing();

        nothing.unsafeMatch(some, none);
    }

    @Test
    public void hashCodeTest()
    {
        assertEquals(just(0).hashCode(), just(0).hashCode());
        assertEquals(nothing().hashCode(), nothing().hashCode());
        assertNotEquals(just(0).hashCode(), nothing().hashCode());
        assertNotEquals(nothing().hashCode(), just(0).hashCode());
        assertNotEquals(just(0).hashCode(), just(1).hashCode());
    }

    @Test
    public void equalsTest()
    {
        assertEquals(just(0), just(0));
        assertEquals(nothing(), nothing());
        assertNotEquals(just(0), nothing());
        assertNotEquals(nothing(), just(0));
        assertNotEquals(just(0), just(1));
    }

    @Test
    public void toStringTest()
    {
        String value = "hello";
        assertThat(Maybe.just(value).toString(), containsString(value.toString()));
        Maybe.nothing().toString(); //ensure no exception is thrown
    }
}
