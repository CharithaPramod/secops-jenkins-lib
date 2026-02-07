def pushFindings(appName, assessmentId) {
    echo "[DefectDojo] Sending findings for ${appName}"
    
    def state = org.secops.StateStore.load(this, assessmentId)
    
    // Example: send JSON to DefectDojo API
    writeFile file: ".secops/state/${assessmentId}_combined.json", text: groovy.json.JsonOutput.toJson(state)
    
    sh "curl -X POST -H 'Authorization: Token YOUR_DEFECTDOJO_API_TOKEN' -F 'file=@.secops/state/${assessmentId}_combined.json' https://defectdojo.example.com/api/v2/import-scan/"
    
    state.defectDojo = [status: "completed", timestamp: new Date().toString()]
    org.secops.StateStore.save(this, assessmentId, state)
}
