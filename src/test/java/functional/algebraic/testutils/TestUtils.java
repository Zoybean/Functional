/*
 * Copyright 2020 Zoey Hewll
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

package functional.algebraic.testutils;

import functional.throwing.ThrowingRunnable;

public class TestUtils
{
    private TestUtils() {}

    /**
     * For when you need to expect an exception of a specific type, but you can't decide which.
     */
    public static class ExpectedException extends Exception {}

    /**
     * @param expected The class object of throwable type we're expecting to catch
     * @param runnable The operation we're expecting to fail
     * @param <E>      The type of throwable we're expecting to catch
     */
    public static <E extends Throwable> void assertThrows(Class<E> expected, ThrowingRunnable<E> runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Throwable t)
        {
            Class<? extends Throwable> actual = t.getClass();
            if(actual.isAssignableFrom(expected))
            {
                return;
            }
            throw new AssertionError("Wrong exception thrown"
                    + ", expected " + expected.getSimpleName()
                    + ", got " + actual.getSimpleName(), t);
        }
        throw new AssertionError("No exception thrown, expected " + expected.getSimpleName());
    }
}
