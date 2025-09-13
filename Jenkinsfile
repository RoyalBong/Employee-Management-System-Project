pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')  // Example: if you use Docker Hub creds
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Print Build Number') {
            steps {
                script {
                    echo "Build Number: ${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Build') {
            steps {
                echo "Running build steps..."
                // Example build command
                sh 'echo Building the project...'
            }
        }

        stage('Push to DockerHub') {
            steps {
                script {
                    echo "Using DockerHub credentials: ${DOCKERHUB_CREDENTIALS_USR}"
                    // Example Docker login (replace with your commands)
                    sh "echo Logging in to DockerHub"
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying application..."
                // Add your deploy commands here
            }
        }
    }

    post {
        always {
            echo "Build finished. Build number was ${env.BUILD_NUMBER}"
        }
    }
}
