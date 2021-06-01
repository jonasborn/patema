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

import com.esotericsoftware.kryo.Kryo

import java.nio.ByteBuffer

class ChunkedFileDescription {

    static Kryo kryo

    static {

    }

    public static byte[] pack(ChunkedFileDescription description) {
        def nameBytes = description.name.getBytes("UTF-8")
        def length = 20 + nameBytes.length
        def buffer = ByteBuffer.allocate(length)
        buffer.putLong(description.length)
        buffer.putInt(description.blockSize)
        buffer.putLong(description.totalLength)
        buffer.put(nameBytes)
        def out = new byte[length]
        buffer.rewind()
        buffer.get(out)
        return out
    }

    public static ChunkedFileDescription unpack(byte[] data) {
        def buffer = ByteBuffer.wrap(data)
        def length = buffer.getLong()
        def blockSize = buffer.getInt()
        def totalSize = buffer.getLong()
        def nameBytes = new byte[buffer.remaining()]
        buffer.get(nameBytes)
        def name = new String(nameBytes, "UTF-8")
        return new ChunkedFileDescription(length, blockSize, totalSize, name)
    }

    public static void write(ChunkedFileDescription description, File file) {
        file.setBytes(pack(description))
    }

    public static ChunkedFileDescription read(File file) {
        return unpack(file.bytes)
    }

    long length
    int blockSize
    long totalLength
    String name

    ChunkedFileDescription(long length, int blockSize, long totalLength, String name) {
        this.length = length
        this.blockSize = blockSize
        this.totalLength = totalLength
        this.name = name
    }

}
