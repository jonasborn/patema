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

package de.jonasborn.patema.register


import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding

import java.nio.charset.Charset

class V1Register implements Register {

    String id
    LinkedList<DescriptionFile> files = []

    V1Register(String id) {
        this.id = id
    }

    @Override
    int getVersion() {
        return 1
    }

    public void add(String name, byte[] md5, long length) {
        files.add(new DescriptionFile(name, md5, length))
    }


    public static class DescriptionFile {
        String name
        byte[] hash
        long length

        DescriptionFile(String name, byte[] hash, long length) {
            this.name = name
            this.hash = hash
            this.length = length
        }
    }


}
