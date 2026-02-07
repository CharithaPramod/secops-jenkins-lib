import org.secops.AuditUtils

def call(Map config = [:]) {

    // ---- Validation (non-negotiable) ----
    if (!config.gateName || !config.requiredRole) {
        error("approvalGate requires gateName and requiredRole")
    }

    // ---- Audit: gate opened ----
    AuditUtils.log(this, [
        event: 'GATE_OPENED',
        gateName: config.gateName,
        requiredRole: config.requiredRole
    ])

    timeout(time: 30, unit: 'MINUTES') {

        def decision = input(
            id: "${config.gateName}_APPROVAL",
            message: "Approval required: ${config.gateName}",
            ok: "Submit",
            submitter: config.requiredRole,
            parameters: [
                choice(
                    name: 'DECISION',
                    choices: ['APPROVE', 'REJECT'],
                    description: 'Approval decision'
                ),
                text(
                    name: 'COMMENTS',
                    description: 'Mandatory comments'
                )
            ]
        )

        // ---- Audit decision ----
        AuditUtils.log(this, [
            event: 'GATE_DECISION',
            gateName: config.gateName,
            decision: decision.DECISION,
            comments: decision.COMMENTS
        ])

        if (decision.DECISION == 'REJECT') {
            error("Gate ${config.gateName} rejected")
        }
    }
}
