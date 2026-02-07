// StateStore.groovy
package org.secops

class StateStore implements Serializable {

    // Pass 'steps' from pipeline so fileExists/readJSON work
    static def load(steps, String assessmentId) {
        def filePath = ".secops/state/assessment.json"

        // Use pipeline step through 'steps'
        if (steps.fileExists(filePath)) {
            // read JSON using pipeline step
            def jsonData = steps.readJSON(file: filePath)
            steps.echo "Assessment state loaded for ID: ${assessmentId}"
            return jsonData
        } else {
            steps.echo "No assessment state file found for ID: ${assessmentId}"
            return null
        }
    }

    static def save(steps, String assessmentId, Map data) {
        def filePath = ".secops/state/assessment.json"

        // Ensure directory exists
        steps.sh "mkdir -p .secops/state"

        // Write JSON using pipeline step
        steps.writeJSON(file: filePath, json: data, pretty: 4)
        steps.echo "Assessment state saved for ID: ${assessmentId}"
    }
}
