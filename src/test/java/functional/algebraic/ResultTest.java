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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static functional.algebraic.Result.*;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class ResultTest
{
    public static IOException expected = new IOException("Expected branch");
    public static IOException unexpected = new IOException();
    public static RuntimeException expectedRuntime = new RuntimeException("Expected branch");
    public static Error error = new AssertionError("Wrong branch taken");

    @Test
    public void matchTestFunction()
    {
        assertTrue(
                value(true).matchThen(
                        v -> v,
                        e -> false));
        assertTrue(
                error(expected).matchThen(
                        v -> false,
                        e -> true));
    }

    @Test
    public void matchTestConsumerValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        value(true).match(
                changed::set,
                e -> toss(error));

        assertTrue(changed.get());
    }

    @Test
    public void matchTestConsumerError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        error(expected).match(
                v -> toss(error),
                e -> changed.set(true));

        assertTrue(changed.get());
    }

    @Test
    public void collapseTestProducing()
    {
        assertEquals(
                ((Integer) 1).toString(),
                value(1).collapseThen(Object::toString));
        assertEquals(
                expected.toString(),
                error(expected).collapseThen(Object::toString));
    }

    @Test
    public void collapseTest()
    {
        value(1).collapse(o -> assertEquals(1, o));
        error(expected).collapse(o -> assertEquals(expected, o));
    }

    @Test
    public void orElseTest()
    {
        assertEquals((Integer) 1, value(1).orElse(0));
        assertEquals(0, error(unexpected).orElse(0));
    }

    @Test
    public void andThenTest()
    {
        assertEquals(
                value(true),
                value(true).andThen(v -> value(v)));
        assertEquals(
                error(expected),
                error(expected).andThen(v -> value(v)));
        assertEquals(
                error(expected),
                value(true).andThen(v -> error(expected)));
        assertEquals(
                value(true),
                value(true).andThenT(v -> v));
        assertEquals(
                error(expected),
                error(expected).andThenT(v -> v));
        assertEquals(
                error(expected),
                value(true).andThenT(v -> toss(expected)));
    }

    @Test
    public void andDoTestValueValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                value(null),
                value(1).andDoT(v -> changed.set(v == 1)));

        assertTrue(changed.get());
    }

    @Test
    public void andDoTestErrorValue()
    {
        assertEquals(
                error(expected),
                error(expected).andDoT(v -> toss(error)));
    }

    @Test
    public void andDoTestError()
    {
        assertEquals(
                error(expected),
                value(1).andDoT(__ -> toss(expected)));
        assertEquals(
                error(expected),
                error(expected).andDoT(__ -> toss(unexpected)));
    }

    @Test
    public void orTest()
    {
        assertEquals(
                value(false),
                value(false).or(value(true)));
        assertEquals(
                value(false),
                value(false).or(() -> value(true)));
        assertEquals(
                value(false),
                value(false).or(error(unexpected)));
        assertEquals(
                value(false),
                value(false).or(() -> error(unexpected)));
        assertEquals(
                value(true),
                error(unexpected).or(value(true)));
        assertEquals(
                value(true),
                error(unexpected).or(() -> value(true)));
        assertEquals(
                error(expected),
                error(unexpected).or(error(expected)));
        assertEquals(
                error(expected),
                error(unexpected).or(() -> error(expected)));
    }

    @Test
    public void orTTest()
    {
        assertEquals(
                value(false),
                value(false).orT(() -> true));
        assertEquals(
                value(false),
                value(false).orT(() -> toss(unexpected)));
        assertEquals(
                value(true),
                error(unexpected).orT(() -> true));
        assertEquals(
                error(expected),
                error(unexpected).orT(() -> toss(expected)));
    }

    @Test
    public void ifOkTestValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                value(true),
                value(true).ifOk(v -> changed.set(v)));

        assertTrue(changed.get());
    }

    @Test
    public void ifOkTestError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                error(expected),
                error(expected).ifOk(v -> changed.set(true)));

        assertFalse(changed.get());
    }

    @Test
    public void ifErrTestValue()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                value(true),
                value(true).ifErr(v -> changed.set(true)));

        assertFalse(changed.get());
    }

    @Test
    public void ifErrTestError()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        assertEquals(
                error(expected),
                error(expected).ifErr(v -> changed.set(true)));

        assertTrue(changed.get());
    }

    @Test
    public void andTTest()
    {
        assertEquals(
                value(null),
                value(false).andT(() -> {}));
        assertEquals(
                value(true),
                value(false).andT(() -> true));
        assertEquals(
                error(expected),
                value(false).andT(() -> toss(expected)));
        assertEquals(
                error(expected),
                error(expected).andT(() -> true));
        assertEquals(
                error(expected),
                error(expected).andT(() -> toss(unexpected)));
    }

    @Test
    public void peekTest()
    {
        assertEquals(
                value(false),
                value(false).peek(b -> value(null)));
        assertEquals(
                error(expected),
                value(false).peek(b -> error(expected)));
        assertEquals(
                error(expected),
                error(expected).peek(b -> value(null)));
        assertEquals(
                value(false),
                value(false).peekT(b -> {}));
        assertEquals(
                error(expected),
                value(false).peekT(b -> toss(expected)));
        assertEquals(
                error(expected),
                error(expected).peekT(o -> {}));
        assertEquals(
                error(expected),
                error(expected).peekT(o -> toss(unexpected)));
    }

    @Test
    public void andTest()
    {
        assertEquals(
                value(true),
                value(false).and(() -> value(true)));
        assertEquals(
                value(true),
                value(false).and(value(true)));
        assertEquals(
                error(expected),
                value(false).and(error(expected)));
        assertEquals(
                error(expected),
                value(false).and(() -> error(expected)));
        assertEquals(
                error(expected),
                error(expected).and(value(true)));
        assertEquals(
                error(expected),
                error(expected).and(() -> value(true)));
        assertEquals(
                error(expected),
                error(expected).and(error(unexpected)));
        assertEquals(
                error(expected),
                error(expected).and(() -> error(unexpected)));
    }

    @Test
    public void setTest()
    {
        assertEquals(
                value(true),
                value(false).set(true));
        assertEquals(
                error(expected),
                error(expected).set(true));
    }

    @Test
    public void mapTest()
    {
        assertEquals(
                value(false),
                value(true).map(t -> !t));
        assertEquals(
                error(expected),
                Result.<Boolean, IOException>error(expected).map(t -> !t));
    }

    @Test
    public void mapErrorTest()
    {
        assertEquals(
                value(true),
                value(true).mapError(e -> unexpected));
        assertEquals(
                error(expected),
                error(unexpected).mapError(e -> expected));
    }

    @Test
    public void convertErrorTest()
    {
        assertTrue(value(true).convertError(e -> false));
        assertTrue(Result.<Boolean, IOException>error(unexpected).convertError(e -> true));
    }

    @Test
    public void bimapTest()
    {
        assertEquals(
                value(true),
                value(true).bimap(value(false), (a, b) -> a || b));
        assertEquals(
                error(expected),
                value(true).bimap(Result.<Boolean, Exception>error(expected), (a, b) -> a || b));
        assertEquals(
                error(expected),
                Result.<Boolean, IOException>error(expected).bimap(value(false), (a, b) -> a || b));
    }

    @Test
    public void getTestValue() throws Exception
    {
        assertTrue(value(true).get());
    }

    @Test(expected = IOException.class)
    public void getTestError() throws IOException
    {
        error(expected).get();
    }

    @Test
    public void ofTestRunnable()
    {
        assertEquals(
                value(null),
                of(() -> {}));
        assertEquals(
                error(expected),
                of(() -> toss(expected)));
    }

    @Test
    public void ofTestSupplier()
    {
        assertEquals(
                value(true),
                of(() -> true));
        assertEquals(
                error(expected),
                of(() -> toss(expected)));
    }

    @Test(expected = RuntimeException.class)
    public void ofTestSupplierRuntime()
    {
        of(() -> toss(expectedRuntime));
    }

    @Test
    public void ofRuntimeTest()
    {
        assertEquals(
                ofRuntime(() -> {}),
                value(null));
        assertEquals(
                error(expectedRuntime),
                ofRuntime((Runnable) () -> toss(expectedRuntime)));
        assertEquals(
                value(true),
                ofRuntime(() -> true));
        assertEquals(
                error(expectedRuntime),
                ofRuntime((Supplier<?>) () -> toss(expectedRuntime)));
    }

    @Test
    public void convertTest()
    {
        assertEquals(
                value(true),
                convertFunction(t -> t).apply(true));
        assertEquals(
                error(expected),
                convertConsumer(t -> toss(expected)).apply(true));
    }

    @Test
    public void transposeTest()
    {
        Maybe<Result<Boolean, Exception>> n = Maybe.nothing();
        Result<Maybe<Boolean>, Exception> vn = value(Maybe.nothing());

        Maybe<Result<Boolean, Exception>> jv = Maybe.just(value(true));
        Result<Maybe<Boolean>, Exception> vj = value(Maybe.just(true));

        Maybe<Result<Boolean, Exception>> je = Maybe.just(error(expected));
        Result<Maybe<Boolean>, Exception> e = error(expected);

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
                value(true),
                join(value(value(true))));
        assertEquals(
                error(expected),
                join(value(error(expected))));
        assertEquals(
                error(expected),
                join(error(expected)));
    }

    @Test
    public void equalsTest()
    {
        assertEquals(value(true), value(true));
        assertNotEquals(value(true), value(false));
        assertNotEquals(value(true), error(unexpected));

        assertEquals(error(expected), error(expected));
        assertNotEquals(error(expected), error(unexpected));

        assertNotEquals(value(true), Either.right(true));
    }
    @Test
    public void hashCodeTest()
    {
        assertEquals(value(true).hashCode(), value(true).hashCode());
        assertNotEquals(value(true).hashCode(), value(false).hashCode());
        assertNotEquals(value(true).hashCode(), error(unexpected).hashCode());

        assertEquals(error(expected).hashCode(), error(expected).hashCode());
        assertNotEquals(error(expected).hashCode(), error(unexpected).hashCode());

        assertNotEquals(value(true).hashCode(), Either.right(true).hashCode());
    }


    @Test
    public void toStringTest()
    {
        Double value = 2.0;
        assertThat(error(expected).toString(), containsString(expected.toString()));
        assertThat(value(value).toString(), containsString(value.toString()));
    }
}
