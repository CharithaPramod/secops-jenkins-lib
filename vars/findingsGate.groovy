import org.secops.FindingsUtils
import org.secops.Severity
import org.secops.AuditUtils

def call(Map config = [:]) {

    // ---- Validation ----
    ['gateName', 'severityThreshold'].each {
        if (!config[it]) {
            error("findingsGate requires ${it}")
        }
    }

    def allowRiskAcceptance = config.allowRiskAcceptance ?: false
    def riskApproverRole = config.riskApproverRole ?: 'Security Auditor'

    // ---- Fetch findings ----
    def findings = FindingsUtils.getFindings(config.gateName)

    AuditUtils.log(this, [
        event: 'FINDINGS_EVALUATED',
        gateName: config.gateName,
        findings: findings
    ])

    // ---- Check severity ----
    if (!Severity.exceeds(findings.highestSeverity, config.severityThreshold)) {
        echo "No blocking findings detected for ${config.gateName}"
        return
    }

    // ---- Findings detected → decision required ----
    timeout(time: 3, unit: 'DAYS') {

        def choices = ['FIX_AND_RETRY']
        if (allowRiskAcceptance) {
            choices << 'ACCEPT_RISK'
        }

        def decision = input(
            id: "${config.gateName}_FINDINGS",
            message: """
Findings detected in ${config.gateName}

Highest severity : ${findings.highestSeverity}
Threshold        : ${config.severityThreshold}
Summary          : ${findings.summary}
""",
            ok: "Submit Decision",
            parameters: [
                choice(
                    name: 'DECISION',
                    choices: choices.join('\n'),
                    description: 'Select how to proceed'
                ),
                text(
                    name: 'JUSTIFICATION',
                    description: 'Mandatory justification'
                )
            ]
        )

        // ---- Decision audit ----
        AuditUtils.log(this, [
            event: 'FINDINGS_DECISION',
            gateName: config.gateName,
            decision: decision.DECISION,
            justification: decision.JUSTIFICATION
        ])

        if (decision.DECISION == 'FIX_AND_RETRY') {
            error("Findings must be fixed before proceeding")
        }

        if (decision.DECISION == 'ACCEPT_RISK') {

            // Role enforcement happens HERE
            if (!currentBuild.rawBuild.getCause(hudson.model.Cause.UserIdCause)
                    ?.userName) {
                error("Unable to identify approving user")
            }

            AuditUtils.log(this, [
                event: 'RISK_ACCEPTED',
                gateName: config.gateName,
                acceptedByRole: riskApproverRole
            ])
        }
    }
}
