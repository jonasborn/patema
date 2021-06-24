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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class ByteUtils {

    public static byte[] xor(byte[] iv, byte[] data) {
        int position = 0;
        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            if (position >= iv.length) position = 0;
            output[i] = (byte) (iv[position] ^ data[i]);
            position++;
        }
        return output;
    }

    public static byte[] join(byte[] a, byte[] b) {
        byte[] output = new byte[a.length + b.length];
        System.arraycopy(a, 0, output, 0, a.length);
        System.arraycopy(b, 0, output, a.length, b.length);
        return output;
    }


    public static long copyBuffered(InputStream from, OutputStream to, int bufferSize) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        byte[] buffer = new byte[bufferSize];
        long total = 0;
        while (true) {
            int read = from.read(buffer);
            if (read == -1) break;
            to.write(buffer, 0, read);
            total += read;
        }
        return total;
    }

    /**
     * Used to copy the contents from an InputStream to an OutputStream using a fixed block size.
     * Therefore the buffer used is also fixed. If the read data from the InputStream is less than the
     * buffer, the data is padded.
     * @param from A InputStream to read from
     * @param to A OutputSteam to write to
     * @param blockSize The block/buffer size used to read and write
     * @return The bytes WRITTEN
     * @throws Exception "everything may happen"
     */
    public static long copyFixed(InputStream from, OutputStream to, int blockSize) throws Exception {
        checkNotNull(from);
        checkNotNull(to);
        byte[] buffer = new byte[blockSize];
        long total = 0;
        while (true) {
            int read = from.read(buffer);
            if (read == -1) break;
            to.write(buffer);
            total += buffer.length;
        }
        return total;
    }

    /**
     * Will copy a maximum amount of bytes from a InputStream to a OutputStream
     *
     * @param from       Source InputStream
     * @param to         Target OutputStream
     * @param maxSize    Max size to copy
     * @param bufferSize BufferSize to use
     * @param strict     If true, only the max bytes will be read, if false, a whole buffer is read but only the missing
     *                   bytes are written to the target stream; this not strict mode is used for reading tape devices
     * @return The read bytes
     * @throws IOException If something went wrong
     */
    public static long copyMax(InputStream from, OutputStream to, long maxSize, int bufferSize, boolean strict) throws IOException {
        int read;
        int total = 0;
        byte[] buffer = new byte[bufferSize];
        while ((read = from.read(buffer)) != -1) {
            to.write(buffer, 0, read);
            total += read;
            if ((maxSize - total) <= bufferSize) break;
        }
        if (read == -1) return total;

        if (strict) buffer = new byte[(int) (maxSize - total)];

        read = from.read(buffer);
        read = (int) Math.min(read, (maxSize - total));
        to.write(buffer, 0, read);
        total += read;
        if (total == 0) return -1;
        return total;
    }

    public static byte[] pad(byte[] source, int toAdd, byte content) {
        byte[] result = new byte[source.length + toAdd];
        byte[] additional = new byte[toAdd];
        for (int i = 0; i < toAdd; i++) additional[i] = content;
        System.arraycopy(source, 0, result, 0, result.length);
        System.arraycopy(additional, result.length, result, 0, additional.length);
        return result;
    }

    public static byte[] create(int length, byte content) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) result[i] = content;
        return result;
    }

}
