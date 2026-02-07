package org.secops

class AuditUtils {
    static void log(def script, Map data) {
        script.echo "[AUDIT] ${data}"
    }
}
