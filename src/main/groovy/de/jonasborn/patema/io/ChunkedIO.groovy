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

package de.jonasborn.patema.io

import com.google.common.io.ByteStreams
import com.rockaport.alice.Alice
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContextBuilder
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZInputStream
import org.tukaani.xz.XZOutputStream

class ChunkedIO {

    Alice alice = new Alice(new AliceContextBuilder()
            .setAlgorithm(AliceContext.Algorithm.AES)
            .setMode(AliceContext.Mode.CTR) // or AliceContext.Mode.CBC
            .setIvLength(16)
            .setKeyLength(AliceContext.KeyLength.BITS_256)
            .build())

    ChunkedIOConfig config;

    ChunkedIO(ChunkedIOConfig config) {
        this.config = config
    }

    public byte[] encode(byte[] data) {
        if (!config.compress && !config.encrypt) return data
        def bout = new ByteArrayOutputStream()
        def out = new XZOutputStream(bout, new LZMA2Options())
        out.write(data)
        out.flush()
        out.finish()
        out.close()
        return alice.encrypt(bout.toByteArray(), config.password.toCharArray())
    }

    public byte[] decode(byte[] data) {
        if (!config.compress && !config.encrypt) return data
        def dec = alice.decrypt(data, config.password.toCharArray())
        def bin = new ByteArrayInputStream(dec)
        def xin = new XZInputStream(bin)
        def bout = new ByteArrayOutputStream()
        ByteStreams.copy(xin, bout)
        bin.close()
        xin.close()
        return bout.toByteArray()
    }


}
