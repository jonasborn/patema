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


import java.nio.ByteBuffer

class ChunkedFile {

    private File directory;
    private File description;
    private Long position = 0
    ChunkedFileConfig config
    ChunkedIO io

    private File splinterFile(int index) {
        return new File(directory, index + ".ptma")
    }

    ChunkedFile(ChunkedFileConfig config, File directory) {
        assert config != null
        this.config = config
        this.io = new ChunkedIO(config)
        this.directory = directory
        this.description = new File(directory, "description.ptmad")
    }

    public void setConfig(ChunkedFileConfig config) {
        this.config = config
    }

    public long getPosition() {
        return position
    }

    public void seek(long position) {
        this.position = position
    }

    public void skip(long amount) {
        this.position += amount
    }


    public byte[] read(int amount) {
        def bout = new ByteArrayOutputStream(amount) //Target to write to
        def written = 0
        int index = (position / config.blockSize) as int //Example: 123/1024 = 0;
        def file = splinterFile(index)
        if (!file.exists()) return null
        int startOfFile = (position - (index * config.blockSize)) as int
        def data = io.decode(index, file.bytes)
        def available = data.length - startOfFile
        if (available <= 0) return null
        bout.write(data, startOfFile, available)
        position += available
        written += available;

        while (amount > written) {
            index++;
            file = splinterFile(index)
            if (!file.exists()) return bout.toByteArray()
            startOfFile = (position - (index * config.blockSize)) as int
            data = io.decode(index, file.bytes)
            available = data.length - startOfFile
            if (available <= 0) return bout.toByteArray()
            bout.write(data, startOfFile, available)
            position += available
            written += available
        }

        return bout.toByteArray()
    }

    public long getSize() {
        try {
            def list = directory.listFiles()
            if (list.length == 0) return 0
            def size = 0;
            list = list.findAll { it.name.endsWith("ptma") }
            list.each {
                size += it.lastModified()
            }
            return size
        } catch (Exception ignored) {
            return -1
        }
    }

    public void finish() {
        def list = directory.listFiles()

        def d = new ChunkedFileDescription(
                list.sum { it.length() } as long,
                config.blockSize,
                getSize(),
                directory.name
        )
        ChunkedFileDescription.write(d, new File(directory, "description.ptmad"))
    }

    public synchronized void write(byte[] data) {
        int index = (position / config.blockSize) as int // 3210/1024 = 3;
        println "index " + index
        def file = splinterFile(index)
        int startOfFile = (position - (index * config.blockSize)) as int //3210 - 3072 = 138
        println "start " + startOfFile
        ByteBuffer buffer = ByteBuffer.allocate(config.blockSize)
        if (file.exists()) {
            buffer.put(io.decode(index, file.bytes))
        }
        buffer.position(startOfFile)

        byte[] now = data;
        byte[] then = null;
        if (data.length > buffer.remaining()) {
            now = new byte[buffer.remaining()]
            then = new byte[data.length - buffer.remaining()]
            //System.arraycopy(data, 0, now, 0, buffer.remaining())
            buffer.get(now)
            //System.arraycopy(data, buffer.remaining(), then, 0, data.length - buffer.remaining())
            buffer.get(then)
        }

        buffer.put(now)
        byte[] output = new byte[buffer.position()]
        buffer.rewind()
        buffer.get(output)
        file.setBytes(io.encode(index, output))
        file.setLastModified(output.length)
        position += now.length

        if (then != null) write(then)

    }


}
