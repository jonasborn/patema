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

package de.jonasborn.patema.ftp.tape

import com.google.common.io.ByteStreams
import de.jonasborn.patema.ftp.FTPDirectory
import de.jonasborn.patema.ftp.FTPElement
import de.jonasborn.patema.ftp.FTPFile
import de.jonasborn.patema.ftp.FTPRoot
import de.jonasborn.patema.ftp.project.FTPProject
import de.jonasborn.patema.ftp.project.FTPProjectFile
import de.jonasborn.patema.register.RegisterEntry
import de.jonasborn.patema.tape.Tape
import de.jonasborn.patema.tape.Tapes
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.text.DecimalFormat

import static de.jonasborn.patema.ftp.FTPElement.Type.TAPE

public class FTPTape extends FTPDirectory<FTPTapeFile> {

    Logger logger = LogManager.getLogger(FTPTape.class)

    static Map<String, FTPTapeOverview> overviews = [:]

    FTPTapeOverview overview

    FTPRoot root
    String devicePath


    FTPTape(FTPRoot root, String devicePath) {
        super(TAPE)
        this.root = root
        devicePath = devicePath.replaceAll("\\[.*\\]", "")
        if (devicePath.startsWith("tape-")) devicePath = devicePath.replace("tape-", "/dev/")
        this.devicePath = devicePath
        overview = overviews.get(devicePath)
        if (overview == null) {
            overview = new FTPTapeOverview()
            overviews.put(devicePath, overview)
        }
    }

    @Override
    String getPath() {
        return "/" + getTitle()
    }

    @Override
    boolean exists() {
        return Tapes.get(devicePath) != null
    }

    @Override
    FTPElement getParent() {
        return root
    }

    @Override
    String getTitle() {
        def id = devicePath.split("/").last()
        def overview = overviews.get(devicePath)
        if (overview != null && !overview.finished) return "tape-" + id + "[" + overview.message + "]"
        return "tape-" + id
    }

    @Override
    void delete() {
        throw new IOException("Not implemented yet")
    }

    public Tape getDevice() {
        return Tapes.get(devicePath)
    }

    public List<FTPTapeFile> list() {
        logger.debug("Listing files on tape {}", devicePath)
        def device = getDevice()
        device.initialize(false)
        def register = device.readRegister(root.config.password)
        register.getEntries().collect { it ->
            def entry = it as RegisterEntry
            def file = new FTPTapeFile(this, entry.getName())
            file.size = entry.getLength()
            return file
        }
    }


    public void write(FTPProject project) {
        try {
            def device = getDevice()
            if (device == null) throw new IOException("Unable to find device " + getDevicePath())
            def files = project.list()
            def total = files.size() + 3
            overview.initialize(total)

            logger.info("Writing {} to {}", this, devicePath)
            logger.debug("Initializing device {}", devicePath)
            overview.step("Initializing")
            device.initialize()
            logger.debug("Writing register from {} to {}", this, tape)
            overview.step("Writing register")
            device.writeRegister(project.register, root.config.password)
            overview.step("Wrote register")
            logger.debug("Successfully wrote register {} to {}", this, tape)
            //Load all files from directory, each as a PartedRawFile and then dump them to the tape with markers

            for (i in 0..<files.size()) {
                def file = files[i]
                overview.step("Writing file " + file.getTitle())
                device.write(project.register, i, file.readRaw(0))
            }


        } catch (Exception e) {
            overview.fail("Unable to write")
            e.printStackTrace()
        }
    }

}

