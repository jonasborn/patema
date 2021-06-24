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

import de.jonasborn.patema.ios.endecode.Encoder
import de.jonasborn.patema.ios.endecode.implementation.AESECBOnLZMA2
import de.jonasborn.patema.util.RandomUtils
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AESECBOnLZMA2EncodeDecodeTest {

    @Test

    public void aEncrypt() {
        Encoder encoder = new AESECBOnLZMA2("hallo", new byte[16], new byte[16])
        byte[] data = "Hallo World, 12345".getBytes("UTF-8")
        def enc = encoder.encode(0, data)
    }

    @Test
    public void bDecrypt() {
        def content = RandomUtils.nextBytes(RandomUtils.nextInt(8, 1024 * 8))
        Encoder encoder = new AESECBOnLZMA2("hallo", new byte[16], new byte[16])

        def enc = encoder.encode(0, content)
        def dec = encoder.decode(0, enc)
        assert dec == content
    }

    @Test
    public void cLoop() {
        def content = RandomUtils.nextBytes(RandomUtils.nextInt(8, 1024 * 8))
        Encoder encoder = new AESECBOnLZMA2("hallo", new byte[16], new byte[16])

        for (i in 0..<100) {
            int index = RandomUtils.nextInt(0, 1024 * 8)
            println "Testing index $index"
            def enc = encoder.encode(index, content)
            def dec = encoder.decode(index, enc)
            assert content == dec
        }
    }

    @Test
    public void dLoppThenLoop() {
        def content = RandomUtils.nextBytes(RandomUtils.nextInt(8, 1024 * 8))
        Encoder encoder = new AESECBOnLZMA2("hallo", new byte[16], new byte[16])

        List<byte[]> encs = []

        for (i in 0..<100) {
            println "Generating index $i"
            encs.add(encoder.encode(i, content))
        }

        for (i in 0..<100) {
            println "Testing index $i"
            def dec = encoder.decode(i, encs[i])
            assert content == dec
        }

    }

}
