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

package de.jonasborn.patema.config

import com.google.common.io.BaseEncoding
import de.jonasborn.patema.util.SecurityUtils

class UsersConfig extends ArrayList<UserConfig> {

    public boolean auth(String username, String password) {
        try {
            def user = this.find { it.username = username }
            if (user == null) return false
            return SecurityUtils.argon2check(BaseEncoding.base64().decode(user.password), password)
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }


}
