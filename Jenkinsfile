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
                git url: 'https://github.com/Adityarrudola/Three-Tier-App-Jenkins.git', branch: 'main'
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

        stage('Build Image') {
            steps {
                sh """
                docker build \
                -t ${IMAGE_NAME} \
                -t ${DOCKER_REPO}:latest \
                .
                """
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
                withCredentials([
                    string(credentialsId: 'azure-client-id', variable: 'AZ_CLIENT'),
                    string(credentialsId: 'azure-client-secret', variable: 'AZ_SECRET'),
                    string(credentialsId: 'azure-tenant-id', variable: 'AZ_TENANT'),
                    string(credentialsId: 'azure-subscription-id', variable: 'AZ_SUB')
                ]) {

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
                    --restart-policy Always
                    """
                }
            }
        }
    }
}
