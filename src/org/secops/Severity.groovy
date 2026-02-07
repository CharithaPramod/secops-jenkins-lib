package org.secops

class Severity {
    static final Map LEVELS = [
        'CRITICAL': 4,
        'HIGH'    : 3,
        'MEDIUM'  : 2,
        'LOW'     : 1,
        'INFO'    : 0
    ]

    static boolean exceeds(String found, String threshold) {
        LEVELS[found] >= LEVELS[threshold]
    }
}
