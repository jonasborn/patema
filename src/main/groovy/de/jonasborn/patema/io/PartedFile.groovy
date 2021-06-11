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

import com.google.common.io.BaseEncoding
import de.jonasborn.patema.util.Buffer
import groovy.transform.CompileStatic

import javax.servlet.ServletOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.logging.Logger

abstract class PartedFile {

    private Logger logger = Logger.getLogger(PartedFile.name)

    int partSize = 1024 * 1024

    Long position = 0

    Map<File, Long> sizes = null

    public abstract File createFile(int index)

    public abstract List<File> listFiles()

    public abstract Long getSize(File file)

    public abstract Long getSizeOnMedia(File file)

    public abstract byte[] pack(Integer index, byte[] data)

    public abstract byte[] unpack(Integer file, byte[] data)

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
        return (size == null) ? null : size as long
    }

    public Long getSizeOnMedia() {
        loadSizes()
        def size = sizes.collect { getSizeOnMedia(it.key) }.sum()
        return (size == null) ? null : size as long
    }

    @CompileStatic
    public PartedFileChunk getReadChunk(long position) {
        loadSizes()
        int index = 0
        final Iterator<Map.Entry<File, Long>> iter = sizes.iterator()
        Long sum = 0;
        if (!iter.hasNext()) return null
        Map.Entry<File, Long> element = iter.next()
        sum += element.value
        while (sum <= position) {
            if (!iter.hasNext()) return null
            element = iter.next()
            sum += element.value
            index++;
        }
        return new PartedFileChunk(index, sum - element.value, element.key)
    }

    @CompileStatic
    public PartedFileChunk getWriteChunk(long position) {
        int index = (int) (position / partSize)
        File file = createFile(index);
        return new PartedFileChunk(index, (long) (index * partSize), file)
    }

    /**
     * Used to read from a parted file
     * @param amount Bytes to read
     * @return A array with max of amount or available bytes
     */
    @CompileStatic
    public byte[] read(int amount) {
        println sizes
        println "Attempting to read ${amount}"
        def current = getReadChunk(position) //Get the positioning
        if (current == null) return null //When there is no file anymore, exit
        //Get the file position by subtracting all other file sizes from the current position
        int startOfFile = (position - current.position) as int
        def file = current.file
        println "AMOUNT $amount FILE $file POSITION $position"
        def bout = new ByteArrayOutputStream(amount) //Target to write to, amount is the max to use
        def written = 0
        def data = unpack(current.index, file.bytes) //Load the bytes
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
            data = unpack(current.index, file.bytes) //Load the data
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

    // 1111111111111333333333333300000000000000000000000 -------------------- Current file content
    // |___________| -------------------------------------------------------- Area to skip
    //              |___________| ------------------------------------------- Will be overwritten
    // |________________________| ------------------------------------------- Data from file
    //                           |_____________________| -------------------- Empty
    // |_______________________________________________| -------------------- Max file size
    //              4444444444444444444444444444444444442222222222222222222
    //              |__________________________________| -------------------- Data to write for current file
    //                                                 |__________________| - Data for next file
    //              |_____________________________________________________| - Data total

    @CompileStatic
    public synchronized void write(byte[] data) {
        final PartedFileChunk positioning = getWriteChunk(position)
        final int index = (int) (position / partSize)

        final File file = positioning.file
        final int startOfFile = (int) (position - (index * partSize))

        Buffer dataBuffer = new Buffer(data);
        Buffer fileBuffer = new Buffer(partSize)
        if (file.exists()) fileBuffer.put(unpack(index, file.bytes))
        int length = fileBuffer.position()
        fileBuffer.position(startOfFile)
        byte[] toWrite = dataBuffer.getRemainingOrMax(fileBuffer.remaining())
        fileBuffer.put(toWrite)
        position += toWrite.length
        length = Math.max(length, fileBuffer.position())
        fileBuffer.rewind()
        file.setBytes(pack(index, fileBuffer.getArray(length)))
        if (dataBuffer.remaining() > 0) write(dataBuffer.getRemaining())
    }

    public void close() {

    }

    public byte[] hash() {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        def list = listFiles()
        for (i in 0..<list.size()) {
            def element = list[i]
            def data = unpack(i, element.bytes)
            digest.update(data)
        }
        return digest.digest()
    }
}
