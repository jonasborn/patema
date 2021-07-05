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
import de.jonasborn.patema.ios.parted.file.PartedFileInputStream
import de.jonasborn.patema.ios.parted.file.PartedFileOutputStream
import de.jonasborn.patema.ios.parted.file.implementation.PartedCompressedCryptoFile
import de.jonasborn.patema.ios.parted.file.implementation.PartedRawFile
import groovy.transform.CompileStatic

import java.security.MessageDigest

class FTPProjectFile extends FTPProjectElement {

    File delegate;
    FTPProject project;
    String title;
    PartedCompressedCryptoFile partedCrypto
    PartedFileOutputStream coutCrypto
    PartedFileInputStream cinCrypto;
    PartedRawFile partedRaw
    PartedFileInputStream cinRaw

    /**
     * Will initialize the underlying chunked file and create
     * the needed directory if not exists
     * The underlying chunked file will be initialized using the current
     * ftp config
     * @param project
     * @param path
     */
    FTPProjectFile(FTPProject project, String path) {
        super(project, path)
        this.title = path
        this.project = project
        this.delegate = new File(project.delegate, path)
        if (!delegate.exists()) delegate.mkdir()
        this.partedCrypto = new PartedCompressedCryptoFile(
                delegate,
                project.register.getPassword(path),
                project.register.getIv(path),
                project.register.getSalt(path)
        )
        this.partedRaw = new PartedRawFile(delegate)
    }

    @Override
    String getPath() {
        return project.getPath() + "/" + this.title
    }

    @Override
    boolean exists() {
        return delegate.exists()
    }



    @Override
    void delete() throws IOException {
        project.unregisterFile(this)
        delegate.deleteDir()
    }

    public Long getSize() {
        def size = partedCrypto.getSizeWithPadding()
        if (size == null) return -1
        return size
    }

    public long getSizeOnMedia() {
        return partedCrypto.getSizeOnMedia()
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

    public PartedFileInputStream readRaw(long start) {
        if (cinRaw == null) cinRaw = new PartedFileInputStream(partedRaw)
        partedRaw.seek(start)
        return cinRaw
    }

    public int getRawChunkAmount() {
        return partedRaw.listFiles().size()
    }

    public InputStream readRawChunk(int chunk) {
        return partedRaw.readChunk(chunk)
    }

    @CompileStatic
    public PartedFileInputStream read(long start) {
        if (cinCrypto == null) cinCrypto = new PartedFileInputStream(partedCrypto)
        cinCrypto.seek(start)
        return cinCrypto
    }

    @CompileStatic
    public PartedFileOutputStream write(long start) {
        if (coutCrypto == null) coutCrypto = new PartedFileOutputStream(partedCrypto)
        coutCrypto.seek(start)
        return coutCrypto
    }
}
