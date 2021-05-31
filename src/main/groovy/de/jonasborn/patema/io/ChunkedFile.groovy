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

import com.google.common.hash.Hashing
import com.google.common.io.Files

import java.nio.ByteBuffer

class ChunkedFile {

    File directory;
    Long position = 0
    int blockSize = 1024 * 1024
    ChunkedIO io = new ChunkedIO()

    private File splinterFile(int index) {
        return new File(directory, index + ".ptma")
    }

    ChunkedFile(File directory) {
        this.directory = directory
    }

    public byte[] read(int total) {
        def bout = new ByteArrayOutputStream(total) //Target to write to
        int index = (position / blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        if (!file.exists()) return new byte[0]
        def read = 0
        def data = io.decode(file.bytes) //Load the block and decode using io
        int startOfFile = (position - (index * blockSize)) as int
        int length = (blockSize - startOfFile) as int
        bout.write(data, startOfFile, length) //write the content to the output
        read += length //Update the length
        while (read < total) {
            index++ //Focus the next file
            file = splinterFile(index)
            if (!file.exists()) return bout.toByteArray() //Could return a index out of bounce
            data = io.decode(file.bytes) //Load the block and decode using io
            startOfFile = 0
            length = (total - read < blockSize) ? total - read : blockSize
            bout.write(data, startOfFile, length) //write the content to the output
            read += length
        }

        return bout.toByteArray()
    }

    public void write(byte[] d) {
        int index = (position / blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        def written = 0;
        def data;
        if (file.exists()) data = ByteBuffer.wrap(io.decode(file.bytes))
        else data = ByteBuffer.allocate(blockSize)
        int startOfFile = (position - (index * blockSize)) as int
        int length = (blockSize - startOfFile) as int
        data.position(startOfFile)
        data.put(d, 0, length)
        byte[] toWrite = new byte[blockSize]
        data.rewind()
        data.get(toWrite)
        file.setBytes(io.encode(toWrite))
        written += length

        while (d.length > written) {
            index++
            file = splinterFile(index)
            if (file.exists()) data = ByteBuffer.wrap(io.decode(file.bytes))//PatemaIO.decompress(file.bytes))
            else data = ByteBuffer.allocate(blockSize)
            startOfFile = 0;
            length = (d.length - written < blockSize) ? d.length - written : blockSize
            data.put(d, written, length)
            toWrite = new byte[blockSize]
            data.rewind()
            data.get(toWrite)
            //file.setBytes(PatemaIO.compress(toWrite, 0, blockSize))
            file.setBytes(io.encode(toWrite))
            written += length
        }
    }

    public void finish() {
        def list = directory.listFiles()
        long totalLength = (list.size() - 1) * blockSize
        def last = io.decode(list.last().bytes).length
        totalLength += last

        def d = new ChunkedFileDescription(
                list.sum { it.length() } as long,
                blockSize,
                totalLength,
                directory.name
        )
        ChunkedFileDescription.write(d, new File(directory, "description.ptma"))
    }

    public static void main(String[] args) {
        def f = new ChunkedFile(new File("test"))
        def input = new File("test.exe");
        def output = new File("textr.exe")

        f.write(input.bytes)
        output.setBytes(f.read(input.length() as int))

        println Files.hash(input, Hashing.md5())
        println Files.hash(output, Hashing.md5())
        println input.size()
        println new File("test").listFiles().sum { it.length() }
        f.finish()
    }
}
