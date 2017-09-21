pipeline {
  
  agent any
  
  stages {
    
    stage('Clone sources') {
      steps {
        git url: 'https://github.com/Tim-Snow/HomeMonitor.git'
        checkout scm
      }  
    }

    stage('Test') {
      steps {
        sh "./gradlew clean testClasses"
      }
    }
    
  }
  
  post {
    
    always {
      junit '**/target/*.xml'
    }
    
    failure {
        //mail to: team@example.com, subject: 'The Pipeline failed :('
    }
    
  }
  
}
