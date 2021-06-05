package de.jonasborn.patema.info

import groovy.transform.CompileStatic

class Info {

    private Map<String, String> map = [:]

    protected Info(Map<String, String> map) {
        this.map = map
    }

    @CompileStatic
    public String get(String key) {
        return map.get(key.toLowerCase())
    }


}
