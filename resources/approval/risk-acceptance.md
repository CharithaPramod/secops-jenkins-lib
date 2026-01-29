@Library('secops-lib') _

pipeline {
    agent any
    stages {
        stage('Test Library') {
            steps {
                script {
                    echo "Shared library loaded successfully"
                }
            }
        }
    }
}
