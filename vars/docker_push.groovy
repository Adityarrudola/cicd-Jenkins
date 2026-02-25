def call(String ProjectName, String DockerHubUser, String ImageTag){
  withCredentials([
    usernamePassword(
      credentialsId:"docker-hub-cred",
      passwordVariable:"dockerHubPass",
      usernameVariable:"dockerHubUser")
    ]){
      sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPass}"
      sh "docker push ${env.dockerHubUser}/${ProjectName}:${ImageTag}"
    }
}
