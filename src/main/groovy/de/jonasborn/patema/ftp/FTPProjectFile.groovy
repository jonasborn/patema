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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import de.jonasborn.patema.io.PartedFile
import de.jonasborn.patema.io.PartedInputStream
import de.jonasborn.patema.io.PartedOutputStream
import de.jonasborn.patema.io.UnPackedFile
import de.jonasborn.patema.io.UnPackedIO
import de.jonasborn.patema.io.chunked.ChunkedFile
import de.jonasborn.patema.io.chunked.ChunkedFileConfig
import de.jonasborn.patema.io.chunked.ChunkedInputStream
import de.jonasborn.patema.io.chunked.ChunkedOutputStream
import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

class FTPProjectFile extends FTPElement {

    private static ChunkedFileConfig createConfig(FTPConfig config) {
        def c = new ChunkedFileConfig(config.password)
        c.blockSize = config.blockSize
        c.compress = config.compress
        c.encrypt = config.encrypt
        c.annoying = config.annoying
        return c
    }

    File delegate;
    FTPProject project;
    String title;
    UnPackedFile unPackedFile;
    PartedOutputStream cout
    PartedInputStream cin;

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
        UnPackedIO io = new UnPackedIO(createConfig(project.getRoot().config))
        this.unPackedFile = new UnPackedFile(delegate, io)
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
    void delete() {
        println delegate.deleteDir()
    }

    public long getSize() {
        return unPackedFile.getSize();
    }

    @CompileStatic
    public PartedInputStream read(FTPConfig config, long start) {
        if (cin == null) cin = new PartedInputStream(unPackedFile)
        cin.seek(start)
        return cin
    }

    @CompileStatic
    public PartedOutputStream write(FTPConfig config, long start) {
        if (cout == null) cout = new PartedOutputStream(unPackedFile)
        cout.seek(start)
        return cout
    }
}
