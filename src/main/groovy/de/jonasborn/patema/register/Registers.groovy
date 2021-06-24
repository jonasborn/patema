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

package de.jonasborn.patema.register

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.google.common.io.BaseEncoding
import com.google.common.primitives.Ints
import com.rockaport.alice.Alice
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContextBuilder

class Registers {

    static Alice v1Alice
    static Kryo v1kryo

    static {
        v1Alice = new Alice(new AliceContextBuilder()
                .setAlgorithm(AliceContext.Algorithm.AES)
                .setKeyLength(AliceContext.KeyLength.BITS_256)
                .setPadding(AliceContext.Padding.PKCS5_PADDING)
                .build())

        v1kryo = new Kryo()
        v1kryo.register(V1Register.class)
        v1kryo.register(V1RegisterEntry.class)
        v1kryo.register(LinkedList.class)
        v1kryo.register(byte[].class)
    }

    // ----- ----- ----- PACK ----- ----- -----

    //Could be more performant, use arrays in the if clauses if needed
    public static byte[] pack(Register register, String password) {
        ByteArrayOutputStream bout
        if (register instanceof V1Register) {
            byte[] data = packV1(register, password)
            bout = new ByteArrayOutputStream(data.length + 1)
            bout.write(Ints.toByteArray(data.length)) //Data might be padded
            bout.write([1] as byte[])
            bout.write(data)
        }

        if (bout == null) throw new UnsupportedRegisterException("Register ${register.class} is not supported")
        return bout.toByteArray()
    }

    private static byte[] packV1(V1Register register, String password) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream()
            Output output = new Output(bout)
            v1kryo.writeObject(output, register)
            output.close()
            return v1Alice.encrypt(bout.toByteArray(), password.toCharArray())
        } catch (Exception e) {
            throw new RegisterException("Unable to encrypt register", e)
        }
    }

    // ----- ----- ----- UNPACK ----- ----- -----

    public static Register unpack(byte[] data, String password) {
        def bin = new ByteArrayInputStream(data)
        int length = Ints.fromByteArray(bin.readNBytes(4))
        int version = (int) bin.readNBytes(1)[0]
        byte[] rest = bin.readNBytes(length)

        switch (version) {
            case 1:
                return unpackV1(rest, password)
                break
        }
        throw new UnsupportedRegisterException("Register not supported")
    }

    private static V1Register unpackV1(byte[] data, String password) {
        try {
            Input input = new Input(v1Alice.decrypt(data, password.toCharArray()))
            return v1kryo.readObject(input, V1Register.class)
        } catch (Exception e) {
            throw new RegisterException("Unable to encrypt register", e)
        }
    }

    public static class UnsupportedRegisterException extends IOException {
        UnsupportedRegisterException(String message) {
            super(message)
        }
    }

    public static class RegisterException extends IOException {
        RegisterException(String message) {
            super(message)
        }

        RegisterException(String message, Throwable cause) {
            super(message, cause)
        }
    }


}
