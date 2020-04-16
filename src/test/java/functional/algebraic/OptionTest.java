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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static functional.algebraic.Option.*;
import static functional.algebraic.testutils.TestUtils.*;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class OptionTest
{
    public static Error             error    = new AssertionError("Wrong branch taken");
    public static ExpectedException expected = new ExpectedException();

    @Test
    public void andThenTestSome()
    {
        int x = 0;
        Function<Integer, Option<Integer>> f = i -> some(i + 1);

        assertEquals(f.apply(x), some(x).andThen(f));
        assertTrue(some(x).andThen(f).isSome());
    }

    @Test
    public void andThenTestSomeNone()
    {
        int x = 0;
        Function<Object, Option<Object>> f = i -> none();

        assertEquals(f.apply(x), some(x).andThen(f));
        assertEquals(none(), some(x).andThen(f));
    }

    @Test
    public void andThenTestNone()
    {
        Function<Integer, Option<Integer>> g = i -> some(i + 1);

        Option<Integer> none = Option.none();
        assertEquals(none(), none.andThen(g));
    }

    @Test
    public void unwrapTest()
    {
        assertTrue(some(true).unwrap());
        assertThrows(NoSuchElementException.class, none()::unwrap);
    }

    @Test
    public void unwrapOrTest()
    {
        assertTrue(Option.<Boolean>some(true).unwrapOr(false));
        assertFalse(Option.<Boolean>none().unwrapOr(false));
    }

    @Test
    public void consumeTest()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        some(true).consume(v -> changed.set(v));
        none().consume(v -> toss(error));

        assertTrue(changed.get());
    }

    @Test
    public void isSomeTest()
    {
        assertTrue(some(0).isSome());
        assertFalse(none().isSome());
    }

    @Test
    public void isNoneTest()
    {
        assertTrue(none().isNone());
        assertFalse(some(0).isNone());
    }

    @Test
    public void iteratorTest()
    {
        Iterator<Boolean> iter = some(true).iterator();
        assertTrue(iter.next());
        assertFalse(iter.hasNext());
        assertFalse(none().iterator().hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
        assertThrows(NoSuchElementException.class, none().iterator()::next);
    }

    @Test
    public void mapTest()
    {
        Function<Integer, Integer> f = x -> x + 1;

        int x = 0;
        Option<Integer> none = none();

        assertEquals(
                some(f.apply(x)),
                some(x).map(f));

        assertEquals(
                none,
                none.map(f));
    }

    @Test
    public void andTest()
    {
        assertEquals(
                some(0),
                some(1).and(some(0)));

        assertEquals(
                none(),
                none().and(some(0)));
        assertEquals(
                none(),
                some(0).and(none()));
        assertEquals(
                none(),
                none().and(none()));
    }
    @Test
    public void orTest()
    {
        assertEquals(
                some(1),
                some(1).or(some(0)));
        assertEquals(
                some(0),
                none().or(some(0)));
        assertEquals(
                some(0),
                some(0).or(none()));

        assertEquals(
                none(),
                none().or(none()));
    }
    @Test
    public void xorTest()
    {
        assertEquals(
                none(),
                some(1).xor(some(0)));
        assertEquals(
                some(0),
                none().xor(some(0)));
        assertEquals(
                some(0),
                some(0).xor(none()));

        assertEquals(
                none(),
                none().xor(none()));
    }

    @Test
    public void filterTest()
    {
        Option<Integer> none = none();
        assertEquals(
                some(0),
                some(0).filter(v -> v == 0));

        assertEquals(
                none(),
                none.filter(v -> v == 0));
        assertEquals(
                none(),
                some(1).filter(v -> v == 0));
        assertEquals(
                none(),
                none.filter(v -> v == 0));
    }

    @Test
    public void orElseTest()
    {
        assertEquals(
                some(1),
                some(1).orElse(() -> some(0)));
        assertEquals(
                some(0),
                none().orElse(() -> some(0)));
        assertEquals(
                some(0),
                some(0).orElse(() -> none()));
        assertEquals(
                none(),
                none().orElse(() -> none()));
    }

    @Test
    public void mapOrTest()
    {
        Function<Integer, Integer> increment = i -> i + 1;
        Option<Integer> none = none();

        assertEquals(
                2,
                (int) some(1).mapOr(increment, 0));
        assertEquals(
                0,
                (int) none.mapOr(increment, 0));
    }

    @Test
    public void mapOrElseTest()
    {
        Function<Integer, Integer> increment = i -> i + 1;
        Option<Integer> none = none();

        assertEquals(
                2,
                (int) some(1).mapOrElse(increment, () -> 0));
        assertEquals(
                0,
                (int) none.mapOrElse(increment, () -> 0));
    }

    @Test
    public void matchThenTestSome()
    {
        Function<Integer, Boolean> some= i -> true;
        Supplier<Boolean> none = () -> false;

        assertTrue(some(0).matchThen(some, none));
    }

    @Test
    public void matchThenTestNone()
    {
        Function<Integer, Boolean> s = i -> true;
        Supplier<Boolean> n = () -> false;
        Option<Integer> none = none();

        assertFalse(none.matchThen(s, n));
    }

    @Test
    public void matchTestSome()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        Consumer<Boolean> s = changed::set;
        Runnable n = () -> toss(error);

        some(true).match(s, n);
        assertTrue(changed.get());
    }

    @Test
    public void matchTestNone()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        Consumer<Integer> s = i -> toss(error);
        Runnable n = () -> changed.set(true);
        Option<Integer> none = none();

        none.match(s, n);
        assertTrue(changed.get());
    }

    @Test
    public void ofTestNullable()
    {
        assertEquals(none(), Option.of((Integer)null));
        assertEquals(some(0), Option.of(0));
    }

    @Test
    public void ofTestOptional()
    {
        assertEquals(none(), Option.of(Optional.empty()));
        assertEquals(some(0), Option.of(Optional.of(0)));
    }

    @Test
    public void unsafeMatchThenTestSome() throws IOException
    {
        ThrowingFunction<Integer, Boolean, IOException> some = i -> true;
        ThrowingSupplier<Boolean, IOException> none = () -> toss(error);

        assertTrue(some(0).unsafeMatchThen(some, none));
    }

    @Test
    public void unsafeMatchThenTestSomeError()
    {
        ThrowingFunction<Integer, ?, ExpectedException> some = i -> toss(expected);
        ThrowingSupplier<?, ExpectedException> none = () -> toss(error);

        assertThrows(
                ExpectedException.class,
                () -> some(0).unsafeMatchThen(some, none));
    }

    @Test
    public void unsafeMatchThenTestNone() throws IOException
    {
        ThrowingFunction<Integer, Boolean, IOException> s= i -> toss(error);
        ThrowingSupplier<Boolean, IOException> n = () -> false;
        Option<Integer> none = none();

        assertFalse(none.unsafeMatchThen(s, n));
    }

    @Test
    public void unsafeMatchThenTestNoneError()
    {
        ThrowingFunction<Integer, ?, ExpectedException> s = i -> toss(error);
        ThrowingSupplier<?, ExpectedException> n = () -> toss(expected);
        Option<Integer> none = none();

        assertThrows(
                ExpectedException.class,
                () -> none.unsafeMatchThen(s, n));
    }

    @Test
    public void unsafeMatchTestSome() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        ThrowingConsumer<Boolean, IOException> s = b -> changed.set(b);
        ThrowingRunnable<IOException> n = () -> toss(error);

        some(true).unsafeMatch(s, n);
        assertTrue(changed.get());
    }

    @Test
    public void unsafeMatchTestSomeError()
    {
        ThrowingConsumer<Integer, ExpectedException> s = i -> toss(expected);
        ThrowingRunnable<ExpectedException> n = () -> toss(error);

        assertThrows(
                ExpectedException.class,
                () -> some(0).unsafeMatch(s, n));
    }

    @Test
    public void unsafeMatchTestNone() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        ThrowingConsumer<Integer, IOException> s = i -> toss(error);
        ThrowingRunnable<IOException> n = () -> changed.set(true);
        Option<Integer> none = none();

        none.unsafeMatch(s, n);
        assertTrue(changed.get());
    }

    @Test
    public void unsafeMatchTestNoneError()
    {
        ThrowingConsumer<Integer, ExpectedException> s = i -> toss(error);
        ThrowingRunnable<ExpectedException> n = () -> toss(expected);
        Option<Integer> none = none();

        assertThrows(
                ExpectedException.class,
                () -> none.unsafeMatch(s, n));
    }

    @Test
    public void hashCodeTest()
    {
        assertEquals(some(0).hashCode(), some(0).hashCode());
        assertEquals(none().hashCode(), none().hashCode());
        assertNotEquals(some(0).hashCode(), none().hashCode());
        assertNotEquals(none().hashCode(), some(0).hashCode());
        assertNotEquals(some(0).hashCode(), some(1).hashCode());
    }

    @Test
    public void equalsTest()
    {
        assertEquals(some(0), some(0));
        assertEquals(none(), none());
        assertNotEquals(some(0), none());
        assertNotEquals(none(), some(0));
        assertNotEquals(some(0), some(1));
    }

    @Test
    public void toStringTest()
    {
        String value = "hello";
        assertThat(Option.some(value).toString(), containsString(value.toString()));
        Option.none().toString(); //ensure no exception is thrown
    }
}
