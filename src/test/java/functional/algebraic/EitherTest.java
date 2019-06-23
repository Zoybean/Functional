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

import functional.combinator.Combinators;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static functional.algebraic.Either.left;
import static functional.algebraic.Either.right;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class EitherTest
{

    public static Error error = new AssertionError("Wrong branch taken");
    public static IOException expected = new IOException("This is the right branch");

    @Test
    public void bimapTest()
    {
        assertEquals(left(0).bimap(l -> l == 0, r -> false), left(true));
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
    public void fromLeftTest()
    {
        assertTrue(left(true).fromLeft(false));
        assertFalse((Boolean) right(true).fromLeft(false));
    }

    @Test
    public void fromRightTest()
    {
        assertFalse((Boolean) left(true).fromRight(false));
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
    public void isLeftTest()
    {
        assertTrue(left(0).isLeft());
        assertFalse(right(0).isLeft());
    }

    @Test
    public void isRightTest()
    {
        assertFalse(left(0).isRight());
        assertTrue(right(0).isRight());
    }

    @Test
    public void matchTestProducing()
    {
        assertTrue(left(0).matchThen(
                l -> l == 0,
                r -> false)
        );
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
                Combinators::noop
        );

        assertTrue(changed.get());
    }

    @Test
    public void matchTestVoidRight()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        right(true).match(
                Combinators::noop,
                r -> changed.set(r)
        );

        assertTrue(changed.get());
    }

    @Test
    public void unsafeMatchTestProducingLeft()
    {
        assertTrue(left(0).unsafeMatchThen(
                l -> l == 0,
                r -> toss(error)
        ));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingLeftError() throws IOException
    {
        left(0).<Integer, IOException>unsafeMatchThen(
                l -> toss(expected),
                r -> toss(error));
    }

    @Test
    public void unsafeMatchTestProducingRight()
    {
        assertTrue(right(0).unsafeMatchThen(
                l -> toss(error),
                r -> r == 0
        ));
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestProducingRightError() throws IOException
    {
        right(0).<Integer, IOException>unsafeMatchThen(
                l -> toss(error),
                r -> toss(expected)
        );
    }

    @Test
    public void unsafeMatchTestVoidLeft() throws IOException
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        left(true).unsafeMatch(
                l -> changed.set(l),
                r -> toss(error)
        );

        assertTrue(changed.get());
    }
   
    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidLeftError() throws IOException
    {
        left(0).<Integer, IOException>unsafeMatchThen(
                l -> toss(expected),
                r -> toss(error));
    }

    @Test
    public void unsafeMatchTestVoidRight()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        right(true).unsafeMatch(
                l -> toss(error),
                r -> changed.set(r));

        assertTrue(changed.get());
    }

    @Test(expected = IOException.class)
    public void unsafeMatchTestVoidRightError() throws IOException
    {
        right(0).<Integer, IOException>unsafeMatchThen(
                l -> toss(error),
                r -> toss(expected));
    }

    @Test
    public void collapseTestProducing()
    {
        assertEquals(
                ((Integer)1).toString(),
                left(1).collapseThen(Object::toString));
        assertEquals(
                ((Boolean)false).toString(),
                right(false).collapseThen(Object::toString));
    }

    @Test
    public void collapseTest()
    {
        left(1).collapse(o -> assertEquals(1, o));
        right(true).collapse(o -> assertEquals(true, o));
    }

    @Test
    public void flipTest()
    {
        assertEquals(right(1), left(1).flip());
        assertEquals(left(1), right(1).flip());
    }

    @Test
    public void toStringTest()
    {
        String value = "hello";
        assertThat(Either.right(value).toString(), containsString(value.toString()));
        assertThat(Either.left(value).toString(), containsString(value.toString()));
    }
}