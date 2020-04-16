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

import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static functional.algebraic.Result.*;
import static functional.algebraic.testutils.TestUtils.assertThrows;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class ResultTest
{
    public static IOException expected = new IOException("Expected branch");
    public static IOException unexpected = new IOException("Wrong branch taken");
    public static RuntimeException expectedRuntime = new RuntimeException("Expected branch");
    public static Error error = new AssertionError("Wrong branch taken");

    @Test
    public void matchTestFunction()
    {
        assertTrue(
                ok(true).matchThen(
                        v -> v,
                        e -> false));
        assertTrue(
                err(expected).matchThen(
                        v -> false,
                        e -> true));
    }

    @Test
    public void matchTestConsumerValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        ok(true).match(
                changed::set,
                e -> toss(error));

        assertTrue(changed.get());
    }

    @Test
    public void matchTestConsumerError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        err(expected).match(
                v -> toss(error),
                e -> changed.set(true));

        assertTrue(changed.get());
    }

    @Test
    public void collapseTestProducing()
    {
        assertEquals(
                ((Integer) 1).toString(),
                ok(1).collapseThen(Object::toString));
        assertEquals(
                expected.toString(),
                err(expected).collapseThen(Object::toString));
    }

    @Test
    public void collapseTest()
    {
        ok(1).collapse(o -> assertEquals(1, o));
        err(expected).collapse(o -> assertEquals(expected, o));
    }

    @Test
    public void orElseTest()
    {
        assertEquals((Integer) 1, ok(1).orElse(0));
        assertEquals(0, err(unexpected).orElse(0));
    }

    @Test
    public void andThenTest()
    {
        assertEquals(
                ok(true),
                ok(true).andThen(v -> ok(v)));
        assertEquals(
                err(expected),
                err(expected).andThen(v -> ok(v)));
        assertEquals(
                err(expected),
                ok(true).andThen(v -> err(expected)));
        assertEquals(
                ok(true),
                ok(true).andThenT(v -> v));
        assertEquals(
                err(expected),
                err(expected).andThenT(v -> v));
        assertEquals(
                err(expected),
                ok(true).andThenT(v -> toss(expected)));
    }

    @Test
    public void andDoTestValueValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                ok(null),
                ok(1).andDoT(v -> changed.set(v == 1)));

        assertTrue(changed.get());
    }

    @Test
    public void andDoTestError()
    {
        assertEquals(
                err(expected),
                err(expected).andDoT(v -> toss(error)));
        assertEquals(
                err(expected),
                ok(1).andDoT(__ -> toss(expected)));
        assertEquals(
                err(expected),
                err(expected).andDoT(__ -> toss(unexpected)));
    }

    @Test
    public void orTest()
    {
        assertEquals(
                ok(false),
                ok(false).or(ok(true)));
        assertEquals(
                ok(false),
                ok(false).orGet(() -> ok(true)));
        assertEquals(
                ok(false),
                ok(false).or(err(unexpected)));
        assertEquals(
                ok(false),
                ok(false).orGet(() -> err(unexpected)));
        assertEquals(
                ok(true),
                err(unexpected).or(ok(true)));
        assertEquals(
                ok(true),
                err(unexpected).orGet(() -> ok(true)));
        assertEquals(
                err(expected),
                err(unexpected).or(err(expected)));
        assertEquals(
                err(expected),
                err(unexpected).orGet(() -> err(expected)));
    }

    @Test
    public void orTTest()
    {
        assertEquals(
                ok(false),
                ok(false).orGetT(() -> true));
        assertEquals(
                ok(false),
                ok(false).orGetT(() -> toss(unexpected)));
        assertEquals(
                ok(true),
                err(unexpected).orGetT(() -> true));
        assertEquals(
                err(expected),
                err(unexpected).orGetT(() -> toss(expected)));
    }

    @Test
    public void ifOkTestValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                ok(true),
                ok(true).ifOk(v -> changed.set(v)));

        assertTrue(changed.get());
    }

    @Test
    public void ifOkTestError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                err(expected),
                err(expected).ifOk(v -> changed.set(true)));

        assertFalse(changed.get());
    }

    @Test
    public void ifErrTestValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                ok(true),
                ok(true).ifErr(v -> changed.set(true)));

        assertFalse(changed.get());
    }

    @Test
    public void ifErrTestError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                err(expected),
                err(expected).ifErr(v -> changed.set(true)));

        assertTrue(changed.get());
    }

    @Test
    public void andTTest()
    {
        assertEquals(
                ok(null),
                ok(false).andDoT(() -> {}));
        assertEquals(
                ok(true),
                ok(false).andGetT(() -> true));
        assertEquals(
                err(expected),
                ok(false).andGetT(() -> toss(expected)));
        assertEquals(
                err(expected),
                err(expected).andGetT(() -> true));
        assertEquals(
                err(expected),
                err(expected).andGetT(() -> toss(unexpected)));
    }
    @Test
    public void andTest()
    {
        assertEquals(
                ok(true),
                ok(false).and(ok(true)));
        assertEquals(
                err(expected),
                ok(false).and(err(expected)));
        assertEquals(
                err(expected),
                err(expected).and(ok(true)));
        assertEquals(
                err(expected),
                err(expected).and(err(unexpected)));
    }

    @Test
    public void andGetTest()
    {
        assertEquals(
                ok(true),
                ok(false).andGet(() -> ok(true)));
        assertEquals(
                err(expected),
                ok(false).andGet(() -> err(expected)));
        assertEquals(
                err(expected),
                err(expected).andGet(() -> ok(true)));
        assertEquals(
                err(expected),
                err(expected).andGet(() -> err(unexpected)));
    }

    @Test
    public void mapTest()
    {
        assertEquals(
                ok(false),
                ok(true).map(t -> !t));
        assertEquals(
                err(expected),
                Result.<Boolean, IOException>err(expected).map(t -> !t));
    }

    @Test
    public void mapErrorTest()
    {
        assertEquals(
                ok(true),
                ok(true).mapError(e -> unexpected));
        assertEquals(
                err(expected),
                err(unexpected).mapError(e -> expected));
    }

    @Test
    public void convertErrorTest()
    {
        assertTrue(ok(true).convertError(e -> false));
        assertTrue(Result.<Boolean, IOException>err(unexpected).convertError(e -> true));
    }

    @Test
    public void bimapTest()
    {
        assertEquals(
                ok(true),
                ok(true).bimap(ok(false), (a, b) -> a || b));
        assertEquals(
                err(expected),
                ok(true).bimap(Result.<Boolean, Exception>err(expected), (a, b) -> a || b));
        assertEquals(
                err(expected),
                Result.<Boolean, IOException>err(expected).bimap(ok(false), (a, b) -> a || b));
    }

    @Test
    public void unwrapTest() throws Exception
    {
        assertTrue(ok(true).unwrap());
        assertThrows(
                IOException.class,
                () -> err(expected).unwrap());
    }

    @Test
    public void ofTestRunnable()
    {
        assertEquals(
                ok(null),
                of(() -> {}));
        assertEquals(
                err(expected),
                of(() -> toss(expected)));
    }

    @Test
    public void ofTestSupplier()
    {
        assertEquals(
                ok(true),
                of(() -> true));
        assertEquals(
                err(expected),
                of(() -> toss(expected)));
        assertThrows(
                RuntimeException.class, () ->
                of(() -> toss(expectedRuntime)));
    }

    @Test
    public void ofRuntimeTest()
    {
        assertEquals(
                ofRuntime(() -> {}),
                ok(null));
        assertEquals(
                err(expectedRuntime),
                ofRuntime((Runnable) () -> toss(expectedRuntime)));
        assertEquals(
                ok(true),
                ofRuntime(() -> true));
        assertEquals(
                err(expectedRuntime),
                ofRuntime((Supplier<?>) () -> toss(expectedRuntime)));
    }

    @Test
    public void convertTest()
    {
        assertEquals(
                ok(true),
                convertFunction(t -> t).apply(true));
        assertEquals(
                err(expected),
                convertConsumer(t -> toss(expected)).apply(true));
    }

    @Test
    public void transposeTest()
    {
        Option<Result<Boolean, Exception>> n = Option.none();
        Result<Option<Boolean>, Exception> vn = ok(Option.none());

        Option<Result<Boolean, Exception>> jv = Option.some(ok(true));
        Result<Option<Boolean>, Exception> vj = ok(Option.some(true));

        Option<Result<Boolean, Exception>> je = Option.some(err(expected));
        Result<Option<Boolean>, Exception> e = err(expected);

        assertEquals(n, transpose(vn));
        assertEquals(n, transpose(transpose(n)));

        assertEquals(jv, transpose(vj));
        assertEquals(jv, transpose(transpose(jv)));

        assertEquals(je, transpose(e));
        assertEquals(je, transpose(transpose(je)));
    }

    @Test
    public void joinTest()
    {
        assertEquals(
                ok(true),
                join(ok(ok(true))));
        assertEquals(
                err(expected),
                join(ok(err(expected))));
        assertEquals(
                err(expected),
                join(err(expected)));
    }

    @Test
    public void iteratorTest()
    {
        Iterator<Boolean> iter = ok(true).iterator();
        assertTrue(iter.next());
        assertFalse(iter.hasNext());
        assertFalse(err(unexpected).iterator().hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
        assertThrows(NoSuchElementException.class, err(unexpected).iterator()::next);
    }

    @Test
    public void equalsTest()
    {
        assertEquals(ok(true), ok(true));
        assertNotEquals(ok(true), ok(false));
        assertNotEquals(ok(true), err(unexpected));

        assertEquals(err(expected), err(expected));
        assertNotEquals(err(expected), err(unexpected));

        assertNotEquals(ok(true), Either.right(true));
    }
    @Test
    public void hashCodeTest()
    {
        assertEquals(ok(true).hashCode(), ok(true).hashCode());
        assertNotEquals(ok(true).hashCode(), ok(false).hashCode());
        assertNotEquals(ok(true).hashCode(), err(unexpected).hashCode());

        assertEquals(err(expected).hashCode(), err(expected).hashCode());
        assertNotEquals(err(expected).hashCode(), err(unexpected).hashCode());

        assertNotEquals(ok(true).hashCode(), Either.right(true).hashCode());
    }


    @Test
    public void toStringTest()
    {
        Double value = 2.0;
        assertThat(err(expected).toString(), containsString(expected.toString()));
        assertThat(ok(value).toString(), containsString(value.toString()));
    }
}
