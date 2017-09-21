pipeline {
  stages {
    stage('Clone sources') {
      steps {
        git url: 'https://github.com/Tim-Snow/HomeMonitor.git'
        checkout scm
      }  
    }

    stage('Build project') {
      steps {
        sh "./gradlew clean assemble"
      }
    }
  }
}
