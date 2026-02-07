import org.secops.StateStore
import org.secops.AuditUtils

def call(Map config = [:]) {

    node {
        // Load previous stage state safely
        def assessmentId = config.assessmentId ?: "default-assessment"
        def state = StateStore.load(this, assessmentId)
        state.stages = state.stages ?: [:]

        // Skip stage if already marked complete
        if(state.stages[config.stageName]?.status == 'PASSED') {
            echo "[INFO] Stage '${config.stageName}' already completed, skipping..."
            return
        }

        stage(config.stageName) {
            try {
                // --- Approval Gate ---
                approvalGate(
                    gateName: config.stageName + "_APPROVAL",
                    requiredRole: config.requiredRole
                )

                // --- Stage Execution ---
                echo "[INFO] Running stage '${config.stageName}' for target ${config.target}"

                // Placeholder: run your actual scan or assessment here
                def findings = [:] // Collect findings here

                // --- Findings Gate ---
                findingsGate(
                    gateName: config.stageName + "_FINDINGS",
                    findings: findings
                )

                // --- Save state safely ---
                state.stages[config.stageName] = [
                    status: 'PASSED',
                    findings: findings,
                    completedAt: new Date().toString()
                ]
                StateStore.save(this, assessmentId, state)

                // --- Optional: push to DefectDojo ---
                echo "[INFO] Sending findings to DefectDojo (placeholder)"

            } catch (err) {
                // Mark stage as failed in state for retry/resume
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
