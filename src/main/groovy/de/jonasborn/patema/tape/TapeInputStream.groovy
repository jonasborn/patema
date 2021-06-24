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

package de.jonasborn.patema.tape

import de.jonasborn.patema.ios.endecode.Decoder

class TapeInputStream extends InputStream{

    TapeReader reader
    Decoder decoder

    TapeInputStream(TapeReader reader, Decoder decoder) {
        this.reader = reader
        this.decoder = decoder
    }

    @Override
    int read() throws IOException {
        return reader.read(decoder, 1)[0]
    }

    @Override
    int read(byte[] b) throws IOException {
        byte[] temp = reader.read(decoder, b.length);
        System.arraycopy(temp, 0, b, 0, temp.length)
        if (temp.length == 0) return -1
        return temp.length
    }


}
