import org.secops.StateStore
import org.secops.AuditUtils

def call(Map config = [:]) {

    def script = this

    String stageName   = config.stage ?: error("stage is required")
    int maxRetries     = config.maxRetries ?: 2
    Map policy         = config.policy ?: [:]
    String approver    = config.approver ?: 'DevOps Engineer'

    def state = StateStore.load(script)
    def stage = state.stages[stageName] ?: [:]

    String existingDecision = stage.decision

    // --------------------------------------------------
    // 1. RESUME / REPLAY SHORT-CIRCUIT
    // --------------------------------------------------
    if (existingDecision in ['APPROVED', 'PASS']) {
        AuditUtils.log(script, [
            event: 'GATE_RESUMED',
            stage: stageName,
            decision: existingDecision
        ])
        return
    }

    if (existingDecision == 'FAIL') {
        error("${stageName} previously failed and is locked")
    }

    int critical = stage.critical ?: 0
    int high     = stage.high ?: 0
    int retries  = stage.retryCount ?: 0

    boolean hasCritical = critical > 0
    boolean approvalRequired = high > (policy.maxHigh ?: 5)

    // --------------------------------------------------
    // 2. RETRY LOGIC
    // --------------------------------------------------
    if (hasCritical && retries < maxRetries) {

        retries++

        AuditUtils.log(script, [
            event: 'GATE_RETRY',
            stage: stageName,
            retry: retries
        ])

        StateStore.updateStage(script, stageName, [
            decision: 'RETRY',
            retryCount: retries
        ])

        error("${stageName} retry ${retries}/${maxRetries}")
    }

    // --------------------------------------------------
    // 3. HARD FAIL
    // --------------------------------------------------
    if (hasCritical && retries >= maxRetries) {

        AuditUtils.log(script, [
            event: 'GATE_FAIL',
            stage: stageName
        ])

        StateStore.updateStage(script, stageName, [
            decision: 'FAIL'
        ])

        error("${stageName} failed after ${retries} retries")
    }

    // --------------------------------------------------
    // 4. HUMAN APPROVAL (ONCE ONLY)
    // --------------------------------------------------
    if (approvalRequired) {

        AuditUtils.log(script, [
            event: 'GATE_APPROVAL_REQUIRED',
            stage: stageName
        ])

        timeout(time: 30, unit: 'MINUTES') {
            def approverUser = input(
                message: "Approve findings for ${stageName}",
                ok: "Approve",
                submitter: approver
            )

            StateStore.updateStage(script, stageName, [
                decision: 'APPROVED',
                decisionBy: approverUser,
                decisionAt: new Date().toString()
            ])
        }

        return
    }

    // --------------------------------------------------
    // 5. AUTO PASS
    // --------------------------------------------------
    AuditUtils.log(script, [
        event: 'GATE_PASS',
        stage: stageName
    ])

    StateStore.updateStage(script, stageName, [
        decision: 'PASS'
    ])
}
