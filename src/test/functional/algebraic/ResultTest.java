/*
 * Copyright 2018 Zoey Hewll
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

import functional.throwing.ThrowingFunction;
import functional.throwing.ThrowingRunnable;
import functional.throwing.ThrowingSupplier;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
    public void thenTestValueValue()
    {
        assertEquals(Result.value(false).then(() -> Result.value(true)), Result.value(true));
    }

    @Test
    public void thenTestValueError()
    {
        IOException e = new IOException();
        assertEquals(Result.value(false).then(() -> Result.error(e)), Result.error(e));
    }

    @Test
    public void thenTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).then(() -> Result.value(true)), Result.error(e));
    }

    @Test
    public void thenTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).then(() -> Result.error(new IOException())), Result.error(e));
    }

    @Test
    public void peekTestValueValue()
    {
        assertEquals(Result.value(false).peek(b -> Result.value(!b)), Result.value(false));
    }

    @Test
    public void peekTestValueError()
    {
        IOException e = new IOException();
        assertEquals(Result.value(false).peek(b -> Result.error(e)), Result.error(e));
    }

    @Test
    public void peekTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).peek(o -> Result.value(o)), Result.error(e));
    }

    @Test
    public void peekTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).peek(o -> Result.error(new FileNotFoundException())), Result.error(e));
    }

    @Test
    public void setTestValueValue()
    {
        assertEquals(Result.value(false).set(Result.value(true)), Result.value(true));
    }

    @Test
    public void setTestValueError()
    {
        IOException e = new IOException();
        assertEquals(Result.value(false).set(Result.error(e)), Result.error(e));
    }

    @Test
    public void setTestErrorValue()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).set(Result.value(true)), Result.error(e));
    }

    @Test
    public void setTestErrorError()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).set(Result.error(new FileNotFoundException())), Result.error(e));
    }

    @Test
    public void setVTestValue()
    {
        assertEquals(Result.value(false).setV(true), Result.value(true));
    }

    @Test
    public void setVTestError()
    {
        IOException e = new IOException();
        assertEquals(Result.error(e).setV(true), Result.error(e));
    }

    @Test
    public void mapTestValue()
    {
        assertEquals(Result.value(true).map(t -> !t), Result.value(false));
    }

    @Test
    public void mapTestError()
    {
        IOException e = new IOException();
        assertEquals(Result.<IOException, Boolean>error(e).map(t -> !t), Result.error(e));
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
        assertEquals(Result.of(() -> {}), Result.value(null));
    }

    @Test
    public void ofTestRunnableError()
    {
        IOException e = new IOException();
        assertEquals(Result.of((ThrowingRunnable<IOException>) () -> {throw e;}), Result.error(e));
    }

    @Test
    public void ofTestSupplierValue()
    {
        assertEquals(Result.of(() -> true), Result.value(true));
    }

    @Test
    public void ofTestSupplierError()
    {
        IOException e = new IOException();
        assertEquals(Result.of((ThrowingSupplier<?, IOException>) () -> {throw e;}), Result.error(e));
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
        assertEquals(Result.ofRuntime((Runnable) () -> {throw e;}), Result.error(e));
    }

    @Test
    public void ofRuntimeTestSupplierValue()
    {
        assertEquals(Result.ofRuntime(() -> true), Result.value(true));
    }

    @Test
    public void ofRuntimeTestSupplierError()
    {
        RuntimeException e = new IllegalArgumentException();
        assertEquals(Result.ofRuntime((Supplier<?>) () -> {throw e;}), Result.error(e));
    }

    @Test
    public void convertTestValue()
    {
        ThrowingFunction<Boolean,Boolean, IOException> f = t -> t;
        assertEquals(Result.convert(f).apply(true), Result.value(true));
    }

    @Test
    public void convertTestError()
    {
        IOException e = new IOException();
        ThrowingFunction<Boolean,?, IOException> f = t -> {throw e;};
        assertEquals(Result.convert(f).apply(true), Result.error(e));
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
    }
}
