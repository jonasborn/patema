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

package de.jonasborn.patema.ftp

import de.jonasborn.patema.tape.Tape
import de.jonasborn.patema.tape.Tapes

import static de.jonasborn.patema.ftp.FTPElement.Type.TAPE

public class FTPTape extends FTPDirectory<FTPTapeFile> {
    FTPRoot root
    String devicePath
    Tape device

    FTPTape(FTPRoot root, String devicePath) {
        super(TAPE)
        this.root = root
        this.devicePath = devicePath
    }

    @Override
    String getPath() {
        return "/" + getTitle()
    }

    @Override
    boolean exists() {
        return Tapes.get(devicePath) != null
    }

    @Override
    FTPElement getParent() {
        return root
    }

    @Override
    String getTitle() {
        return "tape-" + devicePath.split("/").last()
    }

    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }

    public Tape getDevice() {
        return Tapes.get(devicePath)
    }

    public List<FTPTapeFile> list() {
        return [
                new FTPTapeFile(this, "a"),
                new FTPTapeFile(this, "b"),
                new FTPTapeFile(this, "c"),
        ]
    }
}

