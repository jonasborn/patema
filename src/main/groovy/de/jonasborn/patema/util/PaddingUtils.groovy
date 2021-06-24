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

class PaddingUtils {

    public static PaddingResult calculate(long number, long base) {
        long full = ((long) (number / base)) * base
        if (full == number) return new PaddingResult(full, 0, full)
        long last = number - full; //1400-1280 = 120
        long padding = base - last;
        return new PaddingResult(
                full,
                padding,
                number + (padding)
        );
    }

    public static class PaddingResult {
        private final long full;
        private final long padding;
        private final long total;

        public PaddingResult(long full, long padding, long total) {
            this.full = full;
            this.padding = padding;
            this.total = total;
        }

        public long getBase() {
            return full;
        }

        public long getPadding() {
            return padding;
        }

        public long getTotal() {
            return total;
        }
    }


}
