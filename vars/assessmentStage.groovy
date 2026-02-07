import org.secops.StateStore

/**
 * Pipeline stage wrapper for assessment
 * @param config Map of stage config:
 *  - stageName : String, name of the stage
 *  - assessmentId : String, optional ID for state tracking
 *  - requiredRole : String, role required for approval
 *  - target : String, target system or resource
 */
def call(Map config = [:]) {

    node {
        // Load existing state or initialize
        def state = StateStore.load(this, config.assessmentId)

        // Skip stage if already passed
        if (state.stages?.get(config.stageName)?.status == 'PASSED') {
            echo "[INFO] Stage '${config.stageName}' already completed, skipping..."
            return
        }

        stage(config.stageName) {
            try {
                // --- Approval Gate ---
                approvalGate(
                    gateName: "${config.stageName}_APPROVAL",
                    requiredRole: config.requiredRole
                )

                // --- Stage Execution ---
                echo "[INFO] Running stage '${config.stageName}' for target ${config.target}"

                // Placeholder: run your actual scan or assessment here
                def findings = [:] // collect findings dynamically

                // --- Findings Gate ---
                findingsGate(
                    gateName: "${config.stageName}_FINDINGS",
                    findings: findings
                )

                // --- Update state ---
                state.stages = state.stages ?: [:]
                state.stages[config.stageName] = [
                    status: 'PASSED',
                    findings: findings,
                    completedAt: new Date().toString()
                ]
                StateStore.save(this, config.assessmentId, state)

                // Optional: send findings to DefectDojo
                echo "[INFO] Sending findings to DefectDojo (placeholder)"
            }
            catch (err) {
                // Mark stage as FAILED in state
                state.stages = state.stages ?: [:]
                state.stages[config.stageName] = [
                    status: 'FAILED',
                    error: err.toString(),
                    completedAt: new Date().toString()
                ]
                StateStore.save(this, config.assessmentId, state)

                error "[ERROR] Stage '${config.stageName}' failed: ${err}"
            }
        }
    }
}
