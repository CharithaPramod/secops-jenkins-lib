def run(appName, assessmentId) {
    echo "[DAST] Running dynamic analysis for ${appName} (ID: ${assessmentId})"
    
    // Example: OWASP ZAP
    sh "zap-cli start && zap-cli quick-scan http://localhost:8080 && zap-cli stop"
    
    def state = org.secops.StateStore.load(this, assessmentId)
    state.dast = [status: "completed", timestamp: new Date().toString()]
    org.secops.StateStore.save(this, assessmentId, state)
}
