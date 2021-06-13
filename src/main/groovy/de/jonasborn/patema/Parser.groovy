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

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace

class Parser {

    private static Namespace namespace;

    public static void prepare(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("patema").build()
                .defaultHelp(true)
                .description("Patema automated tape encoding management algorithms")
        parser.addArgument("-c", "--config")
                .setDefault("config.json")
                .help("Config file to use")
        parser.addArgument("-l", "--level")
                .choices(
                        "off",
                        "fatal",
                        "error",
                        "warn",
                        "info",
                        "debug",
                        "trace",
                        "all"
                )
                .setDefault("info")
                .help("Log level to use")
        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .setDefault(2121)
                .help("Port to use")

        try {
            namespace = parser.parseArgs(args)
        } catch (ArgumentParserException e) {
            parser.handleError(e)
            System.exit(1)
        }
    }

    public static String getString(String key) {
        return namespace.getString(key)
    }

    public static Integer getInteger(String key) {
        return namespace.getInt(key)
    }

}
