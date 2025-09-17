pipeline {
    agent any

    tools {
        maven 'Maven3'       // el nombre  configurado en Jenkins para Maven
        jdk 'JDK21'          // el nombre JDK configurado en Jenkins
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
            junit '**/target/surefire-reports/*.xml'   // recoge resultados de tests JUnit
        }
    }
}
