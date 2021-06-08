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

package de.jonasborn.patema.tape

public class TapeStatus {
    boolean eof = false;
    boolean bot = false;
    boolean eot = false;
    boolean sm = false;
    boolean eod = false;
    boolean wr_prot = false;
    boolean online = false;
    boolean d_6250 = false;
    boolean d_1600 = false;
    boolean d_800 = false;
    boolean dr_open = false;
    boolean in_rep_en = false;
    boolean cln = false;


    @Override
    public String toString() {
        return "TapeStatus{" +
                "eof=" + eof +
                ", bot=" + bot +
                ", eot=" + eot +
                ", sm=" + sm +
                ", eod=" + eod +
                ", wr_prot=" + wr_prot +
                ", online=" + online +
                ", d_6250=" + d_6250 +
                ", d_1600=" + d_1600 +
                ", d_800=" + d_800 +
                ", dr_open=" + dr_open +
                ", in_rep_en=" + in_rep_en +
                ", cln=" + cln +
                '}';
    }
}