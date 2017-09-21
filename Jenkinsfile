node{
  
  stage 'Clone sources' {
    steps {
      git url: 'https://github.com/Tim-Snow/HomeMonitor.git'
      checkout scm
    }  
  }
  
  stage 'build_Project' {
    steps {
      sh "./gradlew clean assemble"
    }
  }
  
}
