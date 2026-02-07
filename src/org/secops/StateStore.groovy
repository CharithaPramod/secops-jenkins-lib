package org.secops

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class StateStore implements Serializable {

    static String STATE_DIR = ".secops/state"
    static String STATE_FILE = "${STATE_DIR}/assessment.json"

    static void init(def script) {
        script.sh "mkdir -p ${STATE_DIR}"
        if (!script.fileExists(STATE_FILE)) {
            save(script, [
                version: 1,
                createdAt: new Date().toString(),
                stages: [:]
            ])
        }
    }

    static Map load(def script) {
        if (!script.fileExists(STATE_FILE)) {
            return [:]
        }
        return new JsonSlurper().parseText(
            script.readFile(STATE_FILE)
        ) as Map
    }

    static void save(def script, Map state) {
        script.sh "mkdir -p ${STATE_DIR}"
        script.writeFile(
            file: STATE_FILE,
            text: JsonOutput.prettyPrint(JsonOutput.toJson(state))
        )
    }

    static void updateStage(def script, String stage, Map data) {
        def state = load(script)
        state.stages[stage] = data + [
            updatedAt: new Date().toString()
        ]
        save(script, state)
    }
}
