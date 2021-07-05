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

class Entry<A, B> {
    A a
    B b

    Entry(A a, B b) {
        this.a = a
        this.b = b
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Entry entry = (Entry) o

        if (a != entry.a) return false
        if (b != entry.b) return false

        return true
    }

    int hashCode() {
        int result
        result = (a != null ? a.hashCode() : 0)
        result = 31 * result + (b != null ? b.hashCode() : 0)
        return result
    }
}