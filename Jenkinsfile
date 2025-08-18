pipeline {
    agent any

    environment {
        AWS_REGION = "ap-northeast-2"
        AWS_CREDENTIALS = credentials('aws-user')
        ECR_REGISTRY = "048271428028.dkr.ecr.ap-northeast-2.amazonaws.com"
        ECR_REPO = "sigma-backend"
        DEPLOYMENT_NAME = "sigma-backend"
        K8S_NAMESPACE   = "default"
        IMAGE_TAG       = "${BUILD_NUMBER}"
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
                    sh "docker build -t $ECR_REGISTRY/$ECR_REPO:${IMAGE_TAG} ."
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
                sh "docker push $ECR_REGISTRY/$ECR_REPO:${IMAGE_TAG}"
            }
        }

        stage('Restart Deployment') {
            steps {
                script {
                    sh """
                          export KUBECONFIG=/root/.kube/config
                          sed "s|__IMAGE_TAG__|50|g" k8s/deployment.yaml > k8s/deployment_rendered.yaml
                          kubectl apply -f k8s/deployment_rendered.yaml
                          kubectl rollout status deployment/sigma-backend -n default
                       """
                }
            }
        }

    }

}