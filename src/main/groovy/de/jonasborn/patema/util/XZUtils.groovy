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

package de.jonasborn.patema.util

import com.google.common.io.ByteStreams
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZInputStream
import org.tukaani.xz.XZOutputStream

class XZUtils {

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        XZOutputStream xout = new XZOutputStream(bout, new LZMA2Options())
        xout.write(data)
        xout.flush()
        xout.close()
        bout.close()
        return bout.toByteArray()
    }

    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data)
        XZInputStream xin = new XZInputStream(bin)
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        ByteStreams.copy(xin, bout)
        xin.close()
        bin.close()
        bout.close()
        return bout.toByteArray()
    }

}
