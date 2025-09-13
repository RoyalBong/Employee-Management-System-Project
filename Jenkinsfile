pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Print Build Info') {
            steps {
                script {
                    echo "Build Number: ${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image..."
                    sh "docker build -t shayandutta98/employee-management-system:${env.BUILD_NUMBER} ."
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    echo "Logging into Docker Hub and pushing image..."
                    sh """
                        echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    """
                    sh "docker push shayandutta98/employee-management-system:${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Deploy Application') {
            steps {
                echo "Deploying application locally..."

                script {
                    sh 'docker stop employee-management-system || true'
                    sh 'docker rm employee-management-system || true'
                    sh """
                        docker run -d --name employee-management-system \
                        -p 8081:8080 \
                        shayandutta98/employee-management-system:${env.BUILD_NUMBER}
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Build #${env.BUILD_NUMBER} finished."
        }
    }
}
