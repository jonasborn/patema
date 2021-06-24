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

package de.jonasborn.patema.ios.endecode.implementation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import de.jonasborn.patema.crypto.Crypto
import de.jonasborn.patema.crypto.ECBCrypto
import de.jonasborn.patema.ios.endecode.Decoder
import de.jonasborn.patema.ios.endecode.EncodeDecodeException
import de.jonasborn.patema.ios.endecode.Encoder
import de.jonasborn.patema.util.XZUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.TimeUnit

class AESECBOnLZMA2 implements Encoder, Decoder {


    static Logger logger = LogManager.getLogger(AESECBOnLZMA2.class)

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

    Crypto crypto

    AESECBOnLZMA2(String password, byte[] iv, byte[] salt) {
        crypto = cryptoCache.get(new CryptoInfo(password, iv, salt))
    }

    @Override
    byte[] decode(int index, byte[] data) throws EncodeDecodeException {
        XZUtils.decompress(crypto.decrypt(index, data))
    }

    @Override
    byte[] encode(int index, byte[] data) throws EncodeDecodeException {
        crypto.encrypt(index, XZUtils.compress(data))
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
