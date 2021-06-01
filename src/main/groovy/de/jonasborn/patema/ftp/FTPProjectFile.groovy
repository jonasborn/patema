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

import de.jonasborn.patema.io.ChunkedFile
import de.jonasborn.patema.io.ChunkedFileConfig
import de.jonasborn.patema.io.ChunkedInputStream
import de.jonasborn.patema.io.ChunkedOutputStream

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
    ChunkedFile chunkedFile;
    ChunkedOutputStream cout
    ChunkedInputStream cin;

    FTPProjectFile(FTPProject project, String title) {
        super(Type.PROJECT_FILE)
        this.title = title
        this.project = project
        this.delegate = new File(project.delegate, title)
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
        println "DELE"
        println delegate.deleteDir()
    }

    public long getSize() {

        prepare()

        return chunkedFile.getSize();
    }

    private void prepare(FTPConfig config) {
        if (chunkedFile == null) {
            if (!delegate.exists()) delegate.mkdir()
            def c = createConfig(config)
            chunkedFile = new ChunkedFile(c, delegate)
            cin = new ChunkedInputStream(chunkedFile)
            cout = new ChunkedOutputStream(chunkedFile)
        } else {
            chunkedFile.config = createConfig(config)
        }
    }

    public ChunkedInputStream read(FTPConfig config, long start) {
        prepare(config)
        cin.seek(start)
        return cin
    }

    public ChunkedOutputStream write(FTPConfig config, long start) {
        prepare(config)
        cout.seek(start)
        return cout
    }
}
