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

import de.jonasborn.patema.ios.endecode.Decoder
import de.jonasborn.patema.ios.endecode.implementation.AESECBOnLZMA2
import de.jonasborn.patema.ios.parted.file.PartedFileChunk
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.util.ByteUtils
import de.jonasborn.patema.util.FixedByteBucked
import de.jonasborn.patema.util.LogUtils
import de.jonasborn.patema.util.PaddingUtils
import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TapeReader {

    private static final bufferSize = 256
    private static final blockSize = 256;

    Logger logger = LogManager.getLogger(TapeReader.class)

    Register register
    Tape tape
    int file

    long position = 0

    TapeReader(Tape tape, Register register, int file) {
        this.tape = tape
        this.register = register
        this.file = file
    }

    private Integer getStartChunk() {
        def parts = register.getEntry(file).getParts()
        def last = null
        def total = 0;
        for (i in 0..<parts.size()) {
            total += parts[i]
            if (total > position) return i
        }
        return last
    }

    public byte[] read(Decoder decoder, int length) {
        int maxChunk = register.getEntry(file).getParts().size()
        int chunk = getStartChunk()
        int read = 0;
        def bout = new ByteArrayOutputStream(length)

        while (read < length) {
            byte[] chunkData = readChunk(chunk);
            byte[] data = decoder.decode(file, chunkData)
            bout.write(data, 0, Math.min((length - read), data.length))
            read += data.length
            if (chunk + 1 < maxChunk) chunk++
            else break
        }

        return bout.toByteArray()

    }

    public byte[] readChunk(int chunk) {
        tape.moveToFileStart(file + 1) //Skip the register
        def parts = register.getEntry(file).parts

        int realToSkip = 0
        int paddedToSkip = 0
        for (i in 0..<chunk) {
            realToSkip += parts[i] as int
            paddedToSkip += PaddingUtils.calculate(parts[i], blockSize).total as int
        }

        tape.device.clearEOF()
        def is = tape.device.inputStream

        int realPartSize = parts.get(chunk) as int
        int paddedPartSize = PaddingUtils.calculate(realPartSize, blockSize).total as int
        assert (paddedPartSize % blockSize) == 0


        logger.debug(
                "Reading chunk {}, length {} ({}p) from file {} skipping {} ({}p)",
                chunk,
                realPartSize,
                paddedPartSize,
                file,
                realToSkip,
                paddedToSkip
        )

        def skipped = is.skip(paddedToSkip) //Skip all already read parts
        logger.debug("Skipped {} bytes from files {} beginning", skipped, file)

        int prefix = paddedToSkip - realToSkip;

        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        ByteUtils.copyMax(is, bout, paddedPartSize, bufferSize, false)
        logger.debug("Skipping {} and limiting stream to {}", prefix, realPartSize + prefix)
        byte[] temp = bout.toByteArray();
        byte[] result = new byte[realPartSize];
        System.arraycopy(temp, 0, result, 0, Math.min(realPartSize, temp.size()))
        return result;
    }

    @CompileStatic
    public TapeChunk getReadChunk(long position) {
        def parts = register.getEntry(file).getParts()
        int index = 0
        final Iterator<Long> iter = parts.iterator()
        Long sum = 0;
        if (!iter.hasNext()) return null
        Long element = iter.next()
        sum += element
        while (sum <= position) {
            if (!iter.hasNext()) return null
            element = iter.next()
            sum += element
            index++;
        }
        return new TapeChunk(index, sum - element)
    }


    static void main(String[] args) {
        /*LogUtils.setRootLevel("ALL")
        Tape tape1 = new Tape("nst0", "/dev/nst0")
        tape1.initialize()
        def register = tape1.readRegister("jonas")
        println "VERSION: " + register.getVersion()
        def tr = new TapeReader(tape1, register, 0)
        def part = register.getEntry(0)
        def decoder = new AESECBOnLZMA2("jonas", register.getIv(part.name), register.getSalt(part.name))

        def bout = new ByteArrayOutputStream()
        //bout.write(tr.readChunk(0))
        bout.write(tr.readChunk(1))

        new File("chunk01.bin").setBytes(bout.toByteArray())

        tape1.moveToFileStart(1)
        def bout2 = new ByteArrayOutputStream()
        ByteUtils.copyMax(tape1.device.inputStream, bout2, 1048688 * 2, 1024 * 8, false)

        new File("direct.bin").setBytes(bout2.toByteArray())
        *//*for (i in 0..<parts.size()) {
            println parts[i]
            println "READ: " + tr.readChunk(0, i).length
        }*//*

        def tar = new File("steamtar.bin")
        def tars = new FileOutputStream(tar)

        def tis = new TapeInputStream(
                tr,
                decoder
        )

        ByteUtils.copyMax(tis, tars, 1024 * 1024, 1024 * 8, false)*/
    }
}
