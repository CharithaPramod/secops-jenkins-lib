def generate(appName, assessmentId) {
    echo "[AppSecReport] Generating consolidated report for ${appName}"
    
    def state = org.secops.StateStore.load(this, assessmentId)
    
    def reportFile = ".secops/state/${assessmentId}_report.txt"
    writeFile file: reportFile, text: "AppSec Report for ${appName}\n\n${groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(state))}"
    
    echo "[AppSecReport] Report saved at ${reportFile}"
}
