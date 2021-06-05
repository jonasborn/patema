package de.jonasborn.patema.info

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.jonasborn.patema.config.Config
import de.jonasborn.patema.util.RuntimeUtils

class Sys {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create()


    private static Info tapeinfo(String device) {
        def command = [Config.current.binaryConfig._prefix, Config.current.binaryConfig.tapeinfo, "-f", device]
        def l = RuntimeUtils.execute(command as String[])
        Map<String, String> m = l.collect { it.split(":\\s") }.findAll { it.size() == 2 }.collectEntries {
            [it[0].toLowerCase(), it[1].trim().replace("'", "").replaceAll("\\s", "")]
        }
        return new Info(m)
    }

    public static Info sg_log(String device, String sub) {
        def command = [Config.current.binaryConfig._prefix, Config.current.binaryConfig.sg_logs, "-b", "-p", sub, device]
        println command.join(" ")
        def l = RuntimeUtils.execute(command as String[])
        println l
        Map<String, String> m = l.findAll { it.contains(":") }
                .collect { it.split(":\\s") }
                .findAll { it.size() == 2 }
                .collectEntries {
                    [it[0].trim().toLowerCase(), it[1].trim().replace("'", "").replaceAll("\\s", "")]
                }
        return new Info(m)
    }

    /*
    This must be one of the uncleanest solutions I've ever written. I hope I can find a other and better solution than this.
     */

    public static List<Info> lsscsi() {
        def command = [Config.current.binaryConfig._prefix, Config.current.binaryConfig.lsscsi, "-c"]
        def l = RuntimeUtils.execute(command as String[])

        command = [Config.current.binaryConfig._prefix, Config.current.binaryConfig.lsscsi]
        def l2 = RuntimeUtils.execute(command as String[])


        Map<Integer, List<List<String>>> entries = [:]
        Integer position = 0;
        for (i in 0..<l.size()) {
            def row = l.get(i)
            if (row.startsWith("Host")) position++
            if (row.startsWith("  ")) {
                def outer = entries.get(position)
                if (outer == null) {
                    outer = new ArrayList<List<String>>()
                    entries.put(position, outer)
                }
                outer.add([
                        row.substring(0, 19).trim(),
                        row.substring(19, 43).trim(),
                        row.substring(43).trim()
                ])
            }
        }


        List<Map<String, String>> parsedPairs = entries.collect {
            List<String> pairs = []
            it.value.each {
                def (a, b, c) = [it[0], it[1], it[2]]
                def (ac, bc, cc) = [a.contains(":"), b.contains(":"), c.contains(":")]
                if (ac && bc && cc) pairs.addAll([a, b, c])
                if (ac && !bc && cc) pairs.addAll([a + b, c])
                if (ac && !bc && !cc) pairs.add(a + b + c)
                if (ac && bc && !cc) pairs.addAll([a, b + c])
            }
            pairs.collectEntries {
                def p = it.split(":\\s+")
                return [p[0].toLowerCase(), p[1]]
            } as Map<String, String>
        }

        parsedPairs.each { parsedDetails ->
            def model = parsedDetails.get("model")
            if (model == null) return
            def path = l2.find { it.contains(model) }.find("\\/dev\\/(\\w)+")
            if (path != null) parsedDetails.put("path", path)
        }

        return parsedPairs.collect { new Info(it) }


    }


    static void main(String[] args) {
        def t = [:]
        //t.putAll(tapeinfo("/dev/nst0"))
        println gson.toJson(lsscsi())
    }


}
