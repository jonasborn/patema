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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import de.jonasborn.patema.ftp.project.FTPProject
import de.jonasborn.patema.ftp.project.FTPProjectFile
import de.jonasborn.patema.ftp.tape.FTPTape
import de.jonasborn.patema.ftp.tape.FTPTapeFile

import java.util.concurrent.TimeUnit

class FTPFileFactory {

    LoadingCache<String, FTPProject> projectCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, FTPProject>() {
                @Override
                FTPProject load(String key) throws Exception {
                    return new FTPProject(root(), key)
                }
            })

    LoadingCache<String, FTPTape> tapeCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, FTPTape>() {
                @Override
                FTPTape load(String key) throws Exception {
                    return new FTPTape(root(), key)
                }
            })

    FTPConfig config;
    File delegate

    FTPFileFactory(FTPConfig config, File delegate) {
        this.delegate = delegate
        this.config = config
    }

    public FTPRoot root() {
        return new FTPRoot(config, delegate)
    }

    public FTPProject project(String title) {
        return projectCache.get(title)
    }

    public FTPTape tape(String path) {
        return tapeCache.get(path)
    }

    public FTPProjectFile projectFile(FTPProject project, String title) {
        return new FTPProjectFile(project, title)
    }

    public FTPTapeFile tapeFile(FTPTape tape, String title) {
        return new FTPTapeFile(tape, title)
    }

    public FTPElement find(String path) throws IOException {
        def parts = path.split("/")
        if (path == "/" || parts.length == 1) {
            return root()
        }

        if (parts.length == 2) {
            if (parts[1].startsWith("project")) return project(parts[1])
            if (parts[1].startsWith("tape")) return tape(parts[1])
            return project("project-" + parts[1])
        }

        if (parts.length == 3) {
            if (parts[1].startsWith("project")) {
                return projectFile(project(parts[1]), parts[2])
            }
            if (parts[1].startsWith("tape")) {
                return tapeFile(tape(parts[1]), parts[2])
            }
        }

        println "Not found: " + path
        throw new IOException("Not found")

    }

}
