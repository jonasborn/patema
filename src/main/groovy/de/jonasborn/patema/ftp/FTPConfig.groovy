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

package de.jonasborn.patema.ftp

class FTPConfig {

    String username
    String password
    boolean encrypt = false
    boolean compress = false
    boolean annoying = false
    int blockSize = 1024 * 1024

    FTPConfig(String username, String password) {
        this.username = username
        this.password = password
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        FTPConfig ftpConfig = (FTPConfig) o

        if (annoying != ftpConfig.annoying) return false
        if (blockSize != ftpConfig.blockSize) return false
        if (compress != ftpConfig.compress) return false
        if (encrypt != ftpConfig.encrypt) return false
        if (password != ftpConfig.password) return false
        if (username != ftpConfig.username) return false

        return true
    }

    int hashCode() {
        int result
        result = (username != null ? username.hashCode() : 0)
        result = 31 * result + (password != null ? password.hashCode() : 0)
        result = 31 * result + (encrypt ? 1 : 0)
        result = 31 * result + (compress ? 1 : 0)
        result = 31 * result + (annoying ? 1 : 0)
        result = 31 * result + blockSize
        return result
    }
}
