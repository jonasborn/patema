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
import de.jonasborn.patema.io.ChunkedIOConfig
import de.jonasborn.patema.io.ChunkedInputStream
import de.jonasborn.patema.io.ChunkedOutputStream

class FTPProjectFile extends FTPElement {

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

    public long getSize() {

        prepare()

        return chunkedFile.getSize();
    }

    private void prepare() {
        if (chunkedFile == null) {
            if (!delegate.exists()) delegate.mkdir()
            def config = new ChunkedIOConfig("hallo")
            chunkedFile = new ChunkedFile(config, delegate)
            cin = new ChunkedInputStream(chunkedFile)
            cout = new ChunkedOutputStream(chunkedFile)
        }
    }

    public ChunkedInputStream read(long start) {
        prepare()
        cin.seek(start)
        return cin
    }

    public ChunkedOutputStream write( long start) {
        prepare()
        println "STA: " + start
        cout.seek(start)
        return cout
    }
}
