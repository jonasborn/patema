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

package de.jonasborn.patema

import com.guichaguri.minimalftp.FTPServer
import de.jonasborn.patema.config.Config
import de.jonasborn.patema.ftp.FTPAuth
import de.jonasborn.patema.util.LogUtils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.LoggerConfig


class Main {

    static Logger logger = LogManager.getLogger(Main.class)

    public static void main(String[] args) {

        println "         _                 ";
        println " ___ ___| |_ ___ _____ ___ ";
        println "| . | .'|  _| -_|     | .'|";
        println "|  _|__,|_| |___|_|_|_|__,|";
        println "|_|                        ";

        Parser.prepare(args)

        Config.loadConfig(Parser.getString("config"))

        LogUtils.setRootLevel(Parser.getString("level").toUpperCase())
        
        logger.debug("Debugging active")

        File root = new File("root");
        def auth = new FTPAuth(root);
        FTPServer server = new FTPServer(auth);
        logger.info("Starting ftp server using port {}", Parser.getInteger("port"))
        server.listenSync(Parser.getInteger("port"))


    }
}
