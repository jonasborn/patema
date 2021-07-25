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

import de.jonasborn.patema.ftp.FTPDirectory
import de.jonasborn.patema.ftp.FTPElement

class FTPProjectElement extends FTPDirectory<FTPProjectElement> {

    public static FTPElement create(FTPProject project, String path) {
        def file = new File(project.delegate, path)
        println "FILE: " + file
        def dir = FTPProject.isLogicalDirectory(file);
        def parts = path.split("/")

        println "!!! - - - UNTESTED FUNCTION"
        def current = project
        for (i in 0..<parts.length) {
            println "ELEMENT IN LOOP IS " + parts[i]
            println "IS END? " + (i == parts.length -1)
            if (i == parts.length -1) {
                println "END!, CREATING " + parts[i]
                if (dir) current = project.root.factory.projectDir(project, current, parts[i])
                    else current = project.root.factory.projectFile(project, current, parts[i])
            } else {
                println "RUNNING, CREATING DIR " + parts[i]
                current = project.root.factory.projectDir(project, current, parts[i])
            }
        }
        return current
    }

    File delegate
    FTPProject project
    FTPElement parent
    String title

    FTPProjectElement(FTPProject project, FTPElement parent, String title) {
        super(Type.PROJECT_ELEMENT)
        assert project != null
        assert  parent != null
        assert title != null
        this.project = project
        this.parent = parent
        this.title = title
        this.delegate = new File(project.delegate.parentFile, getPath())
    }

    @Override
    String getPath() {
        def current = this
        def path = ""
        while (!(current instanceof FTPProject)) {
            path = current.title + "/" + path
            current = current.getParent()
        }
        path = "/" + (current as FTPProject).name + path
        return path
    }

    @Override
    boolean exists() {
        return delegate.exists()
    }

    @Override
    boolean isFile() {
        return !FTPProject.isLogicalDirectory(delegate)
    }

    @Override
    boolean isDirectory() {
        return FTPProject.isLogicalDirectory(delegate)
    }

    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }

    @Override
    List<FTPProjectElement> list() {
        return null
    }
}
