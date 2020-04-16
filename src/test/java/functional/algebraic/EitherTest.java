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

import functional.algebraic.testutils.TestUtils;
import functional.combinator.Combinators;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static functional.algebraic.Either.left;
import static functional.algebraic.Either.right;
import static functional.algebraic.testutils.TestUtils.*;
import static functional.combinator.Combinators.toss;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class EitherTest
{

    public static Error             error    = new AssertionError("Wrong branch taken");
    public static ExpectedException expected = new ExpectedException();

    @Test
    public void bimapTest()
    {
        assertEquals(left(0).bimap(l -> l == 0, r -> false), left(true));
        assertEquals(right(0).bimap(l -> false, r -> r == 0), right(true));
    }

    @Test
    public void mapLeftTest()
    {
        assertEquals(left(0).mapLeft(l -> l == 0), left(true));
        assertEquals(right(0).mapLeft(l -> false), right(0));
    }

    @Test
    public void mapRightTest()
    {
        assertEquals(left(0).mapRight(r -> false), left(0));
        assertEquals(right(0).mapRight(r -> r == 0), right(true));
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
    public void leftOrTest()
    {
        assertTrue(left(true).leftOr(false));
        assertFalse((Boolean) right(true).leftOr(false));
    }

    @Test
    public void rightOrTest()
    {
        assertFalse((Boolean) left(true).rightOr(false));
        assertTrue(right(true).rightOr(false));
    }
    @Test
    public void leftOrElseTest()
    {
        assertTrue(left(true).leftOrElse(() -> false));
        assertFalse((Boolean) right(true).leftOrElse(() -> false));
    }

    @Test
    public void rightOrElseTest()
    {
        assertFalse((Boolean) left(true).rightOrElse(() -> false));
        assertTrue(right(true).rightOrElse(() -> false));
    }


    @Test
    public void leftAndThenTest()
    {
        assertEquals(left(true), left(0).leftAndThen(l -> left(l == 0)));
        assertEquals(right(true), left(0).leftAndThen(l -> right(l == 0)));
        assertEquals(right(true), right(true).leftAndThen(l -> left(false)));
    }

    @Test
    public void rightAndThenTest()
    {
        assertEquals(right(true), right(0).rightAndThen(r -> right(r == 0)));
        assertEquals(left(true), right(0).rightAndThen(r -> left(r == 0)));
        assertEquals(left(true), left(true).rightAndThen(r -> right(false)));
    }

    @Test
    public void fromLeftTest()
    {
        assertEquals(Option.some(true), left(true).fromLeft());
        assertEquals(Option.none(), right(true).fromLeft());
    }

    @Test
    public void fromRightTest()
    {
        assertEquals(Option.some(true), right(true).fromRight());
        assertEquals(Option.none(), left(true).fromRight());
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

    @Test
    public void unsafeMatchTestProducingLeftError()
    {
        assertThrows(
                ExpectedException.class,
                () -> left(0).<Integer, ExpectedException>unsafeMatchThen(
                        l -> toss(expected),
                        r -> toss(error)));
    }

    @Test
    public void unsafeMatchTestProducingRight()
    {
        assertTrue(right(0).unsafeMatchThen(
                l -> toss(error),
                r -> r == 0));
    }

    @Test
    public void unsafeMatchTestProducingRightError()
    {
        assertThrows(ExpectedException.class,
                     () -> right(0).<Integer, ExpectedException>unsafeMatchThen(
                             l -> toss(error),
                             r -> toss(expected)));
    }

    @Test
    public void unsafeMatchTestVoidLeft()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        left(true).unsafeMatch(
                l -> changed.set(l),
                r -> toss(error)
        );

        assertTrue(changed.get());
    }
   
    @Test
    public void unsafeMatchTestVoidLeftError()
    {
        assertThrows(
                ExpectedException.class,
                () -> left(0).<Integer, ExpectedException>unsafeMatchThen(
                        l -> toss(expected),
                        r -> toss(error)));
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

    @Test
    public void unsafeMatchTestVoidRightError()
    {
        assertThrows(
                ExpectedException.class,
                () -> right(0).<Integer, ExpectedException>unsafeMatchThen(
                        l -> toss(error),
                        r -> toss(expected)));
    }

    @Test
    public void collapseTestProducing()
    {
        assertEquals(
                ((Integer)1).toString(),
                Either.collapseThen(left(1), Object::toString));
        assertEquals(
                ((Boolean)false).toString(),
                Either.collapseThen(right(false), Object::toString));
    }

    @Test
    public void collapseTest()
    {
        Either.collapse(left(1), o -> assertEquals(1, (int) o));
        Either.collapse(right(true), o -> assertEquals(true, o));
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