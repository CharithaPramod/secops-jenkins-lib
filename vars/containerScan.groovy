def run(appName, assessmentId) {
    echo "[ContainerScan] Scanning container images for ${appName}"
    
    // Example: Trivy
    sh "trivy image --severity HIGH,CRITICAL ${appName}:latest --format json --output ./.secops/state/${assessmentId}_container.json"
    
    def state = org.secops.StateStore.load(this, assessmentId)
    state.containerScan = [status: "completed", timestamp: new Date().toString()]
    org.secops.StateStore.save(this, assessmentId, state)
}
