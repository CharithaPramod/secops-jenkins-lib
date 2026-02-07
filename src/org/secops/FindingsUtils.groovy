package org.secops

class FindingsUtils {

    static Map getFindings(String gateName) {
        // TEMPORARY MOCK
        return [
            total: 5,
            highestSeverity: 'HIGH',
            summary: [
                CRITICAL: 0,
                HIGH: 2,
                MEDIUM: 3
            ]
        ]
    }
}
