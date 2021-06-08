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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PartedECBCrypto implements PartedCrypto{

    SecretKeySpec key;
    IvParameterSpec iv;

    Cipher encrypt;
    Cipher decrypt;

    public PartedECBCrypto() {
    }

    private byte[] createIv(int i) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(iv.getIV());
        digest.update((byte) i);
        return digest.digest();
    }

    public byte[] xor(byte[] iv, byte[] data) {
        int position = 0;
        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            if (position >= iv.length) position = 0;
            output[i] = (byte) (iv[position] ^ data[i]);
            position++;
        }
        return output;
    }

    @Override
    public void setPassword(String password) throws Exception {
        byte[] salt = Hashing.murmur3_128().hashString(password, Charsets.UTF_8).asBytes();
        //System.out.println(BaseEncoding.base32Hex().encode(salt));
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
        SecretKey k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec);
        key = new SecretKeySpec(k.getEncoded(), "AES");
        //System.out.println(BaseEncoding.base32Hex().encode(key.getEncoded()));
        PBEKeySpec ivSpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 16 * 8);
        SecretKey ivKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(ivSpec);
        iv = new IvParameterSpec(ivKey.getEncoded());
        //System.out.println(BaseEncoding.base32Hex().encode(iv.getIV()));
        final String transform = "AES/ECB/PKCS5Padding";
        encrypt = Cipher.getInstance(transform);
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        decrypt = Cipher.getInstance(transform);
        decrypt.init(Cipher.DECRYPT_MODE, key);
    }

    public byte[] encrypt(int position, byte[] data) throws Exception {
        byte[] iv = createIv(position);
        byte[] open = xor(iv, data);
        return encrypt.doFinal(open);
    }

    public byte[] decrypt(int position, byte[] encrypted) throws Exception{
        byte[] iv = createIv(position);
        byte[] decrypted = decrypt.doFinal(encrypted);
        return xor(iv, decrypted);
    }



}
