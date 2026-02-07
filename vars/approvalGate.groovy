import org.secops.AuditUtils
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def call(Map config = [:]) {

    // --- Configurable defaults ---
    def gateName = config.gateName ?: 'UNKNOWN_GATE'
    def requiredRole = config.requiredRole ?: 'Security Auditor'
    def escalationRole = config.escalationRole ?: null
    def slaMinutes = config.slaMinutes ?: 30
    def autoDecision = config.autoDecision ?: null // APPROVE / REJECT / null
    def stateFile = "${env.WORKSPACE}/assessment-state.json"

    // --- Load or initialize state ---
    def state = [:]
    if (fileExists(stateFile)) {
        state = new JsonSlurper().parse(new File(stateFile))
    }
    if (!state[gateName]) {
        state[gateName] = [
            status: 'PENDING',
            submittedAt: new Date().toString(),
            approver: requiredRole,
            escalationRole: escalationRole,
            slaMinutes: slaMinutes,
            autoDecision: autoDecision
        ]
        new File(stateFile).write(JsonOutput.toJson(state))
    }

    AuditUtils.log(this, [
        event: 'GATE_OPENED',
        gateName: gateName,
        requiredRole: requiredRole,
        slaMinutes: slaMinutes
    ])

    // --- Timeout + SLA handling ---
    timeout(time: slaMinutes, unit: 'MINUTES') {
        try {
            input(
                message: "Approve ${gateName}?",
                ok: "Approve",
                submitter: requiredRole
            )
            // Approved manually
            state[gateName].status = 'APPROVED'
            AuditUtils.log(this, [
                event: 'GATE_DECISION',
                gateName: gateName,
                decision: 'APPROVE',
                comments: config.comments ?: ''
            ])
        } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
            // Timeout expired
            if (autoDecision) {
                state[gateName].status = autoDecision
                AuditUtils.log(this, [
                    event: 'GATE_AUTO_DECISION',
                    gateName: gateName,
                    decision: autoDecision,
                    reason: 'SLA expired'
                ])
            } else if (escalationRole) {
                state[gateName].status = 'ESCALATED'
                AuditUtils.log(this, [
                    event: 'GATE_ESCALATED',
                    gateName: gateName,
                    escalationRole: escalationRole,
                    reason: 'SLA expired'
                ])
                // Optionally notify the escalationRole via email/Slack here
            } else {
                state[gateName].status = 'REJECTED'
                AuditUtils.log(this, [
                    event: 'GATE_DECISION',
                    gateName: gateName,
                    decision: 'REJECT',
                    reason: 'SLA expired with no autoDecision/escalation'
                ])
                error("Approval SLA expired and no autoDecision defined for ${gateName}")
            }
        } finally {
            // Persist state after decision / timeout
            new File(stateFile).write(JsonOutput.toJson(state))
        }
    }

    return state[gateName].status
}
