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
    String username

    FTPFileSystem(FTPConfig config, FTPConnection connection, File directory) {
        this.config = config
        factory = new FTPFileFactory(config, directory)
        this.connection = connection
        this.username = connection.getUsername()
        logger = LogManager.getLogger(FTPFileSystem.class)
    }

    @Override
    FTPElement getRoot() {
        return factory.root()
    }

    @Override
    String getPath(FTPElement file) {
        logger.debug("{} is requesting path for {}: {}", username, file, file.getPath())
        return file.getPath()
    }

    @Override
    boolean exists(FTPElement file) {
        logger.debug("{} is requesting exists for {}: {}", username, file, file.exists())
        return file.exists()
    }

    @Override
    boolean isDirectory(FTPElement file) {
        logger.debug("{} is requesting is directory for {}: {}", username, file, file.isDirectory())
        return file.isDirectory()
    }

    @Override
    int getPermissions(FTPElement file) {
        logger.debug("{} is requesting permissions for {}: {}", username, file, 0)
        return 0
    }

    @Override
    long getSize(FTPElement file) {
        logger.debug("{} is requesting size for {}", username, file)
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
        logger.debug("{} is requesting list of files for {}", username, dir)
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
        logger.debug("{} is searching file under cwd {} with path {}. Found: {}", username, cwd, path, found)
        return found
    }

    @Override
    InputStream readFile(FTPElement file, long start) throws IOException {
        logger.debug("{} is reading file {} starting at {}", username, file, start)
        if (file.isProjectFile()) return file.asProjectFile().read(start)
        throw new IOException("File access not allowed")
    }

    @Override
    OutputStream writeFile(FTPElement file, long start) throws IOException {
        logger.debug("{} is writing file {} starting at {}", username, file, start)
        if (file.isProjectFile()) return file.asProjectFile().write(start)
        throw new IOException("File access not allowed")
    }

    @Override
    void mkdirs(FTPElement file) throws IOException {
        logger.debug("{} is creating dir {}", username, file)
        if (file.isProject()) file.asProject().mkdir()
        else throw new IOException("Only projects are allowed")
    }

    @Override
    void delete(FTPElement file) throws IOException {
        logger.debug("{} is deleting file {}", username, file)
        file.delete()
    }

    @Override
    void rename(FTPElement from, FTPElement to) throws IOException {
        logger.debug("{} is requesting rename from {} to {}", username, from, to)
        if (from.isProject() && to.parent.isTape()) {
            def project = from.asProject()
            if (!project.locked) {
                //RUN ASYNC
                logger.info("{} is triggering to write {} to {}", username, from, to)
                project.locked = true
                Thread.start {
                    to.getParent().asTape().write(project)
                    project.locked = false
                }
            } else {
                logger.warn("{} is using project {}, which is currently locked", username, from)
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
        logger.info("{} successfully uploaded file {}", username, file)
        if (file.isProjectFile()) file.asProjectFile().finished()
    }
}
