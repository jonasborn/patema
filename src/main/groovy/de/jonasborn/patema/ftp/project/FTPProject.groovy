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

import de.jonasborn.patema.ftp.FTPDirectory
import de.jonasborn.patema.ftp.FTPElement
import de.jonasborn.patema.ftp.FTPRoot
import de.jonasborn.patema.ftp.tape.FTPTape
import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import de.jonasborn.patema.register.V1Register
import de.jonasborn.patema.register.V1RegisterEntry
import de.jonasborn.patema.util.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import static de.jonasborn.patema.ftp.FTPElement.Type.PROJECT

public class FTPProject extends FTPDirectory<FTPProjectFile> {


    Logger logger = LogManager.getLogger(FTPProject.class)

    File delegate
    FTPRoot root;
    String name
    public boolean locked = false;
    public boolean accessible = true
    File tokenFile

    V1Register register
    File registerFile

    FTPProject(FTPRoot root, String name) {
        super(PROJECT)
        this.root = root
        this.name = name.replaceAll("\\[.*\\]", "")
        this.delegate = new File(root.delegate, this.name)

        this.registerFile = new File(delegate, "register.ptmas")
        this.tokenFile = new File(delegate, "token.patmas")
        try {
            prepare()
        } catch (Exception e) {
            logger.info("Unable to access project {} for user {}", name, root.config.username, e)
            accessible = false
        }
    }

    private void prepare() {
        try {
            if (register == null) {
                register = readRegister()
                logger.debug("Loading register from file", this)
            }

            if (register == null) {
                register = new V1Register(getTitle(), root.config.password)
                writeRegister()
                logger.debug("Created new register for {}", this)
            }
        } catch (Exception e) {
            logger.debug("Unable to create load/create register", this)
            throw new ProjectAccessException("Unable to access project, wrong password or corrupted data", e)
        }
    }

    @Override
    String getPath() {
        return "/" + name //getTitle()
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
        if (locked) return name + "[locked]"
        if (!accessible) return name + "[inaccessible]"
        return name + "[" + FileUtils.humanReadableByteCountBin(getSize()) + "]"
    }

    public long getSize() {
        Long size = list().findAll {it.isFile()}.collect { it.size }.sum() as Long
        if (size == null) return 0
        return size
    }

    public void write(FTPTape tape) throws Exception {

    }

    @Override
    void delete() {
        def files = delegate.listFiles()
        if (files != null) files.each {
            it.delete()
        }
        delegate.deleteDir()
    }

    /**
     * Used to determine if a directory is holding a parted file or not
     * @param file A file, should be a directory
     * @return True if file is a real directory
     */
    public static boolean isLogicalDirectory(File file) {
        if (!file.isDirectory()) return false
        def list = FileUtils.list(file)
        def entry = list.find { it.name.contains(".ptma") }
        if (entry == null) return true
        return false
    }

    public List<FTPProjectElement> list() {
        if (!delegate.exists() || !accessible) return []
        return FileUtils.list(delegate).findAll { it.isDirectory() }.collect {
            if (isLogicalDirectory(it)) return new FTPProjectDirectory(this, this, it.name)
            else return new FTPProjectFile(this, this, it.name)
        }

    }

    /*
    Must this really be places here?
    10.06.21 Yes!
     */

    public void registerFile(FTPProjectFile file) {
        prepare()
        def parts = new LinkedList(file.partedRaw.listFiles().collect { it.length() })
        V1RegisterEntry entry = new V1RegisterEntry(
                file.title, register.getEntries().size(), file.partedCrypto.hash(), file.getSize(), parts, file.partedCrypto.getSizeOnMedia(), root.config.password
        )
        register.addEntry(entry)
        writeRegister()
    }

    public void unregisterFile(FTPProjectFile file) {
        prepare()
        def parts = file.partedRaw.listFiles().collect { it.length() }
        V1RegisterEntry entry = new V1RegisterEntry(
                file.title, register.getEntries().size(), file.partedCrypto.hash(), file.getSize(), parts, file.partedCrypto.getSizeOnMedia(), root.config.password
        )
        register.removeEntry(entry)
        writeRegister()
    }

    public Register readRegister() {
        if (!registerFile.exists()) return null
        def data = registerFile.bytes
        return Registers.unpack(data, root.config.password)
    }

    public void writeRegister() {
        def data = Registers.pack(register, root.config.password)
        registerFile.setBytes(data);
    }

    public static class ProjectAccessException extends IOException {
        ProjectAccessException(String message) {
            super(message)
        }

        ProjectAccessException(String message, Throwable cause) {
            super(message, cause)
        }
    }


}
