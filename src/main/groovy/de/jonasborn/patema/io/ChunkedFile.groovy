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
    ChunkedIO io

    private File splinterFile(int index) {
        return new File(directory, index + ".ptma")
    }

    ChunkedFile(ChunkedIOConfig config, File directory) {
        this.io = new ChunkedIO(config)
        this.directory = directory
    }

    public void seek(long position) {
        this.position = position
    }

    public void skip(long amount) {
        this.position += amount
    }

    public byte[] read(int total) {
        def wd  = r2(total);
        return wd
        def bout = new ByteArrayOutputStream(total) //Target to write to
        int index = (position / blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        if (!file.exists()) return null
        def read = 0
        def data = io.decode(file.bytes) //Load the block and decode using io
        int startOfFile = (position - (index * blockSize)) as int
        int length = (blockSize - startOfFile) as int
        if (data.length < length) length = data.length
        println startOfFile + " - " + data.length + " - " + length

        if (length <= 0) return null
        bout.write(data, startOfFile, length) //write the content to the output
        read += length //Update the length
        while (read < total) {
            index++ //Focus the next file
            file = splinterFile(index)
            if (!file.exists()) return bout.toByteArray() //Could return a index out of bounce
            data = io.decode(file.bytes) //Load the block and decode using io
            startOfFile = 0
            length = (total - read < blockSize) ? total - read : blockSize
            if (data.length - startOfFile < length) {
                length = data.length- startOfFile
                bout.write(data, startOfFile, length) //write the content to the output
                break
            }
            bout.write(data, startOfFile, length) //write the content to the output
            read += length
        }

        def readData = bout.toByteArray();
        position += readData.length
        return readData
    }

    public void write(byte[] d) {
        w(d)
        return;
        int index = (position / blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        def written = 0;
        def data = ByteBuffer.allocate(blockSize)
        if (file.exists()) {
            data.put(io.decode(file.bytes))
            data.rewind()
        }
        int startOfFile = (position - (index * blockSize)) as int
        int length = blockSize
        if (d.length < blockSize) length = d.length
        data.position(startOfFile)
        data.put(d, 0, length)
        byte[] toWrite = new byte[length]
        data.rewind()
        data.get(toWrite)
        file.setBytes(io.encode(toWrite))
        written += length
        position += length

        while (d.length > written) {
            index = (position / blockSize) as int //Example: 123/1024 = 0;
            file = splinterFile(index)
            data = ByteBuffer.allocate(blockSize)
            if (file.exists()) {
                data.put(io.decode(file.bytes))
                data.rewind()
            }
            startOfFile = (position - (index * blockSize)) as int
            length = blockSize
            if (d.length < blockSize) length = d.length
            if (d.length - written < blockSize) length = d.length - written
            println d.length + " - " + written + " - " + length
            data.put(d, written, length)
            toWrite = new byte[length]
            data.rewind()
            data.get(toWrite)
            //file.setBytes(PatemaIO.compress(toWrite, 0, blockSize))
            file.setBytes(io.encode(toWrite))
            written += length
        }
        position += d.length
    }

    public byte[] r2(int amount) {
        println "READr2 " + amount
        def bout = new ByteArrayOutputStream() //Target to write to
        def written = 0
        int index = (position / blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        if (!file.exists()) return null
        int startOfFile = (position - (index * blockSize)) as int
        def data = io.decode(file.bytes)
        def available = data.length - startOfFile
        if (available <= 0) return null
        bout.write(data, startOfFile, available)
        position += available
        written += available;

        while (amount > written) {
            index++;
            file = splinterFile(index)
            if (!file.exists()) return bout.toByteArray()
            startOfFile = (position - (index * blockSize)) as int
            data = io.decode(file.bytes)
            available = data.length - startOfFile
            if (available <= 0) return bout.toByteArray()
            bout.write(data, startOfFile, available)
            position+= available
            written += available
        }

        return bout.toByteArray()



    }

    public long getSize() {
        try {
            def list = directory.listFiles()
            if (list.length == 0) return 0
            def size = 0;
            list = list.findAll {it.name.endsWith("ptma")}
            list.each {
                size += it.lastModified()
            }
            return size
        } catch(Exception e) {
            return -1
        }
    }

    public void finish() {
        def list = directory.listFiles()

        def d = new ChunkedFileDescription(
                list.sum { it.length() } as long,
                blockSize,
                getSize(),
                directory.name
        )
        ChunkedFileDescription.write(d, new File(directory, "description.ptmad"))
    }

    public synchronized void w(byte[] data) {
        int index = (position / blockSize) as int // 3210/1024 = 3;
        println "index " + index
        def file = splinterFile(index)
        int startOfFile = (position - (index * blockSize)) as int //3210 - 3072 = 138
        println "start " + startOfFile
        ByteBuffer buffer = ByteBuffer.allocate(blockSize)
        if (file.exists()) {
            buffer.put(io.decode(file.bytes))
        }
        buffer.position(startOfFile)

        byte[] now = data;
        byte[] then = null;
        if (data.length > buffer.remaining()) {
            now = new byte[buffer.remaining()]
            then = new byte[data.length - buffer.remaining()]
            System.arraycopy(data, 0, now, 0, buffer.remaining())
            System.arraycopy(data, buffer.remaining(), then, 0, data.length - buffer.remaining())
        }

        buffer.put(now)
        byte[] output = new byte[buffer.position()]
        buffer.rewind()
        buffer.get(output)
        file.setBytes(io.encode(output))
        file.setLastModified(output.length)
        position += now.length

        if (then != null) w(then)

    }

    public byte[] r(int length) {

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        int index = (position / blockSize) as int
        def file = splinterFile(index)
        int startOfFile = (position - (index * blockSize)) as int //3210 - 3072 = 138

        if (!file.exists()) return null
        def realdata = io.decode(file.bytes)
        ByteBuffer buffer = ByteBuffer.wrap(realdata)

        buffer.position(startOfFile)

        println "READ " + length + " from " + index
        def bufferSize = Math.min(buffer.remaining(), length)
        byte[] temp = new byte[bufferSize]
        buffer.get(temp)
        out.write(temp)
        length -=temp.length
        position +=temp.length

        if (length != 0 && bufferSize == length) {
            def additional = r(length)
            if (additional == null) return out.toByteArray()
            out.write(additional)
        }
        return out.toByteArray()

    }

    public static void main(String[] args) {
        def f = new ChunkedFile(new ChunkedIOConfig("hallo"), new File("C:\\Users\\Jonas Born\\Downloads\\test"))
        def input = new File("C:\\Users\\Jonas Born\\Downloads\\test.bin");
        def output = new File("C:\\Users\\Jonas Born\\Downloads\\test2.bin")

        f.w(input.bytes)
        println f.position

        f.seek(0)

        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        def total = input.length()
        def read = 0
        while (total > 0) {
            def array = f.read(input.length() as int )
            bout.write(array)
            total = total - array.length
            read += array.length
            println read + " - " + array.length
        }
        //output.setBytes(f.r(input.length() as int))
        output.setBytes(bout.toByteArray())
        println f.size

        f.seek(0)

        println f.position
        println Files.hash(input, Hashing.md5())
        println Files.hash(output, Hashing.md5())
        println input.size()
        f.finish()
    }


}
