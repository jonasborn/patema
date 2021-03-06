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

package de.jonasborn.patema.register


import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding

import java.nio.charset.Charset
import java.security.SecureRandom

class V1Register implements Register<V1RegisterEntry> {

    private static SecureRandom random = new SecureRandom()
    
    String id
    private String password;
    private byte[] salt;
    private byte[] iv;
    LinkedList<V1RegisterEntry> entries = []

    V1Register() {
    }

    V1Register(String id, String password) {
        this.id = id
        this.password = password
        salt = new byte[32]
        random.nextBytes(salt)
        iv = new byte[32]
        random.nextBytes(iv)
    }



    @Override
    int getVersion() {
        return 1
    }

    @Override
    void addEntry(V1RegisterEntry entry) {
        entries.add(entry)
    }

    @Override
    void removeEntry(V1RegisterEntry entry) {
        entries.remove(entry)
    }

    @Override
    V1RegisterEntry getEntry(int index) {
        return entries.get(index)
    }




    @Override
    byte[] getIv(String title) {
        return iv;
    }

    @Override
    byte[] getSalt(String title) {
        return salt;
    }

    @Override
    String getPassword(String title) {
        return password;
    }
}
