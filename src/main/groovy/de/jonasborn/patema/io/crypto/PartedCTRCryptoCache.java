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

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class PartedCTRCryptoCache {


    final static Properties properties;

    static {
        properties = new Properties();
        //properties.setProperty(CryptoCipherFactory.CLASSES_KEY, CryptoCipherFactory.CipherProvider.OPENSSL.getClassName());
    }

    Map<Integer, List<CryptoCipher>> cache = new HashMap<>();
    int position = 0;
    int mode;
    SecretKeySpec key;
    IvParameterSpec iv;
    final static String transform = "AES/CTR/NoPadding";
    boolean run = true;

    PartedCTRCryptoCache(int mode, SecretKeySpec key, IvParameterSpec iv) throws Exception {
        this.mode = mode;
        this.key = key;
        this.iv = iv;
        prepare();

        new Thread(() -> {
            while (run) {
                try {
                    prepare();
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

    private IvParameterSpec createIv(int i) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(iv.getIV());
        digest.update((byte) i);
        return new IvParameterSpec(digest.digest());
    }

    private CryptoCipher create(int i) throws Exception {
        System.out.println("CREATING " + i);
        IvParameterSpec iv = createIv(i);
        CryptoCipher instance = Utils.getCipherInstance(transform, properties);
        instance.init(mode, key, iv);
        return instance;
    }

    public CryptoCipher get(int position) throws Exception {
        if (position > this.position) this.position = position;
        List<CryptoCipher> list = cache.get(position);
        if (list == null) return create(position);
        if (list.size() == 0) return create(position);
        return list.remove(0);
    }

    public void prepare() throws Exception {
        for (int i = 0; i < position + 3; i++) {
            List<CryptoCipher> list = cache.get(i);
            if (list == null) list = new ArrayList<>(3);
            while (list.size() < 3) list.add(create(i));
        }
    }


}

