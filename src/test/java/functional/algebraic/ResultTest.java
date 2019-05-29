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
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class ResultTest
{
    @Test
    public void matchTestFunctionValue()
    {
        assertTrue(Result.value(true)
                .match(
                        e -> false,
                        v -> v));
    }

    @Test
    public void matchTestFunctionError()
    {
        assertTrue(Result.error(new Exception())
                .match(
                        e -> true,
                        v -> false));
    }

    @Test
    public void matchTestConsumerValue()
    {
        Result.value(true)
                .match(
                        e -> {throw new AssertionFailedError("Wrong branch taken");},
                        v -> {});
    }

    @Test
    public void matchTestConsumerError()
    {
        Result.error(new Exception())
                .match(
                        e -> {},
                        v -> {throw new AssertionFailedError("Wrong branch taken");});
    }

    @Test
    public void bindTestValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(true).bind(Result::value));
    }

    @Test
    public void bindTestErrorValue()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.error(e).bind(Result::value));
    }

    @Test
    public void bindTestValueError()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.value(true).bind(v -> Result.error(e)));
    }

    @Test
    public void bindTTestValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(true).bindT(v -> v));
    }

    @Test
    public void bindTTestErrorValue()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.error(e).bindT(v -> v));
    }

    @Test
    public void bindTTestValueError()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.value(true).bindT((ThrowingConsumer<Boolean, Exception>) v -> {throw e;}));
    }

    @Test
    public void thenTestValueValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(false).then(() -> Result.value(true)));
    }

    @Test
    public void thenTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(false).then(() -> Result.error(e)));
    }

    @Test
    public void thenTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).then(() -> Result.value(true)));
    }

    @Test
    public void thenTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).then(() -> Result.error(new IOException())));
    }

    @Test
    public void thenTTestValueVoid()
    {
        assertEquals(
                Result.value(null),
                Result.value(false).thenT(() -> {}));
    }

    @Test
    public void thenTTestValueValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(false).thenT(() -> true));
    }

    @Test
    public void thenTTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(false).thenT(() -> {throw e;}));
    }

    @Test
    public void thenTTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).thenT(() -> true));
    }

    @Test
    public void thenTTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).thenT(() -> {throw new IOException();}));
    }


    @Test
    public void peekTestValueValue()
    {
        assertEquals(
                Result.value(false),
                Result.value(false).peek(b -> Result.value(!b)));
    }

    @Test
    public void peekTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(false).peek(b -> Result.error(e)));
    }

    @Test
    public void peekTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).peek(o -> Result.value(o)));
    }

    @Test
    public void peekTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).peek(o -> Result.error(new FileNotFoundException())));
    }

    public void peekTTestValueValue()
    {
        assertEquals(
                Result.value(false),
                Result.value(false).peekT(b -> 3));
    }

    @Test
    public void peekTTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(false).peekT((ThrowingConsumer<Boolean, Exception>) b -> {throw e;}));
    }

    @Test
    public void peekTTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).peekT(o -> o));
    }

    @Test
    public void peekTTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).peekT((ThrowingConsumer<Object, IOException>) o -> {throw new FileNotFoundException();}));
    }


    @Test
    public void setTestValueValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(false).set(Result.value(true)));
    }

    @Test
    public void setTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(false).set(Result.error(e)));
    }

    @Test
    public void setTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).set(Result.value(true)));
    }

    @Test
    public void setTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).set(Result.error(new FileNotFoundException())));
    }

    @Test
    public void setVTestValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(false).setV(true));
    }

    @Test
    public void setVTestError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.error(e).setV(true));
    }

    @Test
    public void mapTestValue()
    {
        assertEquals(
                Result.value(false),
                Result.value(true).map(t -> !t));
    }

    @Test
    public void mapTestError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.<IOException, Boolean>error(e).map(t -> !t));
    }

    @Test
    public void bimapTestValue()
    {
        assertEquals(
                Result.value(true),
                Result.value(true).bimap(Result.value(false), (a, b) -> a || b));
    }

    @Test
    public void bimapTestValueError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.value(true).bimap(Result.<Exception, Boolean>error(e), (a, b) -> a || b));
    }
    @Test
    public void bimapTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.<IOException, Boolean>error(e).bimap(Result.value(false), (a, b) -> a || b));
    }

    @Test
    public void getTestValue() throws Exception
    {
        assertTrue(Result.value(true).get());
    }

    @Test(expected = IOException.class)
    public void getTestError() throws IOException
    {
        Result.error(new IOException()).get();
    }

    @Test
    public void ofTestRunnableValue()
    {
        assertEquals(
                Result.value(null),
                Result.of(() -> {}));
    }

    @Test
    public void ofTestRunnableError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.of((ThrowingRunnable<IOException>) () -> {throw e;}));
    }

    @Test
    public void ofTestSupplierValue()
    {
        assertEquals(
                Result.value(true),
                Result.of(() -> true));
    }

    @Test
    public void ofTestSupplierError()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.of((ThrowingSupplier<?, IOException>) () -> {throw e;}));
    }

    @Test(expected = RuntimeException.class)
    public void ofTestSupplierRuntime()
    {
        IOException e = new IOException();
        assertEquals(
                Result.error(e),
                Result.of((ThrowingSupplier<?, IOException>) () -> {throw new RuntimeException();}));
    }

    @Test
    public void ofRuntimeTestRunnableValue()
    {
        assertEquals(Result.ofRuntime(() -> {}), Result.value(null));
    }

    @Test
    public void ofRuntimeTestRunnableError()
    {
        RuntimeException e = new IllegalArgumentException();
        assertEquals(
                Result.error(e),
                Result.ofRuntime((Runnable) () -> {throw e;}));
    }

    @Test
    public void ofRuntimeTestSupplierValue()
    {
        assertEquals(
                Result.value(true),
                Result.ofRuntime(() -> true));
    }

    @Test
    public void ofRuntimeTestSupplierError()
    {
        RuntimeException e = new IllegalArgumentException();
        assertEquals(
                Result.error(e),
                Result.ofRuntime((Supplier<?>) () -> {throw e;}));
    }

    @Test
    public void convertTestValue()
    {
        ThrowingFunction<Boolean,Boolean, IOException> f = t -> t;
        assertEquals(
                Result.value(true),
                Result.convert(f).apply(true));
    }

    @Test
    public void convertTestError()
    {
        IOException e = new IOException();
        ThrowingFunction<Boolean,?, IOException> f = t -> {throw e;};
        assertEquals(
                Result.error(e),
                Result.convert(f).apply(true));
    }

    @Test
    public void joinTestValue()
    {
        assertEquals(
                Result.value(true),
                Result.join(Result.value(Result.value(true))));
    }

    @Test
    public void joinTestValueError()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.join(Result.value(Result.error(e))));
    }

    @Test
    public void joinTestError()
    {
        Exception e = new Exception();
        assertEquals(
                Result.error(e),
                Result.join(Result.error(e)));
    }

    @Test
    public void equalsTest()
    {
        IOException e = new IOException();

        assertEquals(Result.value(true), Result.value(true));
        assertNotEquals(Result.value(true), Result.value(false));
        assertNotEquals(Result.value(true), Result.error(e));

        assertEquals(Result.error(e), Result.error(e));
        assertNotEquals(Result.error(new IOException()), Result.error(new IOException()));
        assertNotEquals(Result.error(new IOException()), Result.error(new FileNotFoundException()));

        assertNotEquals(Result.value(true), Either.right(true));
    }
    @Test
    public void hashCodeTest()
    {
        IOException e = new IOException();

        assertEquals(Result.value(true).hashCode(), Result.value(true).hashCode());
        assertNotEquals(Result.value(true).hashCode(), Result.value(false).hashCode());
        assertNotEquals(Result.value(true).hashCode(), Result.error(e).hashCode());

        assertEquals(Result.error(e).hashCode(), Result.error(e).hashCode());
        assertNotEquals(Result.error(new IOException()).hashCode(), Result.error(new IOException()).hashCode());
        assertNotEquals(Result.error(new IOException()).hashCode(), Result.error(new FileNotFoundException()).hashCode());

        assertNotEquals(Result.value(true).hashCode(), Either.right(true).hashCode());
    }


    @Test
    public void toStringTest()
    {
        Double value = 2.0;
        Exception e = new Exception("message");
        assertThat(Result.error(e).toString(), containsString(e.toString()));
        assertThat(Result.value(value).toString(), containsString(value.toString()));
    }
}
