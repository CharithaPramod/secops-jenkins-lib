import org.secops.AuditUtils

def call(Map config = [:]) {

    AuditUtils.log(
        gateName: config.gateName ?: 'UNKNOWN',
        requiredRole: config.requiredRole ?: 'UNKNOWN'
    )

    timeout(time: 30, unit: 'MINUTES') {
        input(
            message: "Approve ${config.gateName}",
            ok: "Approve",
            submitter: config.requiredRole
        )
    }
}
