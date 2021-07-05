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
import de.jonasborn.patema.ftp.tape.FTPTape
import de.jonasborn.patema.tape.Tapes

import static de.jonasborn.patema.ftp.FTPElement.Type.ROOT

public class FTPRoot extends FTPDirectory<FTPElement> {

    FTPFileFactory factory
    FTPConfig config
    File delegate

    FTPRoot(FTPFileFactory factory, FTPConfig config, File delegate) {
        super(ROOT)
        assert delegate != null
        this.config = config
        this.delegate = delegate
        this.factory = factory
    }

    @Override
    String getPath() {
        return "/"
    }

    @Override
    boolean exists() {
        return true
    }

    @Override
    FTPElement getParent() {
        return this //Or null or sth. else
    }

    @Override
    String getTitle() {
        return "root"
    }

    @Override
    void delete() {

    }

    @Override
    List<FTPElement> list() {
        def list = []
        list.addAll delegate.listFiles().findAll { it.isDirectory() }.collect() {
            return new FTPProject(this, it.name)
        }
        list.addAll(
                Tapes.list().collect {factory.tape(it.path)}
        )
        return list
    }
}