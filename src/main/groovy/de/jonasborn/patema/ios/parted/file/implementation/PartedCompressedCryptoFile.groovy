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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import de.jonasborn.patema.crypto.Crypto
import de.jonasborn.patema.crypto.ECBCrypto
import de.jonasborn.patema.ios.parted.file.PartedFile
import de.jonasborn.patema.util.PaddingUtils
import de.jonasborn.patema.util.XZUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.TimeUnit

//Replaced by EnDecodedPartedFile using AESECBOnLZMA2 as Encoder and Decoder
class PartedCompressedCryptoFile extends PartedFile {

    static Logger logger = LogManager.getLogger(PartedCompressedCryptoFile.class)

    private static LoadingCache<CryptoInfo, Crypto> cryptoCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES).build(new CacheLoader<CryptoInfo, Crypto>() {
        @Override
        Crypto load(CryptoInfo info) throws Exception {
            logger.info("Created new ECPCrypto instance for {}", info.hashCode())
            def c = new ECBCrypto();
            c.initialize(info.password, info.iv, info.salt)
            return c
        }
    })

    File directory
    Crypto crypto

    PartedCompressedCryptoFile(File directory, String password, byte[] iv, byte[] salt) {
        this.directory = directory
        crypto = cryptoCache.get(new CryptoInfo(password, iv, salt))
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
                def i1 = (o1 as File).name.replaceAll("[^0-9]", "")
                def i2 = (o2 as File).name.replaceAll("[^0-9]", "")

                return Integer.parseInt(i1) <=> Integer.parseInt(i2)
            }
        })
        return list
    }


    //Size of the encrypted data
    @Override
    Long getSizeOfContent(File file) {
        return file.lastModified()
    }

    @Override
    Long getSizeWithoutPadding(File file) {
        return file.size()
    }

    @Override
    Long getSizeWithPadding(File file) {
        def r = PaddingUtils.calculate(file.size(), 256)
        return r.total
    }

    @Override
    Long getSizeOnMedia(File file) {
        return file.size()
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
    String getName() {
        return directory.name
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

    public static class CryptoInfo {
        String password
        byte[] iv
        byte[] salt

        CryptoInfo(String password, byte[] iv, byte[] salt) {
            this.password = password
            this.iv = iv
            this.salt = salt
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            CryptoInfo that = (CryptoInfo) o

            if (!Arrays.equals(iv, that.iv)) return false
            if (password != that.password) return false
            if (!Arrays.equals(salt, that.salt)) return false

            return true
        }

        int hashCode() {
            int result
            result = (password != null ? password.hashCode() : 0)
            result = 31 * result + (iv != null ? Arrays.hashCode(iv) : 0)
            result = 31 * result + (salt != null ? Arrays.hashCode(salt) : 0)
            return result
        }
    }

}
