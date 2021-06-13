package de.jonasborn.patema.config

import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.jonasborn.patema.util.SecurityUtils

class Config {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create()

    public static Config current = new Config()

    public static void loadConfig(String path) {
        def file = new File(path)
        if (!file.exists()) {
            println "Config not found, creating new one"
            writeConfig(path)
            System.exit(1)
        }
        current = gson.fromJson(file.text, Config.class)

        boolean changed = false

        //Replace raw passwords with hashed ones
        current.users.each {
            if (!BaseEncoding.base64().canDecode(it.password)) {
                it.password = BaseEncoding.base64().encode(SecurityUtils.argon2(it.password))
                changed = true
            }
        }

        if (changed) writeConfig(path)

    }

    public static void writeConfig(String path) {
        def file = new File(path)
        file.write(gson.toJson(current))
    }

    UsersConfig users = new UsersConfig()
    BinaryConfig binaryConfig = new BinaryConfig()

}
