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

package de.jonasborn.patema.template

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader

class Templates {
    static PebbleEngine engine
    static {
        engine = new PebbleEngine.Builder().loader(
                new FileLoader()
        ).templateCache(null).tagCache(null).cacheActive(false).build()
    }

    public static String render(String name, Map data) {
        Writer writer = new StringWriter()
        def t = engine.getTemplate(name)
        t.evaluate(writer)
        return writer.toString()
    }

}
