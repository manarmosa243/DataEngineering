@Library('my-sharedlib') _

pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'build-1', description: 'Docker image tag')
    }

    environment {
        DOCKER_CREDENTIALS = credentials('dockerHub')
        DOCKER_IMAGE = "manarmosa/java-app:${params.IMAGE_TAG}"
    }

    stages {
        stage('Build') {
            parallel {
                stage('Build JAR') {
                    steps {
                        sh 'mvn clean package -DskipTests'
                    }
                }
                stage('Run Bounds SharedLib') {
                    steps {
                        script {
                            bounds(1, 100)   // Example use of shared lib
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Login to Docker Hub') {
            steps {
                sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push ${DOCKER_IMAGE}"
            }
        }
    }

    post {
        always {
            sh 'mvn clean'
            sh 'docker system prune -f'
        }
    }
}
