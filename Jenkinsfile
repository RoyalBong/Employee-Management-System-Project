pipeline {
    agent any
    tools {
        maven 'Maven3' 
        jdk 'Oracle JDK17' 
    }
    environment {
        DOCKER_REGISTRY = 'shayandutta98' 
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id') 
        MYSQL_PASSWORD = credentials('mysql-password') 
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm 
            }
        }
        stage('Build Services') {
            parallel {
                stage('Employee Service') {
                    steps {
                        dir('employee-service') {
                            sh 'mvn clean install -DskipTests' 
                        }
                    }
                    post {
                        always {
                            dir('employee-service') {
                                archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                }
                stage('Department Service') {
                    steps {
                        dir('department-service') {
                            sh 'mvn clean install -DskipTests' 
                        }
                    }
                    post {
                        always {
                            dir('department-service') {
                                archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
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
                    sh 'docker compose -f docker-compose-deploy.yml down || true' 
                    sh 'docker compose -f docker-compose-deploy.yml up -d'
                    sh 'sleep 60' 
                    sh 'curl --fail http://localhost:8081/actuator/health || exit 1' 
                    sh 'curl --fail http://localhost:8082/actuator/health || exit 1' 
                    sh 'curl --fail http://localhost:15672 || exit 1' 
                }
            }
            post {
                always {
                    sh 'docker compose -f docker-compose-deploy.yml down || true'
                }
            }
        }
        stage('Deploy Application') {
            when {
                branch 'main'
            }
            steps {
                script {
                    sh 'docker compose -f docker-compose-deploy.yml down || true'
                    sh 'docker compose -f docker-compose-deploy.yml up -d'
                    echo 'Deployed at http://localhost:8081 (employee), http://localhost:8082 (department), RabbitMQ UI: http://localhost:15672'
                }
            }
        }
    }
    post {
        always {
            sh "docker rmi ${DOCKER_REGISTRY}/employee-service:${BUILD_NUMBER} || true"
            sh "docker rmi ${DOCKER_REGISTRY}/department-service:${BUILD_NUMBER} || true"
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs.'
            sh 'docker compose -f docker-compose-deploy.yml down || true'
        }
    }
}