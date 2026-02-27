pipeline {
    agent { label 'jenkins-agent' }

    environment {
        DOCKER_REPO = "adityarrudola/react-app"
        IMAGE_NAME = "${DOCKER_REPO}:${BUILD_NUMBER}"
        RESOURCE_GROUP = "aditya"
        ACI_NAME = "azure-sp"
    }

    
    stages {

        stage('Approve Build') {
            steps { 
                input message: 'Do you want to proceed with the build?', ok: 'Yes, proceed'
            }
        }

        stage('Clone Repository') {
            steps {
                git url: 'https://github.com/Adityarrudola/cicd-Jenkins.git', branch: 'main'
            }
        }

        stage('Build Image') {
            steps {
                sh """
                docker build --no-cache \
                  -t ${IMAGE_NAME} \
                  -t ${DOCKER_REPO}:latest \
                  .
                """
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    '''
                }
            }
        }

        stage('Push Image') {
            steps {
                sh """
                docker push ${IMAGE_NAME}
                docker push ${DOCKER_REPO}:latest
                """
            }
        }

        stage('Deploy to Azure Container Instances') {
            steps {
                withCredentials([azureServicePrincipal(
                    credentialsId: 'azure-sp',
                    subscriptionIdVariable: 'AZ_SUB',
                    clientIdVariable: 'AZ_CLIENT',
                    clientSecretVariable: 'AZ_SECRET',
                    tenantIdVariable: 'AZ_TENANT'
                )]) {

                    sh """
                    az login --service-principal \
                    -u $AZ_CLIENT \
                    -p $AZ_SECRET \
                    --tenant $AZ_TENANT

                    az account set --subscription $AZ_SUB

                    az container delete \
                    --resource-group ${RESOURCE_GROUP} \
                    --name ${ACI_NAME} \
                    --yes || true

                    az container create \
                    --resource-group ${RESOURCE_GROUP} \
                    --name ${ACI_NAME} \
                    --image ${IMAGE_NAME} \
                    --dns-name-label react-app-${BUILD_NUMBER} \
                    --ports 80 \
                    --os-type Linux \
                    --cpu 1 \
                    --memory 1.5 \
                    --restart-policy Always
                    """
                }
            }
        }
    }

    post {
        success {
            emailext(
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2 style="color:green;">Build SUCCESS</h2>
                    <p><b>Job:</b> ${env.JOB_NAME}</p>
                    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Docker Image:</b> ${IMAGE_NAME}</p>
                    <p><b>URL:</b> ${env.BUILD_URL}</p>
                """,
                mimeType: 'text/html',
                to: 'aditya.rudola@quokkalabs.com'
            )
        }

        failure {
            emailext(
                subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2 style="color:red;">Build FAILED</h2>
                    <p><b>Job:</b> ${env.JOB_NAME}</p>
                    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Check Console:</b> ${env.BUILD_URL}</p>
                """,
                mimeType: 'text/html',
                to: 'aditya.rudola@quokkalabs.com'
            )
        }

    }
    
}
