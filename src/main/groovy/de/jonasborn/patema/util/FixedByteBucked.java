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

package de.jonasborn.patema.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class FixedByteBucked extends OutputStream {

    int size;
    int position = 0;
    private final byte[] buffer;

    public FixedByteBucked(int size) {
        this.size = size;
        buffer = new byte[size];
    }

    @Override
    public synchronized void write(int b) {
        if (position + 1 < size) {
            buffer[position +1] = (byte) b;
            position++;
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        int toWrite = Math.min(len - off, size - position);
        System.arraycopy(b, off, buffer, position, toWrite);
        position +=toWrite;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, Math.min(b.length, size - position));
    }

    public byte[] trim(int index, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(buffer, index, temp, 0, length);
       return temp;
    }

    public  byte[] toByteArray() {
        return buffer;
    }

    public int getSize() {
        return buffer.length;
    }

    public int length() {
        return buffer.length;
    }
}
