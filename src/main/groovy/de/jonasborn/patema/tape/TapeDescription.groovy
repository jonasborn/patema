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

package de.jonasborn.patema.tape

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding

import java.nio.charset.Charset

class TapeDescription {

    static Kryo kryo

    static {
        kryo = new Kryo()
        kryo.register(TapeDescription.class)
        kryo.register(LinkedList.class)
        kryo.register(DescriptionFile.class)
        kryo.register(byte[].class)
    }

    public static byte[] pack(TapeDescription description) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        Output output = new Output(bout)
        kryo.writeObject(output, description)
        output.close()
        return bout.toByteArray()
    }

    public static TapeDescription unpack(byte[] data) {
        Input input = new Input(data)
        kryo.readObject(input, TapeDescription.class)
    }

    String id
    LinkedList<DescriptionFile> files = []
    
    public void add(String name, byte[] md5, long length) {
        files.add(new DescriptionFile(name, md5, length))
    }

    public static class DescriptionFile {
        String name
        byte[] hash
        long length

        DescriptionFile(String name, byte[] hash, long length) {
            this.name = name
            this.hash = hash
            this.length = length
        }
    }

    static void main(String[] args) {
        def des = new TapeDescription()
        des.add("Hallo.text", Hashing.md5().hashString("12345", Charset.defaultCharset()).asBytes(), 123)
        println BaseEncoding.base64().encode(pack(des))
    }


}
