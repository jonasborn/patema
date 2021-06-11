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

import de.jonasborn.patema.util.SecurityUtils

class Token {

    protected String password
    byte[] salt
    byte[] iv
    byte[] pool

    Token(String password, byte[] salt, byte[] iv, byte[] pool) {
        this.password = password
        this.salt = salt
        this.iv = iv
        this.pool = pool
    }


    public boolean checkPassword() {
        return SecurityUtils.argon2check(iv, password)
    }


}
