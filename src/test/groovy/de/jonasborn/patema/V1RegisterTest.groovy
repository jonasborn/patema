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

import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import de.jonasborn.patema.register.V1Register
import de.jonasborn.patema.register.V1RegisterEntry
import org.junit.Test

class V1RegisterTest {

    @Test
    public void pack() {
        def r = new V1Register()
        Registers.pack(r, UUID.randomUUID().toString())
    }

    @Test
    public void packUnpack() {
        def password = UUID.randomUUID().toString()
        def r = new V1Register()
        def packed = Registers.pack(r, password)
        def unpacked = Registers.unpack(packed, password)
        assert unpacked instanceof V1Register
    }

    @Test
    public void packFilled() {
        def r = new V1Register()
        r.addEntry(new V1RegisterEntry(
                "test", 0, new byte[32], 1234, 1235, "hallo"
        ))
        Registers.pack(r, UUID.randomUUID().toString())
    }

}
