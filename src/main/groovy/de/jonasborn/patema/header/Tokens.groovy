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

package de.jonasborn.patema.header

import com.google.common.io.BaseEncoding
import de.jonasborn.patema.util.ByteUtils
import de.jonasborn.patema.util.SecurityUtils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class Tokens {

    private static final int poolLength = 256
    private static final int saltLength = 128

    public static SecureRandom random;
 

    static {
        random = new SecureRandom()
    }

    //VSSSSSSSSSSSSSSSSXXXXXXXXX....
    //|_ Version     |_ Salt  |_ Password

    private static SecretKey createKey(byte[] salt, String password) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 1000, 128);
        SecretKey k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec);
        return new SecretKeySpec(k.getEncoded(), "AES");
    }


    public static Token createToken(String password) {
        byte[] salt = new byte[saltLength]
        random.nextBytes(salt)
        byte[] hash = SecurityUtils.argon2(password)
        byte[] pool = new byte[poolLength]
        random.nextBytes(pool)
        return new Token(password, salt, hash, pool)
    }

    public static byte[] encrypt(Token token) {
        Cipher encrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
        encrypt.init(Cipher.ENCRYPT_MODE, createKey(token.salt, token.password));


        byte[] data = ByteUtils.join(token.iv, token.pool)
        data = ByteUtils.xor(token.salt, data)
        byte[] encrypted = encrypt.doFinal(data)
        byte[] output = new byte[token.salt.length + encrypted.length + 1]
        output[0] = (byte) 1
        System.arraycopy(token.salt, 0, output, 1, token.salt.length)
        System.arraycopy(encrypted, 0, output, 1 + token.salt.length, encrypted.length)
        return output
    }

    public static Token decrypt(byte[] data, String password) {
        byte[] salt = new byte[saltLength];
        System.arraycopy(data, 1, salt, 0, salt.length)
        byte[] encrypted = new byte[data.length - salt.length - 1]
        System.arraycopy(data, 1 + salt.length, encrypted, 0, encrypted.length)

        Cipher decrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
        decrypt.init(Cipher.DECRYPT_MODE, createKey(salt, password));
        byte[] decrypted = decrypt.doFinal(encrypted)
        decrypted = ByteUtils.xor(salt, decrypted)
        byte[] hash = new byte[decrypted.length - poolLength]
        System.arraycopy(decrypted, 0, hash, 0, hash.length)
        byte[] pool = new byte[poolLength]
        System.arraycopy(decrypted, hash.length, pool, 0, poolLength)
        return new Token(password, salt, hash, pool)
    }


    static void main(String[] args) {
        Token token = createToken("Hallo Du Da")
        println token.checkPassword()

        def enc = encrypt(token)
        println BaseEncoding.base16().encode(enc)
        def dec = decrypt(enc, "Hallo Du Da")

        println dec.checkPassword()

    }

}
