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

package functional.algebraic.utils;

import functional.combinator.Combinators;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static functional.combinator.Combinators.toss;

import static org.junit.Assert.*;
import static functional.algebraic.utils.Voids.*;

public class VoidsTest
{
    public static Error error = new AssertionError("Wrong branch taken");
    public static IOException expected = new IOException("This is the right branch");

    @Test
    public void convertTestRunnable()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        convert(() -> changed.set(true)).get();

        assertTrue(changed.get());
    }

    @Test
    public void convertTestConsumer()
    {
        final AtomicBoolean changed = new AtomicBoolean(false);

        convert(changed::set).apply(true);

        assertTrue(changed.get());
    }

    @Test(expected = IOException.class)
    public void convertUnsafeTestRunnable() throws IOException
    {
        convertUnsafe(() -> toss(expected)).get();
    }

    @Test(expected = IOException.class)
    public void convertUnsafeTestConsumer() throws IOException
    {
        Voids.<IOException, IOException>convertUnsafe(Combinators::toss).apply(expected);
    }
}
