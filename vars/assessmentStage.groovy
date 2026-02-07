import org.secops.StateStore

def call(Map config = [:]) {

    node {
        def assessmentId = config.assessmentId ?: "default-assessment"
        def state = StateStore.load(this, assessmentId)
        state.stages = state.stages ?: [:]

        // Skip if already passed
        if(state.stages[config.stageName]?.status == 'PASSED') {
            echo "[INFO] Stage '${config.stageName}' already completed."
            return
        }

        stage(config.stageName) {
            try {
                // --- Approval Gate ---
                input message: "Approve ${config.stageName}?", ok: "Approve"

                echo "[INFO] Running stage '${config.stageName}'"

                // --- Placeholder for actual scan ---
                def findings = [:] // make sure this is a plain Map

                // --- Save state safely ---
                state.stages[config.stageName] = [
                    status: 'PASSED',
                    findings: findings,
                    completedAt: new Date().toString()
                ]
                StateStore.save(this, assessmentId, state)

            } catch (err) {
                // Mark failed safely
                state.stages[config.stageName] = [
                    status: 'FAILED',
                    error: err.toString(),
                    completedAt: new Date().toString()
                ]
                StateStore.save(this, assessmentId, state)

                error "[ERROR] Stage '${config.stageName}' failed: ${err}"
            }
        }
    }
}
