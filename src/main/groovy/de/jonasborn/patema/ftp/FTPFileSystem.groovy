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

import com.guichaguri.minimalftp.api.IFileSystem

class FTPFileSystem implements IFileSystem<FTPElement> {

    FTPFileFactory factory;

    FTPFileSystem(File directory) {
        factory = new FTPFileFactory(directory)
    }

    @Override
    FTPElement getRoot() {
        return factory.root()
    }

    @Override
    String getPath(FTPElement file) {
        println "GP: " + file.getPath()
        return file.getPath()
    }

    @Override
    boolean exists(FTPElement file) {
        println "EX: " + file
        return file.exists()
    }

    @Override
    boolean isDirectory(FTPElement file) {
        println "DI: " + file + " - " + file.isDirectory()
        return file.isDirectory()
    }

    @Override
    int getPermissions(FTPElement file) {
        return 0
    }

    @Override
    long getSize(FTPElement file) {
        if (file.isProjectFile()) return file.asProjectFile().size
        if (file.isTapeFile()) return file.asTapeFile().size
        return 0
    }

    @Override
    long getLastModified(FTPElement file) {
        return 0
    }

    @Override
    int getHardLinks(FTPElement file) {
        return 0
    }

    @Override
    String getName(FTPElement file) {
        return file.title
    }

    @Override
    String getOwner(FTPElement file) {
        return null
    }

    @Override
    String getGroup(FTPElement file) {
        return null
    }

    @Override
    FTPElement getParent(FTPElement file) throws IOException {
        return file.parent
    }

    @Override
    FTPElement[] listFiles(FTPElement dir) throws IOException {
        if (dir.isDirectory()) return dir.asDirectory().list()
        throw new IOException("Not a directory, can not list")
    }

    @Override
    FTPElement findFile(String path) throws IOException {
        return factory.find(path)
    }

    @Override
    FTPElement findFile(FTPElement cwd, String path) throws IOException {
        println "CWD: " + cwd + ", PTH: " + path + ", FFI: " + findFile(cwd.path + path)
        return findFile(cwd.path + path)
    }

    @Override
    InputStream readFile(FTPElement file, long start) throws IOException {
        return null
    }

    @Override
    OutputStream writeFile(FTPElement file, long start) throws IOException {
        return null
    }

    @Override
    void mkdirs(FTPElement file) throws IOException {
        println file.path
    }

    @Override
    void delete(FTPElement file) throws IOException {

    }

    @Override
    void rename(FTPElement from, FTPElement to) throws IOException {

    }

    @Override
    void chmod(FTPElement file, int perms) throws IOException {

    }

    @Override
    void touch(FTPElement file, long time) throws IOException {

    }

    @Override
    void finishedFile(FTPElement file) {

    }
}
