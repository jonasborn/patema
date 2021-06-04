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

import de.jonasborn.patema.io.chunked.UnPackedFileConfig
import de.jonasborn.patema.io.chunked.ChunkedIO
import org.apache.commons.crypto.cipher.CryptoCipher
import org.apache.commons.crypto.cipher.CryptoCipherFactory
import org.apache.commons.crypto.utils.Utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

class PartedIO {

    final SecretKeySpec key
    final IvParameterSpec iv
    final CryptoCipher encrypt;
    final CryptoCipher decrypt;

    public PartedIO(String password) {
        key = new SecretKeySpec(getUTF8Bytes(password),"AES");
        iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));

        final Properties properties = new Properties();
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY, CryptoCipherFactory.CipherProvider.OPENSSL.getClassName());
        final String transform = "AES/CBC/PKCS5Padding";
        encrypt = Utils.getCipherInstance(transform, properties);
        encrypt.init(Cipher.ENCRYPT_MODE, key, iv);
        decrypt = Utils.getCipherInstance(transform, properties);
        decrypt.init(Cipher.DECRYPT_MODE, key, iv);
    }

    public byte[] encrypt(byte[] input) {
        byte[] output = new byte[input.length + iv.IV.length]
        def length = encrypt.doFinal(input, 0, input.length, output, 0);
        byte[] real = new byte[length]
        System.arraycopy(output, 0, real, 0, length)
        return real
    }

    public byte[] decrypt(byte[] input) {
        byte[] output = new byte[input.length + key.encoded.length]
        def length = decrypt.doFinal(input, 0, input.length, output, 0)
        byte[] real = new byte[length]
        System.arraycopy(output, 0, real, 0, length)
        return real
    }




    private static getUTF8Bytes(final String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        def data = new File("C:\\Users\\Jonas Born\\7z1900-x64.exe").bytes
        Long start = System.currentTimeMillis()
        def pio = new PartedIO("HalloWelt")
        pio.decrypt(pio.encrypt(data))
        println System.currentTimeMillis() - start
        start = System.currentTimeMillis()
        def c = new UnPackedFileConfig("HalloWelt")
        c.compress = false
        ChunkedIO io = new ChunkedIO(c)
        io.decode(0, io.encode(0, data))
        println System.currentTimeMillis() - start
    }
}
