def run(appName, assessmentId) {
    echo "[SAST] Running static analysis for ${appName} (ID: ${assessmentId})"
    
    // Example: SonarQube CLI
    sh "sonar-scanner -Dsonar.projectKey=${appName} -Dsonar.sources=."
    
    def state = org.secops.StateStore.load(this, assessmentId)
    state.sast = [status: "completed", timestamp: new Date().toString()]
    org.secops.StateStore.save(this, assessmentId, state)
}
