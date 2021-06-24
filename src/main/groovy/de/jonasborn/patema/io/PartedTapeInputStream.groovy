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
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.tape.Tape
import de.jonasborn.patema.util.Buffer
import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.security.MessageDigest

abstract class PartedTapeInputStream {

    Logger logger = LogManager.getLogger(PartedTapeInputStream.class)

    int partSize = 1024 * 1024

    Long position = 0

    Map<Integer, Long> indexSizes = null

    Tape tape
    Register register

    PartedTapeInputStream(Tape tape, Register register) {
        this.tape = tape
        this.register = register
    }

    public boolean isStreamAvailable(int index) {
        return register.getEntry(index) == null
    }

    public abstract InputStream createInputStream(int index)

    //List of raw lengths
    public abstract List<Long> listParts()

    public abstract Long getSize(Integer index)

    public abstract Long getSizeOnMedia(Integer index)

    public abstract byte[] pack(Integer index, byte[] data)

    public abstract byte[] unpack(Integer file, byte[] data)

    public abstract String getName();

    private byte[] readBytes(int index) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        ByteStreams.copy(createInputStream(index), bout)
        return bout.toByteArray()
    }


    public void seek(Long position) {
        logger.debug("Setting position {} for {}", position, this)
        this.position = position
    }

    public void skip(long amount) {
        this.position += amount
    }

    public void loadSizes() {

        if (indexSizes == null) {
            indexSizes = [:]
            def parts = listParts()
            for (i in 0..<parts.size()) {
                indexSizes.put(i, getSize(i))
            }
        }

    }

    public Long getSize() {
        loadSizes()
        def size = indexSizes.collect { getSize(it.key) }
        return (size == null) ? null : size as long
    }

    public Long getSizeOnMedia() {
        loadSizes()
        def size = indexSizes.collect { getSizeOnMedia(it.key) }
        return (size == null) ? null : size as long
    }

    @CompileStatic
    public PartedStreamChunk getReadChunk(long position) {
        loadSizes()
        int index = 0
        //final Iterator<Map.Entry<File, Long>> iter = sizes.iterator()
        final Iterator<Map.Entry<Integer, Long>> iter = indexSizes.iterator()
        Long sum = 0;
        if (!iter.hasNext()) return null
        Map.Entry<Integer, Long> element = iter.next()
        sum += element.value
        while (sum <= position) {
            if (!iter.hasNext()) return null
            element = iter.next()
            sum += element.value
            index++;
        }
        return new PartedStreamChunk(index, sum - element.value)
    }

    @CompileStatic
    public PartedStreamChunk getWriteChunk(long position) {
        int index = (int) (position / partSize)
        return new PartedStreamChunk(index, (long) (index * partSize))
    }

    /**
     * Used to read from a parted file
     * @param amount Bytes to read
     * @return A array with max of amount or available bytes
     */
    @CompileStatic
    public byte[] read(int amount) {
        logger.debug("Attempting to read {} bytes from {}", amount, this)
        def current = getReadChunk(position) //Get the positioning
        if (current == null) return null //When there is no file anymore, exit
        logger.debug("Identified chunk {} for read of {} bytes from {}", current.getIndex(), amount, this)
        //Get the file position by subtracting all other file sizes from the current position
        int startOfFile = (position - current.position) as int
        def bout = new ByteArrayOutputStream(amount) //Target to write to, amount is the max to use
        def written = 0
        def data = unpack(current.index, readBytes(current.index)) //Load the bytes
        //Either take the remaining bytes of the current file or the amount as max available
        int available = Math.min(data.length - startOfFile, amount)
        if (available <= 0) return null
        //Write the data to the output
        bout.write(data, startOfFile, available)
        position += available //Add the read bytes to the position
        written += available; //Add the written bytes to the position
        while (amount > written) { //While there is still sth. to read left
            logger.debug("Chunk {} fully read, moving on", current.getIndex())
            current = getReadChunk(position) //Get the positioning again
            if (current == null) return bout.toByteArray() //When no positioning is found, return the current result
            logger.debug("Attempting to read remaining {} bytes from chunk {} of {}", amount - written, current.getIndex(), this)
            if (!isStreamAvailable(current.index)) return bout.toByteArray()
            //When the file is missing, return the current result
            startOfFile = (position - current.position) as int //Set the start of file by substr. like above
            logger.debug("Skipping to {} in chunk {} of {}", startOfFile, current.getIndex(), this)
            data = unpack(current.index, readBytes(current.index)) //Load the data
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


    public void close() {

    }


    @Override
    public String toString() {
        return "PartedFile{" + this.getName() + "}";
    }
}
