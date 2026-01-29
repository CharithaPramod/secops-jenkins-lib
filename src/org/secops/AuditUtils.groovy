package org.secops

class AuditUtils implements Serializable {

    static void log(def script, Map data) {
        script.echo """
[AUDIT]
Gate      : ${data.gateName}
Role      : ${data.requiredRole}
Build     : ${script.env.BUILD_NUMBER}
Job       : ${script.env.JOB_NAME}
"""
    }
}
