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

import com.sun.istack.internal.Nullable;
import functional.throwing.ThrowingConsumer;
import functional.throwing.ThrowingSupplier;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>A container which may be empty, or hold a value; and provides pattern matching to safely unwrap the data.</p>
 *
 * <p>The type is sealed with a private constructor to ensure the only subclasses are {@link Nothing} and {@link Just}.</p>
 *
 * @param <V> The type of the optional value
 * @author Zoey Hewll
 */
public abstract class Maybe<V> implements ThrowingSupplier<V, IllegalStateException>
{
  /**
   * Private constructor to seal the type.
   */
  private Maybe() {}

  /**
   * Effectively final, singleton instance.
   */
  private static Nothing<?> NOTHING;

  /**
   * Perform pattern matching on the potential states of the container.
   * @param some the operation to perform on the contained value if there is one
   * @param none the operation to perform if there is no contained value
   * @param <T> the type of the value returned by the supplied operations
   * @return the result of the matched operation
   */
  public abstract <T> T match(Function<? super V, ? extends T> some, Supplier<? extends T> none);

  /**
   * Perform pattern matching on the potential states of the container.
   * @param some the operation to perform on the contained value if there is one
   * @param none the operation to perform if there is no contained value
   */
  public abstract void match(Consumer<? super V> some, Runnable none);

  /**
   * Performs the operation on any contained value and returns a Maybe of the result.
   * @param f the function to apply to the contained value
   * @param <T> the type of the function's return value
   * @return the modified Maybe
   */
  public abstract <T> Maybe<T> map(Function<? super V, ? extends T> f);

  /**
   * Performs the operation on any contained value and returns the resulting Maybe if there is one.
   * @param f the function to apply to the contained value
   * @param <T> the type of the function's optional return value
   * @return the modified Maybe
   */
  public abstract <T> Maybe<T> bind(Function<? super V, ? extends Maybe<? extends T>> f);

  /**
   * Returns the contained value, if it exists, otherwise raise an exception.
   * @return The contained value, if it exists
   * @throws IllegalStateException if nothing is contained
   */
  public abstract V get() throws IllegalStateException;

  /**
   * Returns the contained value, or the supplied default if it does not exits.
   * @param value the default value to use
   * @return the contained value, or the supplied default if it does not exits
   */
  public V fromMaybe(V value)
  {
    return match(
        (V v) -> v,
        ()    -> value
    );
  }

  /**
   * Returns true if there is a contained value.
   * @return true if there is a contained value
   */
  public boolean isJust()
  {
    return match(
        (V v) -> true,
        ()    -> false
    );
  }

  /**
   * Converts a nullable value into an equivalent Maybe, returning Nothing if the value is null, and just(value) otherwise.
   * @param value the value to wrap
   * @return nothing() if the value is null, and just(value) otherwise
   */
  public Maybe<V> of(@Nullable V value)
  {
    return value == null
        ? nothing()
        : just(value);
  }

  /**
   * Turns an Optional into a Maybe, turning Empty into Nothing, and a present value into just(value).
   * @param value the optional to transform
   * @return A Maybe mirroring the Optional parameter
   */
  public Maybe<V> of(Optional<V> value)
  {
    return value.map(Maybe::just).orElse(nothing());
  }

  /**
   * Wraps the value in a Maybe.
   * @param value The value to wrap
   * @param <V> The type of the containted value
   * @return The value wrapped in a Maybe
   */
  public static <V> Maybe<V> just(V value)
  {
    return new Just<>(value);
  }

  /**
   * Returns the singleton Nothing instance.
   * @param <V> The type of the non-existent contained value
   * @return The singleton Nothing instance
   */
  public static <V> Maybe<V> nothing()
  {
    if (NOTHING == null)
    {
       NOTHING = new Nothing<>();
    }
    @SuppressWarnings("unchecked") Nothing<V> nothing = (Nothing<V>) NOTHING;
    return nothing;
  }

  /**
   * Upcast the container by upcasting the contained type.
   * @param m The Maybe to cast
   * @param <T> The type to cast to
   * @param <V> The type to cast from
   * @return A Maybe of the upcast type
   */
  private static <T, V extends T> Maybe<T> cast(Maybe<V> m)
  {
    return m.match(
        (T value) -> just(value),
        ()        -> nothing()
    );
  }

  /**
   * The class representing an absent value.
   * @param <V> unused
   */
  private static class Nothing<V> extends Maybe<V>
  {
    @Override
    public <T> T match(Function<? super V, ? extends T> some, Supplier<? extends T> none)
    {
      return none.get();
    }

    @Override
    public void match(Consumer<? super V> some, Runnable none)
    {
      none.run();
    }

    @Override
    public <T> Maybe<T> map(Function<? super V, ? extends T> f)
    {
      return nothing();
    }

    @Override
    public <T> Maybe<T> bind(Function<? super V, ? extends Maybe<? extends T>> f)
    {
      return nothing();
    }

    @Override
    public V get() throws IllegalStateException
    {
      throw new IllegalStateException();
    }
  }

  /**
   * The class representing a present value.
   * @param <V> The type of the contained value.
   */
  private static class Just<V> extends Maybe<V>
  {
    /**
     * The contained value.
     */
    final V value;

    /**
     * Construct a Maybe with the provided value.
     * @param value The value to wrap.
     */
    Just(V value)
    {
      this.value = value;
    }

    @Override
    public <T> T match(Function<? super V, ? extends T> some, Supplier<? extends T> none)
    {
      return some.apply(value);
    }

    @Override
    public void match(Consumer<? super V> some, Runnable none)
    {
      some.accept(value);
    }

    @Override
    public <T> Maybe<T> map(Function<? super V, ? extends T> f)
    {
      return just(f.apply(value));
    }

    @Override
    public <T> Maybe<T> bind(Function<? super V, ? extends Maybe<? extends T>> f)
    {
      return cast(f.apply(value));
    }

    @Override
    public V get()
    {
      return value;
    }
  }
}
