pipeline {
    agent any  // Requires Maven, JDK 17, Docker, and docker-compose

    environment {
        DOCKER_REGISTRY = 'shayandutta98'  // Your Docker Hub username
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')  // Your credential ID
        MYSQL_PASSWORD = credentials('mysql-password')  // For MySQL
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm  // Pulls from GitHub
            }
        }

        stage('Build and Test Services') {
            parallel {
                stage('Employee Service') {
                    steps {
                        dir('employee-service') {
                            sh 'mvn clean compile'
                            sh 'mvn test'
                            sh 'mvn package -DskipTests'
                        }
                    }
                    post {
                        always {
                            dir('employee-service') {
                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                archiveArtifacts artifacts: 'target/employee-service-docker.jar', allowEmptyArchive: true
                            }
                        }
                    }
                }
                stage('Department Service') {
                    steps {
                        dir('department-service') {
                            sh 'mvn clean compile'
                            sh 'mvn test'
                            sh 'mvn package -DskipTests'
                        }
                    }
                    post {
                        always {
                            dir('department-service') {
                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                archiveArtifacts artifacts: 'target/department-service-docker.jar', allowEmptyArchive: true
                            }
                        }
                    }
                }
            }
        }

        stage('Build and Push Docker Images') {
            parallel {
                stage('Employee Service Docker') {
                    steps {
                        dir('employee-service') {
                            script {
                                def image = docker.build("${DOCKER_REGISTRY}/employee-service:${BUILD_NUMBER}")
                                docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                                    image.push()
                                    image.push('latest')
                                }
                            }
                        }
                    }
                }
                stage('Department Service Docker') {
                    steps {
                        dir('department-service') {
                            script {
                                def image = docker.build("${DOCKER_REGISTRY}/department-service:${BUILD_NUMBER}")
                                docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                                    image.push()
                                    image.push('latest')
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Integration Test with Docker Compose') {
            steps {
                script {
                    sh 'docker-compose -f docker-compose-deploy.yml down || true'
                    sh 'docker-compose -f docker-compose-deploy.yml up -d'
                    sh 'sleep 30'  // Wait for services to stabilize
                    sh 'curl --fail http://localhost:8081/actuator/health || exit 1'
                    sh 'curl --fail http://localhost:8082/actuator/health || exit 1'
                    sh 'curl --fail http://localhost:15672 || exit 1'  // RabbitMQ UI
                }
            }
            post {
                always {
                    sh 'docker-compose -f docker-compose-deploy.yml down || true'
                }
            }
        }

        stage('Deploy Application') {
            when {
                branch 'main'  // Deploy only on main branch
            }
            steps {
                script {
                    sh 'docker-compose -f docker-compose-deploy.yml down || true'
                    sh 'docker-compose -f docker-compose-deploy.yml up -d'
                    echo 'Deployed at http://localhost:8081 (employee), http://localhost:8082 (department), RabbitMQ UI: http://localhost:15672'
                }
            }
        }
    }

    post {
        always {
            node {
                sh "docker rmi ${env.DOCKER_REGISTRY}/employee-service:${env.BUILD_NUMBER} || true"
                sh "docker rmi ${env.DOCKER_REGISTRY}/department-service:${env.BUILD_NUMBER} || true"
                cleanWs()
            }
        }
        success {
            node {
                echo 'Pipeline completed successfully!'
            }
        }
        failure {
            node {
                echo 'Pipeline failed. Check logs.'
                sh 'docker-compose -f docker-compose-deploy.yml down || true'
            }
        }
    }
}