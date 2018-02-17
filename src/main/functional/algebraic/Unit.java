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

/**
 * A class representing the unit type.
 * This class has a single value.
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
}
