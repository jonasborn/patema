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

class V1RegisterEntry implements RegisterEntry{

    String name
    int position
    byte[] hash
    long length
    long lengthOnMedia
    String password

    V1RegisterEntry() {
    }

    V1RegisterEntry(String name, int position, byte[] hash, long length, long lengthOnMedia, String password) {
        this.name = name
        this.position = position
        this.hash = hash
        this.length = length
        this.lengthOnMedia = lengthOnMedia
        this.password = password
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        V1RegisterEntry that = (V1RegisterEntry) o

        if (length != that.length) return false
        if (lengthOnMedia != that.lengthOnMedia) return false
        if (position != that.position) return false
        if (!Arrays.equals(hash, that.hash)) return false
        if (name != that.name) return false
        if (password != that.password) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + position
        result = 31 * result + (hash != null ? Arrays.hashCode(hash) : 0)
        result = 31 * result + (int) (length ^ (length >>> 32))
        result = 31 * result + (int) (lengthOnMedia ^ (lengthOnMedia >>> 32))
        result = 31 * result + (password != null ? password.hashCode() : 0)
        return result
    }
}
