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

package de.jonasborn.patema.tape

import de.jonasborn.patema.info.Sys
import jtape.BasicTapeDevice
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class Tapes {

    private static Logger logger = LogManager.getLogger(Tapes.class)

    private static final directory = new File("/dev/tape/by-id/")

    public static void checkRequirements() {
        //TODO Check if /dev/tape exists when no device is attached
    }

    public static Tape get(String path) {
        return list().find { it.path = path }
    }

    private static String extractId(File file) {
        try {
            def name = file.name
            return name.split("-")[1]
        } catch (Exception ignored) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    public static List<Tape> list() {
        directory.listFiles().findAll { it.path.contains("nst") }.collect {
            if (it.exists()) {
                try {
                    logger.debug("Preparing tape ${it.getCanonicalPath()}")
                    return new Tape(extractId(it), it.getCanonicalPath())
                } catch (Exception e) {
                    logger.warn("Unable to prepare tape ${it.getPath()}: ${e.getMessage()}")
                }
            }
            return null
        }.findAll { it != null }
    }

    static void main(String[] args) {

        //BasicTapeDevice.prepare()

        println new File("/dev/tape/by-id/").listFiles()

        def path = new File("/dev/tape/by-id/scsi-350050763120c95a7-nst").getCanonicalPath()
        println path
        def bt = new BasicTapeDevice(path)

        println bt.status
    }

}
