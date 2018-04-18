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

import functional.throwing.ThrowingConsumer;
import functional.throwing.ThrowingFunction;
import functional.throwing.ThrowingRunnable;
import functional.throwing.ThrowingSupplier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class representing the unit type.
 * This class has a single value, and is used as a placeholder for data that would usually be present,
 * but has been omitted.
 * Returning Unit is equivalent to returning void, but not Void.
 *
 * @author Zoey Hewll
 */
public final class Unit
{
    /**
     * The single value of the Unit type.
     */
    public static final Unit unit = new Unit();

    /**
     * The only constructor for the Unit type.
     */
    private Unit() {}

    /**
     * Turns a function returning void into a function returning Unit.
     *
     * @param f   The function to convert
     * @param <E> The parameter type of the function
     * @return An equivalent function that returns Unit instead of void
     */
    public static <E> Function<E, Unit> convert(Consumer<E> f)
    {
        return convertUnsafe(f::accept)::apply;
    }

    /**
     * Turns a function returning void into a function returning Unit.
     *
     * @param f The function to convert
     * @return An equivalent function that returns Unit instead of void
     */
    public static Supplier<Unit> convert(Runnable f)
    {
        return convertUnsafe(f::run)::get;
    }

    /**
     * Turns a function returning void into a function returning Unit.
     *
     * @param f   The function to convert
     * @param <T> The parameter type of the function
     * @return An equivalent function that returns Unit instead of void
     */
    public static <T, E extends Exception> ThrowingFunction<T, Unit, E> convertUnsafe(ThrowingConsumer<T, E> f)
    {
        return f.andThen(() -> unit);
    }

    /**
     * Turns a function returning void into a function returning Unit.
     *
     * @param f The function to convert
     * @return An equivalent function that returns Unit instead of void
     */
    public static <E extends Exception> ThrowingSupplier<Unit, E> convertUnsafe(ThrowingRunnable<E> f)
    {
        return f.andThen(() -> unit);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Unit;
    }

    @Override
    public int hashCode()
    {
        return 0;
    }
}
