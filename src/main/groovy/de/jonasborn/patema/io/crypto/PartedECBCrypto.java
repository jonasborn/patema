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
import de.jonasborn.patema.util.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PartedECBCrypto implements PartedCrypto {

    SecretKeySpec key;
    byte[] iv;
    byte[] salt;

    Cipher encrypt;
    Cipher decrypt;

    public PartedECBCrypto() {
    }

    private byte[] createIv(int i) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(iv);
        digest.update(salt);
        digest.update((byte) i);
        return digest.digest();
    }


    /*
    11.06.21: I thought about adding a salt multiple times. When ding this, I must calculate and store the salt on a other
    place as this stream wont start at the first block. Probably using the register?
     */
    @Override
    public void initialize(String password, byte[] iv, byte[] salt) throws Exception {
        this.salt = salt;
        this.iv = iv;
        //byte[] salt = Hashing.murmur3_128().hashString(password, Charsets.UTF_8).asBytes();
        //System.out.println(BaseEncoding.base32Hex().encode(salt));
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
        SecretKey k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec);
        key = new SecretKeySpec(k.getEncoded(), "AES");
        //System.out.println(BaseEncoding.base32Hex().encode(key.getEncoded()));
        //PBEKeySpec ivSpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 16 * 8);
        //SecretKey ivKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(ivSpec);
        //iv = new IvParameterSpec(ivKey.getEncoded());
        //System.out.println(BaseEncoding.base32Hex().encode(iv.getIV()));
        final String transform = "AES/ECB/PKCS5Padding";
        encrypt = Cipher.getInstance(transform);
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        decrypt = Cipher.getInstance(transform);
        decrypt.init(Cipher.DECRYPT_MODE, key);
    }

    public byte[] encrypt(int position, byte[] data) throws Exception {
        byte[] iv = createIv(position);
        byte[] open = ByteUtils.xor(iv, data);
        return encrypt.doFinal(open);
    }

    public byte[] decrypt(int position, byte[] encrypted) throws Exception {
        byte[] iv = createIv(position);
        byte[] decrypted = decrypt.doFinal(encrypted);
        return ByteUtils.xor(iv, decrypted);
    }


}
