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

package de.jonasborn.patema.ftp.project

import de.jonasborn.patema.ftp.FTPElement
import de.jonasborn.patema.util.FileUtils

class FTPProjectDirectory extends FTPProjectElement{

    FTPProjectDirectory(FTPProject project, FTPElement parent, String title) {
        super(project, parent, title)
        assert title != null
        this.type = Type.PROJECT_DIR
        this.project = project
        println "CREATED DIRECTORY WITH PATH: " + getPath()
    }

    @Override
    List<FTPProjectElement> list() {
        if (!delegate.exists()) return []
        return FileUtils.list(delegate).findAll { it.isDirectory() }.collect {
            if (FTPProject.isLogicalDirectory(it)) return new FTPProjectDirectory(project, this, it.name)
            else return new FTPProjectFile(project, this, it.name)
        }
    }

    @Override
    boolean exists() {
        new File(path).exists()
    }


    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }
}
