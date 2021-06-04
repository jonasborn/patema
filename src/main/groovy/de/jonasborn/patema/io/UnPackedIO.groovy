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
import de.jonasborn.patema.io.chunked.UnPackedFileConfig
import groovy.transform.CompileStatic
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZInputStream
import org.tukaani.xz.XZOutputStream

class UnPackedIO {

    /*
    Look, I'm not a security expert, but I'm quite sure, that AES is
    one of the most attacked algos out there. Therefore I'm also sure
    that I can annoy every body using random keys for every file part.
    Guessing one key used for all would make everything accessible, using this
    solution, it could take some time.
    Comment: This may be true, but where the hell are the keys stored?
    On the tape? Then you have to encrypt them, same problem as before.
    Therefore, quite useless I guess...
     */
    Map<String, String> keys = [:]

    Alice alice = new Alice(new AliceContextBuilder()
            .setAlgorithm(AliceContext.Algorithm.AES)
            .setMode(AliceContext.Mode.CTR) // or AliceContext.Mode.CBC
            .setIvLength(16)
            .setKeyLength(AliceContext.KeyLength.BITS_128)
            .build())

    UnPackedFileConfig config;

    UnPackedIO(UnPackedFileConfig config) {
        this.config = config
    }

    /*
    Quite ineffective, isn't it?
     */
    @CompileStatic
    public char[] getPassword(File file) {
        if (!config.annoying) return config.password.toCharArray()
        def value = keys.get(file.name)
        if (value == null) {
            value = UUID.randomUUID().toString()
            keys.put(file.name, value)
        }
        return value.toCharArray()
    }

    @CompileStatic
    public byte[] encode(File file, byte[] data) {
        if (!config.compress && !config.encrypt) return data

        if (config.compress && !config.encrypt) {
            def bout = new ByteArrayOutputStream()
            def out = new XZOutputStream(bout, new LZMA2Options())
            out.write()
            out.flush()
            out.finish()
            out.close()
            return bout.toByteArray()
        }

        def password = getPassword(file)

        if (config.encrypt && !config.compress) {
            return alice.encrypt(data, password)
        }

        def bout = new ByteArrayOutputStream()
        def out = new XZOutputStream(bout, new LZMA2Options())
        out.write(data)
        out.flush()
        out.finish()
        out.close()
        return alice.encrypt(bout.toByteArray(), password)
    }

    @CompileStatic
    public byte[] decode(File file, byte[] data) {
        if (!config.compress && !config.encrypt) return data

        if (config.compress && !config.encrypt) {
            def bin = new ByteArrayInputStream(data)
            def xin = new XZInputStream(bin)
            def bout = new ByteArrayOutputStream()
            ByteStreams.copy(xin, bout)
            xin.close()
            return bout.toByteArray()
        }

        def password = getPassword(file)

        if (config.encrypt && !config.compress) {
            return alice.decrypt(data, password)
        }

        def dec = alice.decrypt(data, password)
        def bin = new ByteArrayInputStream(dec)
        def xin = new XZInputStream(bin)
        def bout = new ByteArrayOutputStream()
        ByteStreams.copy(xin, bout)
        bin.close()
        xin.close()
        return bout.toByteArray()
    }


}
