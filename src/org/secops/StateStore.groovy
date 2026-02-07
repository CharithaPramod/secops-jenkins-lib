package org.secops

import groovy.json.*

class StateStore {

    static String stateFile(def script) {
        return "${script.env.WORKSPACE}/assessment-state.json"
    }

    static Map load(def script) {
        def file = new File(stateFile(script))
        if (!file.exists()) {
            return [:]
        }
        return new JsonSlurper().parseText(file.text)
    }

    static void save(def script, Map state) {
        def json = JsonOutput.prettyPrint(JsonOutput.toJson(state))
        new File(stateFile(script)).write(json)
    }
}
