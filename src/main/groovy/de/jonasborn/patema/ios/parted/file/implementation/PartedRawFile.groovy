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

package de.jonasborn.patema.ios.parted.file.implementation

import de.jonasborn.patema.ios.parted.file.PartedFile
import de.jonasborn.patema.util.PaddingUtils

class PartedRawFile extends PartedFile {

    File directory

    PartedRawFile(File directory) {
        this.directory = directory
    }

    @Override
    File createFile(int index) {
        return new File(directory, index + ".ptma")
    }

    @Override
    List<File> listFiles() {
        List<File> list = this.directory.listFiles()
        if (list == null) return []
        list = list.findAll { it.name.endsWith(".ptma") }
        list.sort(new Comparator<File>()
        {
            @Override
            public int compare(File o1, File o2) {
                def i1 = (o1 as File).name.replaceAll("[^0-9.]", "")
                def i2 = (o2 as File).name.replaceAll("[^0-9.]", "")
                return i1 <=> i2
            }
        })
        return list
    }

    @Override
    Long getSizeOfContent(File file) {
        return file.size()
    }

    @Override
    Long getSizeWithoutPadding(File file) {
        return file.size()
    }

    @Override
    Long getSizeWithPadding(File file) {
        def r = PaddingUtils.calculate(file.length(), 256)
        return r.total
    }

    @Override
    Long getSizeOnMedia(File file) {
        return file.size()
    }

    @Override
    byte[] pack(Integer integer, byte[] data) {
        return data
    }

    @Override
    byte[] unpack(Integer index, byte[] data) {
        return data
    }

    @Override
    String getName() {
        return directory.name
    }

    public InputStream readChunk(int index) {
        def file = new File(directory, index + ".ptma")
        if (!file.exists()) return null
        return new FileInputStream(file)
    }
}
