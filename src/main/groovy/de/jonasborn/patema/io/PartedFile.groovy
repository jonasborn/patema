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


import groovy.transform.CompileStatic

import java.nio.ByteBuffer
import java.util.logging.Logger

abstract class PartedFile {

    private Logger logger = Logger.getLogger(PartedFile.name)

    int partSize = 1024 * 1024

    Long position = 0

    Map<File, Long> sizes = null

    public abstract File createFile(int index)

    public abstract List<File> listFiles()

    public abstract Long getSize(File file)

    public abstract byte[] pack(File file, byte[] data)

    public abstract byte[] unpack(File file, byte[] data)

    public void read(File file, byte[] bytes) {

    }



    public void seek(Long position) {
        this.position = position
    }

    public void skip(long amount) {
        this.position += amount
    }

    public void loadSizes() {
        if (sizes == null) {
            sizes = [:]
            listFiles().each { sizes[it] = getSize(it) }
        }
    }

    public Long getSize() {
        loadSizes()
        def size = sizes.collect { getSize(it.key) }.sum()
        return (size == null) ? -1 : size as long
    }

    @CompileStatic
    public PartedChunk getReadChunk(long position) {
        loadSizes()
        final Iterator<Map.Entry<File, Long>> iter = sizes.iterator()
        final Long sum = 0;
        if (!iter.hasNext()) return null
        Map.Entry<File, Long> element = iter.next()
        sum += element.value
        while (sum <= position) {
            if (!iter.hasNext()) return null
            element = iter.next()
            sum += element.value
        }
        return new PartedChunk(sum - element.value, element.key)
    }

    @CompileStatic
    public PartedChunk getWriteChunk(long position) {
        int index = (int) (position / partSize)
        File file = createFile(index);
        return new PartedChunk((long) (index * partSize), file)
    }

    /**
     * Used to read from a parted file
     * @param amount Bytes to read
     * @return A array with max of amount or available bytes
     */
    @CompileStatic
    public byte[] read(int amount) {
        println "Attempting to read ${amount}"
        def current = getReadChunk(position) //Get the positioning
        if (current == null) return null //When there is no file anymore, exit
        //Get the file position by subtracting all other file sizes from the current position
        int startOfFile = (position - current.position) as int
        def file = current.file
        def bout = new ByteArrayOutputStream(amount) //Target to write to, amount is the max to use
        def written = 0
        def data = unpack(file, file.bytes) //Load the bytes
        //Either take the remaining bytes of the current file or the amount as max available
        int available = Math.min(data.length - startOfFile, amount)
        if (available <= 0) return null
        //Write the data to the output
        bout.write(data, startOfFile, available)
        position += available //Add the read bytes to the position
        written += available; //Add the written bytes to the position
        while (amount > written) { //While there is still sth. to read left
            current = getReadChunk(position) //Get the positioning again
            if (current == null) return bout.toByteArray() //When no positioning is found, return the current result
            file = current.file
            if (!file.exists()) return bout.toByteArray() //When the file is missing, return the current result
            startOfFile = (position - current.position) as int //Set the start of file by substr. like above
            data = unpack(file, file.bytes) //Load the data
            //Take either the remaining size or the amount needed minus the already written bytes
            available = Math.min(data.length - startOfFile, amount - written)
            //If there is nothing left to read, just return the result
            if (available <= 0) return bout.toByteArray()
            bout.write(data, startOfFile, available)
            position += available
            written += available
        }

        return bout.toByteArray()
    }

    @CompileStatic
    public synchronized void write(byte[] data) {
        final PartedChunk positioning = getWriteChunk(position)
        final int index = (int) (position / partSize)
        final File file = positioning.file
        final int startOfFile = (int) (position - (index * partSize))
        final ByteBuffer buffer = ByteBuffer.allocate(partSize)
        if (file.exists()) {
            buffer.put(unpack(file, file.bytes))
        }
        buffer.position(startOfFile)

        byte[] now = data;
        byte[] then = null;
        if (data.length > buffer.remaining()) {
            now = new byte[buffer.remaining()]
            then = new byte[data.length - buffer.remaining()]
            buffer.get(now)
            buffer.get(then)
        }

        buffer.put(now)
        final byte[] output = new byte[buffer.position()]
        buffer.rewind()
        buffer.get(output)
        final byte[] content = pack(file, output)
        file.setBytes(content)
        position += now.length
        sizes = null
        if (then != null) write(then)

    }

    public void close() {

    }
}
