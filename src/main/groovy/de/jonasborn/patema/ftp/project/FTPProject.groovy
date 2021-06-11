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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.google.common.io.BaseEncoding
import de.jonasborn.patema.ftp.FTPDirectory
import de.jonasborn.patema.ftp.FTPElement
import de.jonasborn.patema.ftp.FTPRoot
import de.jonasborn.patema.ftp.tape.FTPTape

import de.jonasborn.patema.register.Register
import de.jonasborn.patema.register.Registers
import de.jonasborn.patema.register.V1Register
import de.jonasborn.patema.register.V1RegisterEntry
import de.jonasborn.patema.util.FileUtils

import java.util.logging.Logger

import static de.jonasborn.patema.ftp.FTPElement.Type.PROJECT

public class FTPProject extends FTPDirectory<FTPProjectFile> {


    Logger logger;

    File delegate
    FTPRoot root;
    String name
    public boolean locked = false;
    public boolean accessable = true
    File tokenFile

    V1Register register
    File registerFile

    FTPProject(FTPRoot root, String name) {
        super(PROJECT)
        this.root = root
        this.name = name.replaceAll("\\[.*\\]", "")
        this.delegate = new File(root.delegate, this.name)
        this.logger = Logger.getLogger("FTPProject-" + name)
        this.registerFile = new File(delegate, "register.ptmas")
        this.tokenFile = new File(delegate, "token.patmas")
        try {
            prepare()
        } catch (Exception e) {
            accessable = false
        }
    }

    private void prepare() {
        try {
            if (register == null) register = readRegister()
            if (register == null) {
                register = new V1Register(getTitle())
                writeRegister()
            }
        } catch (Exception e) {
            throw new ProjectAccessException("Unable to access project, wrong password or corrupted data")
        }
    }

    @Override
    String getPath() {

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
        if (locked) return name + "[locked]"
        if (!accessable) return name + "[inaccessible]"
        return name + "[" + FileUtils.humanReadableByteCountBin(getSize()) + "]"
    }

    public long getSize() {
        Long size = list().collect { it.size }.sum() as Long
        if (size == null) return 0
        return size
    }

    public void write(FTPTape tape) throws Exception {
        try {
            def device = tape.getDevice()
            if (device == null) throw new IOException("Unable to find device " + tape.getDevicePath())
            device.initialize()
            println "WRITING REGISTER"
            device.writeRegister(register, root.config.password)
            println "REGISTER WRITTEN"
        } catch (Exception e) {
            e.printStackTrace()
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
        if (!delegate.exists() || !accessable) return []
        delegate.listFiles().collect {
            return new FTPProjectFile(this, it.name)
        }
    }

    /*
    Must this really be places here?
    10.06.21 Yes!
     */

    public void registerFile(FTPProjectFile file) {
        prepare()
        V1RegisterEntry entry = new V1RegisterEntry(
                file.title, register.getEntries().size(), file.parted.hash(), file.getSize(), file.parted.getSizeOnMedia(), root.config.password
        )
        register.addEntry(entry)
        writeRegister()
    }

    public void unregisterFile(FTPProjectFile file) {
        prepare()
        V1RegisterEntry entry = new V1RegisterEntry(
                file.title, register.getEntries().size(), file.parted.hash(), file.getSize(), file.parted.getSizeOnMedia(), root.config.password
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
    }


}
