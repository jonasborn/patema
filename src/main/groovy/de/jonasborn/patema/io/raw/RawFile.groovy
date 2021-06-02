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

package de.jonasborn.patema.io.raw

import de.jonasborn.patema.io.chunked.ChunkedFile

class RawFile {

    long position
    ChunkedFile file;
    Map<File, Long> fileSizes = [:]

    RawFile(ChunkedFile file) {
        this.file = file
        file.listChuncks().each {
            fileSizes.put(it.file, it.file.length())
        }
        println fileSizes
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


    private File file(int index) {
        return new File(file.directory, index + ".ptma")
    }

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
    public byte[] read(int amount) {
        def current = getPositioning() //Get the positioning
        if (current == null) return null //When there is no file anymore, exit
        //Get the file position by subtracting all other file sizes from the current position
        int startOfFile = (position - current.before) as int
        def file = current.file
        def bout = new ByteArrayOutputStream(amount) //Target to write to, amount is the max to use
        def written = 0
        def data = file.bytes //Load the bytes
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
            data = file.bytes //Load the data
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


    public long getSize() {
        def l = file.directory.listFiles()
        if (l == null) return 0
        return l.findAll { it.name.endsWith(".ptma") }.sum { it.length() } as long
    }


}
