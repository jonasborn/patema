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



}
