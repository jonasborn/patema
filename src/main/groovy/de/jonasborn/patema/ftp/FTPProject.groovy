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

import static de.jonasborn.patema.ftp.FTPElement.Type.PROJECT

public class FTPProject extends FTPDirectory<FTPProjectFile> {

    private static String fixName(String input) {
        return (input.replaceAll("project(-|\\[.*]-)", ""))
    }

    File delegate
    FTPRoot root;
    String name
    FTPProject(FTPRoot root, String name) {
        super(PROJECT)
        println name
        this.root = root
        this.name = name
        this.delegate = new File(root.delegate, name)
    }


    @Override
    String getPath() {
        return "/" + name
    }

    @Override
    boolean exists() {
        new File(root.delegate, name).exists()
    }

    @Override
    FTPElement getParent() {
        return root
    }

    @Override
    String getTitle() {
        return name
    }

    @Override
    void delete() {
        def files = delegate.listFiles()
        if (files != null) files.each {
            it.delete()
        }
        delegate.deleteDir()
    }

    public List<FTPProjectFile> list() {
        if (!delegate.exists()) throw new IOException("Unable to find project " + delegate.path)
        delegate.listFiles().collect {
            return new FTPProjectFile(this, it.name)
        }
    }



}
