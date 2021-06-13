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

package de.jonasborn.patema.ftp.tape

import java.text.DecimalFormat

class FTPTapeOverview {

    private Long total = null
    private Long position = 0
    private String message = "0% pending"
    boolean finished = true

    public void initialize(long total) {
        this.total = total
        finished = false;
    }


    public void step(String message) {
        position++;
        build(message)
    }

    public void update(long position, String message) {
        this.position = position
        build(message)
    }

    private void build(String message) {
        if (total == null ) message = "0% pending"
        def percent = (position / total) * 100
        this.message = new DecimalFormat("#.##").format(percent) + "% " + message
    }

    public String getMessage() {
        return this.message
    }

    public void fail(String message) {
        this.message = "E% " + message
    }

    public void finish() {
        finished = true
    }

}
