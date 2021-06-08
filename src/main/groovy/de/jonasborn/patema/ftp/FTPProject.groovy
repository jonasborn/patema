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

import com.google.common.io.BaseEncoding
import de.jonasborn.patema.tape.TapeDescription
import de.jonasborn.patema.util.FileUtils

import static de.jonasborn.patema.ftp.FTPElement.Type.PROJECT

public class FTPProject extends FTPDirectory<FTPProjectFile> {


    File delegate
    FTPRoot root;
    String name
    private boolean locked = false;


    FTPProject(FTPRoot root, String name) {
        super(PROJECT)
        this.root = root
        this.name = name.replaceAll("\\[.*\\]", "")
        this.delegate = new File(root.delegate, this.name)
    }


    public void lock() {
        locked = true
    }

    public void unlock() {
        locked = false
    }

    @Override
    String getPath() {
        println "PATH" + "/" + getTitle()
        return "/" + getTitle()
    }

    @Override
    boolean exists() {
        new File(root.delegate, name).exists()
    }

    public void mkdir() {
        new File(root.delegate, name).mkdir()
    }

    @Override
    FTPElement getParent() {
        return root
    }

    @Override
    String getTitle() {
        return name + "[" + FileUtils.humanReadableByteCountBin(getSize()) + "]"
    }

    public long getSize() {
        Long size = list().collect { it.size }.sum() as Long
        if (size == null) return 0
        return size
    }

    public void write(FTPTape tape) {
        list().each {
            def hash = it.hash()
            println BaseEncoding.base32().encode(hash)
            tape.device.description.add(
                    it.getTitle(), hash, it.size
            )
        }
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
        if (!delegate.exists()) return []
        delegate.listFiles().collect {
            return new FTPProjectFile(this, it.name)
        }
    }


}
