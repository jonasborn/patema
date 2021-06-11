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

import com.guichaguri.minimalftp.FTPConnection
import com.guichaguri.minimalftp.api.IFileSystem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class FTPFileSystem implements IFileSystem<FTPElement> {

    Logger logger;
    FTPConnection connection
    FTPConfig config
    FTPFileFactory factory;

    FTPFileSystem(FTPConfig config, FTPConnection connection, File directory) {
        this.config = config
        factory = new FTPFileFactory(config, directory)
        this.connection = connection
        logger = LogManager.getLogger("FTPFS-" + connection.getUsername())
    }

    @Override
    FTPElement getRoot() {
        return factory.root()
    }

    @Override
    String getPath(FTPElement file) {
        logger.debug("Get path? {}: {}", file, file.getPath())
        return file.getPath()
    }

    @Override
    boolean exists(FTPElement file) {
        logger.debug("Exists? {}: {}", file, file.exists())
        return file.exists()
    }

    @Override
    boolean isDirectory(FTPElement file) {
        logger.debug("Is directory? {}: {}", file, file.isDirectory())
        return file.isDirectory()
    }

    @Override
    int getPermissions(FTPElement file) {
        logger.debug("Permissions? {}: {}", file, 0)
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
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1)
        path = path.replaceAll("/{2,}", "/")
        return factory.find(path)
    }

    @Override
    FTPElement findFile(FTPElement cwd, String path) throws IOException {
        def found = findFile(cwd.path + "/" + path)
        logger.debug("Find File? Cwd {}, path {}. Found: {}", cwd, path, found)
        return found
    }

    @Override
    InputStream readFile(FTPElement file, long start) throws IOException {
        logger.debug("Read! File {} starting at {}", file, start)
        if (file.isProjectFile()) return file.asProjectFile().read(start)
        throw new IOException("File access not allowed")
    }

    @Override
    OutputStream writeFile(FTPElement file, long start) throws IOException {
        logger.debug("Write! File {} starting at {}", file, start)
        if (file.isProjectFile()) return file.asProjectFile().write(start)
        throw new IOException("File access not allowed")
    }

    @Override
    void mkdirs(FTPElement file) throws IOException {
        logger.debug("Mkdir? File {}", file)
        if (file.isProject()) file.asProject().mkdir()
        else throw new IOException("Only projects are allowed")
    }

    @Override
    void delete(FTPElement file) throws IOException {
        logger.debug("Delete? File {}", file)
        file.delete()
    }

    @Override
    void rename(FTPElement from, FTPElement to) throws IOException {
        if (from.isProject() && to.parent.isTape()) {
            def project = from.asProject()
            if (!project.locked) {
                //RUN ASYNC
                project.locked = true
                project.write(to.getParent().asTape())
                project.locked = false
            } else {
                throw new IOException("Project is currently in use and locked, please wait for the last operation to complete")
            }
        } else {
            throw new IOException("Not allowed")
        }
    }

    @Override
    void chmod(FTPElement file, int perms) throws IOException {

    }

    @Override
    void touch(FTPElement file, long time) throws IOException {

    }

    @Override
    void finishedFile(FTPElement file) {
        if (file.isProjectFile()) file.asProjectFile().finished()
    }
}
