package org.secops

class AuditUtils {
    static void logApproval(String gate, String decision, String comments) {
        println "[AUDIT] Gate=${gate} Decision=${decision}"
        println "[AUDIT] Comments=${comments}"
    }
}
