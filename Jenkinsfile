pipeline {
    agent any  

    environment {
        DOCKER_REGISTRY = 'royalbong'  
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'  /
        BUILD_NUMBER = "${env.BUILD_NUMBER}"  
        MYSQL_PASSWORD = credentials('mysql-password')  // Secured MySQL password from Jenkins credentials
    }

    stages {
        stage('Checkout') {
            steps {
                
                checkout scm  
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
                                docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
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
                                docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
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
                    
                    sh 'sleep 30'
                    
                    sh 'curl --fail http://localhost:8081/actuator/health || exit 1'
                    sh 'curl --fail http://localhost:8082/actuator/health || exit 1'
                    sh 'curl --fail http://localhost:15672 || exit 1'  
                }
            }
            post {
                always {
                    
                    sh 'docker-compose -f docker-compose-deploy.yml down || true'
                }
            }
        }

        stage('Local Deployment') {
            when {
                branch 'main'  
            }
            steps {
                script {
                    
                    sh 'docker-compose -f docker-compose-deploy.yml down || true'
                    
                    sh 'docker-compose -f docker-compose-deploy.yml up -d'
                    echo 'Services deployed locally. Access at http://localhost:8081 (employee) and http://localhost:8082 (department).'
                    echo 'RabbitMQ UI: http://localhost:15672 (guest/guest)'
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
            echo 'Pipeline completed! Services are running locally.'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
            sh 'docker-compose -f docker-compose-deploy.yml down || true'
        }
    }
}