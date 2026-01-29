package org.secops

class AuditUtils implements Serializable {

    static void log(Map data) {
        echo """
        [AUDIT]
        Gate      : ${data.gateName}
        Role      : ${data.requiredRole}
        Build     : ${env.BUILD_NUMBER}
        Job       : ${env.JOB_NAME}
        Triggered : ${currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause)?.userId}
        """
    }
}

def call(Map data) {
    log(data)
}
