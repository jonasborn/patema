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

import com.guichaguri.minimalftp.FTPConnection
import com.guichaguri.minimalftp.api.CmdResponse
import com.guichaguri.minimalftp.api.Command
import com.guichaguri.minimalftp.api.CommandInfo
import com.guichaguri.minimalftp.api.IFileSystem
import com.guichaguri.minimalftp.api.IUserAuthenticator

class FTPAuth implements IUserAuthenticator{

    File directory

    FTPAuth(File directory) {
        this.directory = directory
    }

    @Override
    boolean needsUsername(FTPConnection con) {
        return true
    }

    @Override
    boolean needsPassword(FTPConnection con, String username, InetAddress host) {
        return true
    }

    @Override
    IFileSystem authenticate(FTPConnection con, InetAddress host, String username, String password) throws IUserAuthenticator.AuthException {
        FTPConfig config = new FTPConfig(username, password)
        con.registerCmd("ENCRYPT", "encryption <on/off>", {
            if (it.args == "on") config.encrypt = true
            if (it.args == "off") config.encrypt = false
            return new CmdResponse(200,  "Encryption is " + ((config.encrypt) ? "on" : "off"))
        })
        con.registerCmd("COMPRESS", "COMPRESS <on/off>", {
            if (it.args == "on") config.compress = true
            if (it.args == "off") config.compress = false
            return new CmdResponse(200,  "Compression is " + ((config.compress) ? "on" : "off"))
        })
        con.registerCmd("BLOCKSIZE", "BLOCKSIZE <(int)>", {
            if (it.args == "") return new CmdResponse(200, "Block size is " + config.blockSize)
            try {
                config.blockSize = Integer.parseInt(it.args)
                return new CmdResponse(200, "Block size is " + config.blockSize)
            } catch (Exception e) {
                e.printStackTrace()
                return new CmdResponse(500, "Not a valid value")
            }
        })

        return new FTPFileSystem(config, directory)
    }
}
