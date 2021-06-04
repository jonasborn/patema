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

class PartedFileInputStream extends InputStream {

    PartedFile file;

    PartedFileInputStream(PartedFile file) {
        this.file = file
    }

    @Override
    int read() throws IOException {
        return (int) file.read(1)[0]
    }

    @Override
    int read(byte[] b) throws IOException {
        try {
            def temp = file.read(b.length)
            if (temp == null) return -1
            System.arraycopy(temp, 0, b, 0, Math.min(b.length, temp.length))
            return Math.min(b.length, temp.length)
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }
    }

    @Override
    byte[] readNBytes(int len) throws IOException {
        return file.read(len)
    }

    void seek(long position) {
        file.seek(position)
    }


    @Override
    long skip(long n) throws IOException {
        file.skip(n)
        return n
    }


    @Override
    synchronized void reset() throws IOException {
        file.seek(0)
    }

    @Override
    void close() throws IOException {
        file.seek(0)
    }
}
