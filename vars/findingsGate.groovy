import org.secops.StateStore
import org.secops.AuditUtils

def call(Map config = [:]) {

    def script = this

    def stageName   = config.stage ?: error("stage is required")
    def maxRetries  = config.maxRetries ?: 2
    def policy      = config.policy ?: [:]

    def state = StateStore.load(script)
    def stage = state.stages[stageName] ?: [:]

    int retryCount = stage.retryCount ?: 0

    // --- POLICY EVALUATION ---
    boolean hasCritical = (stage.critical ?: 0) > 0
    boolean requiresApproval = (stage.high ?: 0) > (policy.maxHigh ?: 5)

    if (hasCritical && retryCount < maxRetries) {
        retryCount++

        AuditUtils.log(script, [
            event: 'GATE_RETRY',
            stage: stageName,
            retry: retryCount
        ])

        StateStore.updateStage(script, stageName, [
            decision: 'RETRY',
            retryCount: retryCount
        ])

        error("${stageName} retry ${retryCount}/${maxRetries}")

    }

    if (hasCritical && retryCount >= maxRetries) {

        AuditUtils.log(script, [
            event: 'GATE_FAIL',
            stage: stageName
        ])

        StateStore.updateStage(script, stageName, [
            decision: 'FAIL'
        ])

        error("${stageName} failed after ${retryCount} retries")
    }

    if (requiresApproval) {

        AuditUtils.log(script, [
            event: 'GATE_APPROVAL_REQUIRED',
            stage: stageName
        ])

        timeout(time: 30, unit: 'MINUTES') {
            input(
                message: "Approve findings for ${stageName}",
                ok: "Approve",
                submitter: config.approver ?: 'DevOps Engineer'
            )
        }

        StateStore.updateStage(script, stageName, [
            decision: 'APPROVED'
        ])
    }

    AuditUtils.log(script, [
        event: 'GATE_PASS',
        stage: stageName
    ])

}
`