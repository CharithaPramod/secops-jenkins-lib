def init(appName, assessmentId) {
    echo "[AssessmentStage] Initializing assessment for ${appName} with ID ${assessmentId}"

    def state = [
        appName: appName,
        assessmentId: assessmentId,
        timestamp: new Date().toString(),
        status: 'initialized'
    ]
    
    org.secops.StateStore.save(this, assessmentId, state)
}
