package de.jonasborn.patema.util
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

class RuntimeUtils {

    public static void checkRequirements() {

    }


    public static List<String> execute(String... command) {
        ProcessBuilder builder = new ProcessBuilder(command)
        def p = builder.start()
        def s = new Scanner(p.getInputStream())
        def o = []

        while (s.hasNext()) o.add(s.nextLine())
        p.waitFor();
        return o
    }
}