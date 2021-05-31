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

import static de.jonasborn.patema.ftp.FTPElement.Type.*

abstract class FTPElement {

    Type type
    String title

    FTPElement(Type type) {
        this.type = type
        this.title = title
    }

    abstract String getPath()

    abstract boolean exists()

    abstract FTPElement getParent()

    abstract String getTitle()

    boolean isFile() {
        return type == PROJECT_FILE || type == TAPE_FILE
    }

    boolean isDirectory() {
        return type == ROOT || type == PROJECT || type == TAPE
    }

    public boolean isRoot() {
        return type == ROOT
    }

    public boolean isProject() {
        return type == PROJECT
    }

    public boolean isTape() {
        return type == TAPE
    }

    public boolean isTapeFile() {
        return type == TAPE_FILE
    }

    public boolean isProjectFile() {
        return type == PROJECT_FILE
    }

    public FTPRoot asRoot() {
        return (FTPRoot) this
    }

    public FTPProject asProject() {
        return (FTPProject) this
    }

    public FTPTape asTape() {
        return (FTPTape) this
    }

    public FTPProjectFile asProjectFile() {
        return (FTPProjectFile) this
    }

    public FTPTapeFile asTapeFile() {
        return (FTPTapeFile) this
    }

    public FTPDirectory asDirectory() {
        return (FTPDirectory) this
    }

    public FTPFile asFile() {
        return (FTPFile) this
    }

    @Override
    public String toString() {
        return "{" + path + " - " + type + "}"
    }


    public static enum Type {
        ROOT,
        PROJECT,
        PROJECT_FILE,
        TAPE,
        TAPE_FILE
    }




}
