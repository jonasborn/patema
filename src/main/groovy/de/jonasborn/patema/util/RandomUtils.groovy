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

package de.jonasborn.patema.util

import java.util.concurrent.ThreadLocalRandom

class RandomUtils {

    private static final Random random = new Random()
    private static final Integer bufferSize = 1024 * 8

    public static int nextInt(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to)
    }

    public static byte[] nextBytes(int length) {
        byte[] temp = new byte[length]
        random.nextBytes(temp)
        return temp
    }

    public static String nextUUID() {
        return UUID.randomUUID().toString()
    }

    public static File fillFile(File file, long size) {
        def name = nextUUID()
        def rounds = (size / bufferSize) as int
        for (i in 0..<rounds) {
            file.append(nextBytes(bufferSize))
        }
        file.append(nextBytes(size - (rounds * bufferSize) as int))
        return file
    }

    public static File nextFile(File directory, long size, boolean deleteOnExit = true) {
        def name = nextUUID()
        def file = new File(directory, name)
        fillFile(file, size)
        if (deleteOnExit) file.deleteOnExit()
        return file
    }

    public static File nextFile(File directory, boolean deleteOnExit = true) {
        def file = new File(directory, UUID.randomUUID().toString())
        if (deleteOnExit) file.deleteOnExit()
        return file;
    }

}
