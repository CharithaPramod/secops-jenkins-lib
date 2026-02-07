package org.secops

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class StateStore implements Serializable {
    static String STATE_DIR = ".secops/state"

    static Map load(script, String assessmentId) {
        script.sh "mkdir -p ${STATE_DIR}"
        def filePath = "${STATE_DIR}/${assessmentId}.json"
        if (script.fileExists(filePath)) {
            def content = script.readFile(filePath)
            return new JsonSlurper().parseText(content) as Map
        }
        return [:]
    }

    static void save(script, String assessmentId, Map state) {
        script.sh "mkdir -p ${STATE_DIR}"
        def filePath = "${STATE_DIR}/${assessmentId}.json"
        def content = JsonOutput.prettyPrint(JsonOutput.toJson(state))
        script.writeFile file: filePath, text: content
        script.echo "[StateStore] Saved assessment state for ID: ${assessmentId}"
    }
}
