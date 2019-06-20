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
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static functional.algebraic.Either.left;
import static functional.algebraic.Either.right;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class EitherTest
{
    @Test
    public void bimapTestLeft()
    {
        assertEquals(left(0).bimap(l -> l == 0, r -> false), left(true));
    }

    @Test
    public void bimapTestRight()
    {
        assertEquals(right(0).bimap(l -> false, r -> r == 0), right(true));
    }

    @Test
    public void hashCodeTest()
    {
        assertEquals(left(0).hashCode(), left(0).hashCode());
        assertEquals(right(0).hashCode(), right(0).hashCode());
        assertNotEquals(left(0).hashCode(), right(0).hashCode());
        assertNotEquals(right(0).hashCode(), left(0).hashCode());
        assertNotEquals(left(0).hashCode(), left(1).hashCode());
        assertNotEquals(right(0).hashCode(), right(1).hashCode());
    }

    @Test
    public void equalsTest()
    {
        assertEquals(left(0), left(0));
        assertEquals(right(0), right(0));
        assertNotEquals(left(0), right(0));
        assertNotEquals(right(0), left(0));
        assertNotEquals(left(0), left(1));
        assertNotEquals(right(0), right(1));
    }

    @Test
    public void fromLeftTestLeft()
    {
        assertTrue(left(true).fromLeft(false));
    }

    @Test
    public void fromLeftTestRight()
    {
        assertFalse((Boolean) right(true).fromLeft(false));
    }

    @Test
    public void fromRightTestLeft()
    {
        assertFalse((Boolean) left(true).fromRight(false));
    }

    @Test
    public void fromRightTestRight()
    {
        assertTrue(right(true).fromRight(false));
    }

    @Test
    public void ifLeftTestLeft()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        left(true).ifLeft(l -> changed.set(l));
        assertTrue(changed.get());
    }

    @Test
    public void ifLeftTestRight()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        Either.<Boolean, Boolean>right(true).ifLeft(l -> changed.set(l));
        assertFalse(changed.get());
    }

    @Test
    public void ifRightTestLeft()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        Either.<Boolean, Boolean>left(true).ifRight(r -> changed.set(r));
        assertFalse(changed.get());
    }

    @Test
    public void ifRightTestRight()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        right(true).ifRight(r -> changed.set(r));
        assertTrue(changed.get());
    }

    @Test
    public void isLeftTestLeft()
    {
        assertTrue(left(0).isLeft());
    }

    @Test
    public void isLeftTestRight()
    {
        assertFalse(right(0).isLeft());
    }

    @Test
    public void isRightTestLeft()
    {
        assertFalse(left(0).isRight());
    }

    @Test
    public void isRightTestRight()
    {
        assertTrue(right(0).isRight());
    }

    @Test
    public void matchTestProducingLeft()
    {
        assertTrue(left(0).matchThen(
                l -> l == 0,
                r -> false)
        );
    }

    @Test
    public void matchTestProducingRight()
    {
        assertTrue(right(0).matchThen(
                l -> false,
                r -> r == 0)
        );
    }

    @Test
    public void matchTestVoidLeft()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        left(true).match(
                l -> changed.set(l),
                r -> {}
                );

        assertTrue(changed.get());
    }

    @Test
    public void matchTestVoidRight()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        right(true).match(
                l -> {},
                r -> changed.set(r)
        );

        assertTrue(changed.get());
    }

    @Test
    public void unsafeMatchTestProducingLeft()
    {
        assertTrue(left(0).unsafeMatchThen(
                l -> l == 0,
                r -> {throw new AssertionFailedError("Wrong branch taken");}
        ));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingLeftError() throws IOException
    {
        ThrowingFunction<Integer, ?, IOException> left = l -> { throw new IOException("This is the right path"); };
        ThrowingFunction<Object, ?, IOException> right = r -> { throw new AssertionFailedError("Wrong branch taken"); };
        left(0).unsafeMatchThen(left, right);
    }

    @Test
    public void unsafeMatchTestProducingRight()
    {
        assertTrue(right(0).unsafeMatchThen(
                l -> {throw new AssertionFailedError("Wrong branch taken");},
                r -> r == 0
        ));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingRightError() throws IOException
    {
        ThrowingFunction<Object, ?, IOException> left = l -> { throw new AssertionFailedError("Wrong branch taken"); };
        ThrowingFunction<Integer, ?, IOException> right = r -> { throw new IOException("This is the right path"); };
        right(0).unsafeMatchThen(left, right);
    }

    @Test
    public void unsafeMatchTestVoidLeft() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        ThrowingConsumer<Boolean, IOException> left = l -> changed.set(l);
        ThrowingConsumer<Object, IOException> right = r -> { throw new AssertionFailedError("Wrong branch taken"); };

        left(true).unsafeMatch(
                left,
                right
        );

        assertTrue(changed.get());
    }
   
    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidLeftError() throws IOException
    {
        ThrowingFunction<Integer, ?, IOException> left = l -> { throw new IOException("This is the right path"); };
        ThrowingFunction<Object, ?, IOException> right = r -> { throw new AssertionFailedError("Wrong branch taken"); };
        left(0).unsafeMatchThen(left, right);
    }

    @Test
    public void unsafeMatchTestVoidRight() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        ThrowingConsumer<Object, IOException> left = l -> { throw new AssertionFailedError("Wrong branch taken"); };
        ThrowingConsumer<Boolean, IOException> right = r -> changed.set(r);

        right(true).unsafeMatch(
                left,
                right
        );

        assertTrue(changed.get());
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidRightError() throws IOException
    {
        ThrowingFunction<Object, ?, IOException> left = l -> { throw new AssertionFailedError("Wrong branch taken"); };
        ThrowingFunction<Integer, ?, IOException> right = r -> { throw new IOException("This is the right path"); };
        right(0).unsafeMatchThen(left, right);
    }

    @Test
    public void toStringTest()
    {
        String value = "hello";
        assertThat(Either.right(value).toString(), containsString(value.toString()));
        assertThat(Either.left(value).toString(), containsString(value.toString()));
    }
}