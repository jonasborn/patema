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

class FTPProjectDirectory extends FTPProjectElement {

    File delegate;
    FTPProject project;
    String path

    FTPProjectDirectory(FTPProject project, String path) {
        super(project, path)
        this.type = Type.PROJECT_DIR
        this.project = project
    }


    @Override
    boolean exists() {
        new File(path).exists()
    }


    @Override
    String getTitle() {
        return path.split("/").last()
    }

    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }
}
