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

class FTPProjectElement extends FTPElement {

    File delegate
    FTPProject parent
    String title

    FTPProjectElement(FTPProject project, String title) {
        super(Type.PROJECT_ELEMENT)
        this.delegate = new File(project.delegate, title)
    }

    @Override
    String getPath() {
        return null
    }

    @Override
    boolean exists() {
        return delegate.exists()
    }

    @Override
    String getTitle() {
        return delegate.getName()
    }

    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }
}
