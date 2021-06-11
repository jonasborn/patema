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

package de.jonasborn.patema.io.crypto;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.apache.commons.crypto.cipher.CryptoCipher;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/*
    So, I'm still not a crypto expert. Bit as it seems, using a padding-less
    solution with big blocks and a not predictable iv per block may work without any
    concerns.
 */

class PartedCTRCrypto implements PartedCrypto {

    SecretKeySpec key;
    byte[] salt;
    byte[] iv;

    PartedCTRCryptoCache encryptionCache;
    PartedCTRCryptoCache decryptionCache;


    PartedCTRCrypto() throws Exception {

    }

    @Override
    public void initialize(String password, byte[] iv,  byte[] salt) throws Exception{
        this.iv = iv;
        this.salt = salt;
        //byte[] salt = Hashing.murmur3_128().hashString(password, Charsets.UTF_8).asBytes();
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
        SecretKey k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec);
        key = new SecretKeySpec(k.getEncoded(), "AES");
        //PBEKeySpec ivSpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 16);
        //SecretKey ivKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(ivSpec);
        //iv = new IvParameterSpec(ivKey.getEncoded());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        this.encryptionCache = new PartedCTRCryptoCache(Cipher.ENCRYPT_MODE, key, ivSpec);
        this.decryptionCache = new PartedCTRCryptoCache(Cipher.DECRYPT_MODE, key, ivSpec);
    }

    public byte[] encrypt(int index, byte[] data) throws Exception {
        CryptoCipher cipher = encryptionCache.get(index);
        byte[] output = new byte[data.length];
        cipher.doFinal(data, 0, data.length, output, 0);
        return output;
    }

    public byte[] decrypt(int index, byte[] data) throws Exception {
        CryptoCipher cipher = decryptionCache.get(index);
        byte[] output = new byte[data.length];
        cipher.doFinal(data, 0, data.length, output, 0);
        return output;
    }


}
