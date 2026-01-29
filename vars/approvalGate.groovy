import org.secops.AuditUtils

def call(Map config = [:]) {

    AuditUtils.log(this, [
        gateName: 'PRE_SCAN',
        requiredRole: 'DevOps Engineer'
    ])


    timeout(time: 30, unit: 'MINUTES') {
        input(
            message: "Approve ${config.gateName}",
            ok: "Approve",
            submitter: config.requiredRole
        )
    }
}
