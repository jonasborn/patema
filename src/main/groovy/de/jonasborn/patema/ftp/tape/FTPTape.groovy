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

import de.jonasborn.patema.ftp.FTPDirectory
import de.jonasborn.patema.ftp.FTPElement
import de.jonasborn.patema.ftp.FTPRoot
import de.jonasborn.patema.ftp.project.FTPProject
import de.jonasborn.patema.tape.Tape
import de.jonasborn.patema.tape.Tapes
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.text.DecimalFormat

import static de.jonasborn.patema.ftp.FTPElement.Type.TAPE

public class FTPTape extends FTPDirectory<FTPTapeFile> {

    Logger logger = LogManager.getLogger(FTPTape.class)

    static Map<String, String> overviews = [:]

    FTPRoot root
    String devicePath


    FTPTape(FTPRoot root, String devicePath) {
        super(TAPE)
        this.root = root
        devicePath = devicePath.replaceAll("\\[.*\\]", "")
        if (devicePath.startsWith("tape-")) devicePath = devicePath.replace("tape-", "/dev/")
        this.devicePath = devicePath

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
        if (overview != null) return "tape-" + id + "[" + overview + "]"
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
        return [
                new FTPTapeFile(this, "a"),
                new FTPTapeFile(this, "b"),
                new FTPTapeFile(this, "c"),
        ]
    }

    private void setOverview(double percent, String message) {
        def data = new DecimalFormat("#.##").format(percent) + "% " + message
        overviews.put(devicePath, data)
    }

    private void clearOverview() {
        overviews.remove(devicePath) //TODO CHECK EXCEPTION
    }

    public void write(FTPProject project) {
        try {
            def device = getDevice()
            if (device == null) throw new IOException("Unable to find device " + getDevicePath())
            logger.info("Writing {} to {}", this, devicePath)
            logger.debug("Initializing device {}", devicePath)
            setOverview(10, "Initializing")
            device.initialize()
            logger.debug("Writing register from {} to {}", this, tape)
            setOverview(30, "Write register")
            device.writeRegister(project.register, root.config.password)
            setOverview(90, "Wrote register")
            logger.debug("Successfully wrote register {} to {}", this, tape)
            setOverview(100, "Finished")
            //Load all files from directory, each as a PartedRawFile and then dump them to the tape with markers

        } catch (Exception e) {
            setOverview(-1, "Unable to write")
            e.printStackTrace()
        }
    }

}

