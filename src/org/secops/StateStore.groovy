package org.secops

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class StateStore implements Serializable {

    // Load state from JSON file (convert LazyMap to plain Map)
    static def load(steps, String assessmentId) {
        def filePath = ".secops/state/assessment.json"

        if (steps.fileExists(filePath)) {
            def jsonText = steps.readFile(filePath)
            def jsonData = new JsonSlurper().parseText(jsonText)
            steps.echo "[StateStore] Loaded assessment state for ID: ${assessmentId}"

            return deepCopyMap(jsonData) // Convert to serializable Map
        } else {
            steps.echo "[StateStore] No assessment state file found for ID: ${assessmentId}"
            return [:] // Return empty Map
        }
    }

    // Save state to JSON file
    static def save(steps, String assessmentId, Map data) {
        def filePath = ".secops/state/assessment.json"
        steps.sh "mkdir -p .secops/state"

        def plainData = deepCopyMap(data)
        def jsonText = JsonOutput.prettyPrint(JsonOutput.toJson(plainData))

        steps.writeFile(file: filePath, text: jsonText)
        steps.echo "[StateStore] Saved assessment state for ID: ${assessmentId}"
    }

    // Recursively convert LazyMap/LazyList to plain Map/List
    private static def deepCopyMap(obj) {
        if (obj instanceof Map) {
            def result = [:]
            obj.each { k, v -> result[k] = deepCopyMap(v) }
            return result
        } else if (obj instanceof List) {
            return obj.collect { deepCopyMap(it) }
        } else {
            return obj
        }
    }
}
