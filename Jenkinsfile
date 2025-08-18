pipeline {
    agent any

    environment {
        AWS_REGION = "ap-northeast-2"
        AWS_CREDENTIALS = credentials('aws-user')
        ECR_REGISTRY = "048271428028.dkr.ecr.ap-northeast-2.amazonaws.com"
        ECR_REPO = "sigma-backend"
        DEPLOYMENT_NAME = "sigma-backend"
        K8S_NAMESPACE   = "default"
    }

    stages{
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                  branches: [[name: "*/main"]],
                  userRemoteConfigs: [[
                    url: 'https://github.com/AWS-AI-team3/sigma-BE.git',
                    credentialsId: 'github-pat'
                  ]]
                ])
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t $ECR_REGISTRY/$ECR_REPO:latest ."
                }
            }
        }

        stage('Login to ECR') {
            steps {
                sh """
                aws ecr get-login-password --region $AWS_REGION \
                | docker login --username AWS --password-stdin $ECR_REGISTRY
                """
            }
        }

        stage('Push to ECR') {
            steps {
                sh "docker push $ECR_REGISTRY/$ECR_REPO:latest"
            }
        }

        stage('Restart Deployment') {
            steps {
                script {
                    sh "kubectl rollout restart deployment/${DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}"

                    // 롤아웃 완료될 때까지 대기
                    sh "kubectl rollout status deployment/${DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}"
                }
            }
        }

    }

}