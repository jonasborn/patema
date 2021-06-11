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
import de.jonasborn.patema.io.PartedFileInputStream
import de.jonasborn.patema.io.PartedFileOutputStream
import de.jonasborn.patema.io.PartedCompressedCryptoFile

import groovy.transform.CompileStatic

import java.security.MessageDigest

class FTPProjectFile extends FTPElement {

    File delegate;
    FTPProject project;
    String title;
    PartedCompressedCryptoFile parted;
    PartedFileOutputStream cout
    PartedFileInputStream cin;

    /**
     * Will initialize the underlying chunked file and create
     * the needed directory if not exists
     * The underlying chunked file will be initialized using the current
     * ftp config
     * @param project
     * @param title
     */
    FTPProjectFile(FTPProject project, String title) {
        super(Type.PROJECT_FILE)
        this.title = title
        this.project = project
        this.delegate = new File(project.delegate, title)
        if (!delegate.exists()) delegate.mkdir()
        this.parted = new PartedCompressedCryptoFile(
                delegate,
                project.getRoot().getConfig().getPassword(),
                project.register.iv,
                project.register.salt
        )
    }

    @Override
    String getPath() {
        return project.getPath() + "/" + title
    }

    @Override
    boolean exists() {
        return delegate.exists()
    }

    @Override
    FTPElement getParent() {
        return project
    }

    @Override
    void delete() throws IOException {
        project.unregisterFile(this)
        delegate.deleteDir()
    }

    public Long getSize() {
        def size = parted.getSize()
        if (size == null) return -1
        return size
    }

    public long getSizeOnMedia() {
        return parted.getSizeOnMedia()
    }

    public void finished() {
        project.registerFile(this)
    }

    public byte[] hash() {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        def is = read(0)
        def read = 0;
        def buffer = new byte[1024]
        while ((read = is.read(buffer)) != -1) {
            digest.update(buffer, 0, read)
        }
        return digest.digest()
    }

    @CompileStatic
    public PartedFileInputStream read(long start) {
        if (cin == null) cin = new PartedFileInputStream(parted)
        cin.seek(start)
        return cin
    }

    @CompileStatic
    public PartedFileOutputStream write(long start) {
        if (cout == null) cout = new PartedFileOutputStream(parted)
        cout.seek(start)
        return cout
    }
}
