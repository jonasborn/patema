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

package de.jonasborn.patema

import com.guichaguri.minimalftp.FTPServer
import de.jonasborn.patema.ftp.FTPAuth

class Main {

    public static void main(String[] args) {

        File root = new File("root");
        def auth = new FTPAuth(root);
        FTPServer server = new FTPServer(auth);
        server.listenSync(21);

    }
}
