/*
 * Copyright 2021 Jonas Born
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

package de.jonasborn.patema

import de.jonasborn.patema.util.RandomUtils
import org.bouncycastle.pqc.math.linearalgebra.RandUtils
import org.junit.Test

class RandomUtilsTest {

    @Test
    public void testBytesSameLength() {
        def a = RandomUtils.nextBytes(123)
        def b = RandomUtils.nextBytes(123)
        assert a.length == b.length
        assert a.length == 123
        assert b.length == 123
    }

    @Test
    public void testBytesDifferentLength() {
        def a = RandomUtils.nextBytes(123)
        def b = RandomUtils.nextBytes(321)
        assert a.length != b.length
        assert a.length == 123
        assert b.length == 321
    }

    @Test
    public void testFileSameLength() {
        def a = RandomUtils.nextFile(new File("."), 123)
        def b = RandomUtils.nextFile(new File("."), 123)

        assert a.length() == b.length()
        assert a.length() == 123
        assert b.length() == 123
    }

}
