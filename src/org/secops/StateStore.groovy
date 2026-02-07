package org.secops

class StateStore implements Serializable {

    // Default state directory and file
    static String STATE_DIR = ".secops/state"
    static String STATE_FILE = "${STATE_DIR}/assessment.json"

    /**
     * Load assessment state
     * @param steps - pipeline steps object
     * @param assessmentId - optional ID for logging
     * @return Map of state or empty initialized state
     */
    static Map load(steps, String assessmentId = "") {
        if (steps.fileExists(STATE_FILE)) {
            def state = steps.readJSON(file: STATE_FILE)
            steps.echo "[StateStore] Loaded assessment state for ID: ${assessmentId}"
            return state as Map
        } else {
            steps.echo "[StateStore] No state file found for ID: ${assessmentId}, initializing empty state"
            return [version: 1, createdAt: new Date().toString(), stages: [:]]
        }
    }

    /**
     * Save assessment state
     * @param steps - pipeline steps object
     * @param assessmentId - optional ID for logging
     * @param state - Map containing current state
     */
    static void save(steps, String assessmentId = "", Map state) {
        // Ensure state directory exists
        steps.sh "mkdir -p ${STATE_DIR}"

        // Write JSON using pipeline step
        steps.writeJSON(file: STATE_FILE, json: state, pretty: 4)
        steps.echo "[StateStore] Saved assessment state for ID: ${assessmentId}"
    }

    /**
     * Update a specific stage in the state
     * @param steps - pipeline steps object
     * @param stageName - Name of the stage
     * @param data - Map of stage details
     */
    static void updateStage(steps, String stageName, Map data) {
        def state = load(steps)
        state.stages = state.stages ?: [:]
        state.stages[stageName] = data + [updatedAt: new Date().toString()]
        save(steps, "", state)
    }
}
