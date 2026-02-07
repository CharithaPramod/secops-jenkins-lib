def run(appName, assessmentId) {
    echo "[SecretScan] Scanning for secret keys in ${appName}"
    
    // Example: GitLeaks
    sh "gitleaks detect --source . --report-path ./.secops/state/${assessmentId}_secrets.json"
    
    def state = org.secops.StateStore.load(this, assessmentId)
    state.secretScan = [status: "completed", timestamp: new Date().toString()]
    org.secops.StateStore.save(this, assessmentId, state)
}
