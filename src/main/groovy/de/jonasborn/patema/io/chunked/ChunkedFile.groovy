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

package de.jonasborn.patema.io.chunked


import java.nio.ByteBuffer

class ChunkedFile {

    private File directory;
    private File description;
    private Long position = 0
    UnPackedFileConfig config
    ChunkedIO io

    private File file(int index) {
        return new File(directory, index + ".ptma")
    }

    ChunkedFile(UnPackedFileConfig config, File directory) {
        assert config != null
        this.config = config
        this.io = new ChunkedIO(config)
        this.directory = directory
        this.description = new File(directory, "description.ptmad")
    }

    public void setConfig(UnPackedFileConfig config) {
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
        return r(amount);
        def bout = new ByteArrayOutputStream(amount) //Target to write to
        def written = 0
        int index = (position / config.blockSize) as int //Example: 123/1024 = 0;
        def file = file(index)
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
            file = file(index)
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

    public File getDirectory() {
        return directory
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
        fileSizes = null
        int index = (position / config.blockSize) as int // 3210/1024 = 3;
        println "index " + index
        def file = file(index)
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


    public List<ChunkedFileChunk> listChuncks() {
        List<File> list = this.directory.listFiles()
        if (list == null) return []
        list = list.findAll {it.name.endsWith(".ptma")}
        list.sort(new Comparator<File>()
        {
            @Override
            public int compare(File o1, File o2) {
                def i1 = (o1 as File).name.replaceAll("[^0-9.]", "")
                def i2 = (o2 as File).name.replaceAll("[^0-9.]", "")
                return i1 <=> i2
            }
        })
        def r = []
        for (int i = 0; i < list.size(); i++) {
            r.add(new ChunkedFileChunk(this, list[i], i))
        }
        return r
    }

    public Map<File, Long> fileSizes = null


    /**
     * Search for the current file using the position.
     * Will also return the sum of all sizes of files before
     * @return A fresh Positioning object
     */
    public Positioning getPositioning() {
        def iter = fileSizes.iterator()
        def sum = 0;
        if (!iter.hasNext()) return null
        def element = iter.next()
        sum += element.value
        while (sum <= position) {
            if (!iter.hasNext()) return null
            element = iter.next()
            sum += element.value
        }
        return new Positioning(sum - element.value, element.key)
    }

    static class Positioning {
        Long before;
        File file

        Positioning(Long before, File file) {
            this.before = before
            this.file = file
        }


        @Override
        public String toString() {
            return "CurrentResult{" +
                    "before=" + before +
                    ", file=" + file +
                    '}';
        }
    }

    //This could be a do-while but groovy won't support it
    public byte[] readDynamic(int amount) {
        if (fileSizes == null ){
            def list = directory.listFiles()
            fileSizes = [:]
            if (list != null) {
                list.each {
                    fileSizes.put(it, it.lastModified())
                }
            }
        }
        def current = getPositioning() //Get the positioning
        if (current == null) return null //When there is no file anymore, exit
        //Get the file position by subtracting all other file sizes from the current position
        int startOfFile = (position - current.before) as int
        def file = current.file
        def bout = new ByteArrayOutputStream(amount) //Target to write to, amount is the max to use
        def written = 0
        def data = io.decode(0, file.bytes) //Load the bytes
        //Either take the remaining bytes of the current file or the amount as max available
        int available = Math.min(data.length - startOfFile, amount)
        if (available <= 0) return null
        //Write the data to the output
        bout.write(data, startOfFile, available)
        position += available //Add the read bytes to the position
        written += available; //Add the written bytes to the position
        while (amount > written) { //While there is still sth. to read left
            current = getPositioning() //Get the positioning again
            if (current == null) return bout.toByteArray() //When no positioning is found, return the current result
            file = current.file
            if (!file.exists()) return bout.toByteArray() //When the file is missing, return the current result
            startOfFile = (position - current.before) as int //Set the start of file by substr. like above
            data = io.decode(0, file.bytes) //Load the data
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

}
