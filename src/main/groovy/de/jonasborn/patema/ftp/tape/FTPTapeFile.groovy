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

package de.jonasborn.patema.ftp.tape

import de.jonasborn.patema.ftp.FTPElement

class FTPTapeFile extends FTPElement {

    FTPTape tape
    String title
    Long size

    FTPTapeFile(FTPTape tape, String title) {
        super(Type.TAPE_FILE)
        this.tape = tape
        this.title = title
    }

    @Override
    String getPath() {
        return tape.getPath() + "/" + title
    }

    @Override
    boolean exists() {
        return false
    }

    @Override
    FTPElement getParent() {
        return tape
    }

    @Override
    void delete() {

    }


}
