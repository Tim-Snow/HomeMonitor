stage 'build_Project'
node{
  
  git url: 'https://github.com/Tim-Snow/HomeMonitor.git'
  
  if(isUnix()){
  sh 'gradle build --info'

  }
  else{
    bat 'gradle build --info'
  }
}
