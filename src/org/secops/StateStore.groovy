package org.secops

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class StateStore implements Serializable {

    // Always return a serializable Map
    static Map load(steps, String assessmentId) {
        def filePath = ".secops/state/assessment.json"

        if (steps.fileExists(filePath)) {
            // Use readFile + JsonSlurper
            def jsonText = steps.readFile(filePath)
            def jsonData = new JsonSlurper().parseText(jsonText)

            // Convert recursively to plain Map/List
            def safeData = toPlainMap(jsonData)

            steps.echo "[StateStore] Loaded assessment state for ID: ${assessmentId}"
            return safeData
        } else {
            steps.echo "[StateStore] No assessment state file found for ID: ${assessmentId}"
            return [:]
        }
    }

    // Save Map as JSON
    static void save(steps, String assessmentId, Map data) {
        def filePath = ".secops/state/assessment.json"
        steps.sh "mkdir -p .secops/state"

        def plainData = toPlainMap(data)
        def jsonText = JsonOutput.prettyPrint(JsonOutput.toJson(plainData))

        steps.writeFile(file: filePath, text: jsonText)
        steps.echo "[StateStore] Saved assessment state for ID: ${assessmentId}"
    }

    // Convert LazyMap/LazyList to serializable plain Map/List
    private static def toPlainMap(obj) {
        if (obj instanceof Map) {
            def result = [:]
            obj.each { k, v -> result[k.toString()] = toPlainMap(v) }
            return result
        } else if (obj instanceof List) {
            return obj.collect { toPlainMap(it) }
        } else {
            return obj
        }
    }
}
