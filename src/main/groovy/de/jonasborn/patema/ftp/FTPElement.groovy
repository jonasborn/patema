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

import de.jonasborn.patema.ftp.project.FTPProject
import de.jonasborn.patema.ftp.project.FTPProjectDirectory
import de.jonasborn.patema.ftp.project.FTPProjectFile
import de.jonasborn.patema.ftp.project.FTPProjectElement
import de.jonasborn.patema.ftp.tape.FTPTape
import de.jonasborn.patema.ftp.tape.FTPTapeFile

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

    abstract void delete()

    boolean isFile() {
        return type == PROJECT_FILE || type == TAPE_FILE
    }

    boolean isDirectory() {
        return type == ROOT || type == PROJECT || type == TAPE
    }

    public boolean typeRoot() {
        return type == ROOT
    }


    public boolean typeProject() {
        return type == PROJECT
    }

    public boolean typeTape() {
        return type == TAPE
    }

    public boolean typeTapeFile() {
        return type == TAPE_FILE
    }

    public boolean typeProjectFile() {
        return type == PROJECT_FILE
    }

    public boolean typeProjectDirectory() {
        return type == PROJECT_DIR
    }

    public boolean typeProjectElement() {
        return type == PROJECT_ELEMENT || type == PROJECT_FILE || type == PROJECT_DIR
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

    public FTPProjectElement asProjectElement() {
        return (FTPProjectElement) this
    }

    public FTPProjectFile asProjectFile() {
        return (FTPProjectFile) this
    }

    public FTPProjectDirectory asProjectDirectory() {
        return (FTPProjectDirectory) this
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
        PROJECT_ELEMENT,
        PROJECT_FILE,
        PROJECT_DIR,
        TAPE,
        TAPE_FILE
    }


}
