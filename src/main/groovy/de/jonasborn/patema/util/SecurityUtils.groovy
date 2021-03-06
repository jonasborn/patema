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

package de.jonasborn.patema.util

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import sun.security.ssl.SecureKey

import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SecurityUtils {

    public static byte[] argon2(String password) {
        def a2 = new Argon2PasswordEncoder();
        return a2.encode(password).getBytes("UTF-8")
    }

    public static boolean argon2check(byte[] encoded, String password) {
        def a2 = new Argon2PasswordEncoder()
        return a2.matches(password, new String(encoded, "UTF-8"))
    }

}
