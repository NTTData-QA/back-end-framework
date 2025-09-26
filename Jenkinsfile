pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh "mvn clean test"
            }
        }

    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
