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

package de.jonasborn.patema.io

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import de.jonasborn.patema.io.crypto.PartedCrypto
import de.jonasborn.patema.io.crypto.PartedECBCrypto
import de.jonasborn.patema.util.XZUtils

import java.util.concurrent.TimeUnit

class PartedCompressedCryptoFile extends PartedFile{

    private static LoadingCache<String, PartedCrypto> cryptoCache = CacheBuilder.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES).build(new CacheLoader<String, PartedCrypto>() {
        @Override
        PartedCrypto load(String key) throws Exception {
            def c = new PartedECBCrypto();
            c.setPassword(key)
            return c
        }
    })

    File directory
    PartedCrypto crypto;

    PartedCompressedCryptoFile(File directory, String password) {
        this.directory = directory
        crypto = cryptoCache.get(password)
    }

    @Override
    File createFile(int index) {
        return new File(directory, index + ".ptma")
    }

    @Override
    List<File> listFiles() {
        List<File> list = this.directory.listFiles()
        if (list == null) return []
        list = list.findAll {it.name.endsWith(".ptma")}
        list.sort(new Comparator<File>()
        {
            @Override
            public int compare(File o1, File o2) {
                def i1 = (o1 as File).name.replaceAll("[^0-9]", "")
                def i2 = (o2 as File).name.replaceAll("[^0-9]", "")

                return Integer.parseInt(i1) <=> Integer.parseInt(i2)
            }
        })
        return list
    }



    @Override
    Long getSize(File file) {
        return file.lastModified()
    }

    private int getIndex(File file) {

    }

    @Override
    byte[] pack(Integer integer, byte[] data) {
        crypto.encrypt(integer, XZUtils.compress(data))
    }

    @Override
    byte[] unpack(Integer integer, byte[] data) {
        XZUtils.decompress(crypto.decrypt(integer, data))
    }

    @Override
    void close() {
        def files = listFiles()
        for (int i = 0; i < files.size(); i++) {
            final file = files[i]
            def data = unpack(i, file.bytes)
            file.setLastModified(data.size())
        }
    }

}
