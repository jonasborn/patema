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

class ChunkedOutputStream extends OutputStream {

    RandomAccessFile test

    ChunkedFile file

    ChunkedOutputStream(ChunkedFile file) {
        test = new RandomAccessFile("hallo.test", "rw")
        this.file = file
    }

    public void seek(long position) {
        this.file.seek(position)
    }

    @Override
    void write(int b) throws IOException {
        println "WRITEBYTE"
        file.write([(byte) b] as byte[])
    }

    @Override
    void write(byte[] b) throws IOException {
        println "WRITEARRAY"
        file.write(b)
    }

    @Override
    void write(byte[] b, int off, int len) throws IOException {
        try {
            //println "WRITEOFF " + off + " - " + len
            byte[] temp = new byte[len]
            System.arraycopy(b, off, temp, 0, len)
            file.write(temp)

        } catch (Exception e) {
            e.printStackTrace()
            throw new IOException(e.getMessage())
        }
        //println "WRITTEN " + file.position
    }

    @Override
    void close() throws IOException {
        file.seek(0)
    }
}
