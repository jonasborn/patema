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
import de.jonasborn.patema.io.chunked.UnPackedFileConfig

class RawInputStream extends InputStream {

    RawFile file;

    RawInputStream(RawFile file) {
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
            println "TEMPL " + temp.length
            println "BLEN  " + b.length
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

    public static void main(String[] args) {
        def c = new ChunkedFile(new UnPackedFileConfig("a"), new File("root/project-test1/8axw02ww.exe"))
        def r = new RawFile(c)
        def is = new RawInputStream(r)

        def f = new File("test")
        FileOutputStream fout = new FileOutputStream(f)

        def total = 0
        def read =0;
        byte[] buffer = new byte[8192]
        while ((read = is.read(buffer)) > 0) {
            fout.write(buffer, 0, read)
            def before = total
            total += read
            println "READ TOTAL: " + total + " ${total - before}"
        }
        println "-"
        println "SIZE :" + r.size
        println "READ : " + total + " - ${r.size - total}"
        println "FILE :" + f.length() + " - ${r.size - f.length()}"
    }
}
