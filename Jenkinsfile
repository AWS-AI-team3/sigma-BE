pipeline {
    agent any

    environment {
        AWS_REGION = "ap-northeast-2"
        AWS_CREDENTIALS = credentials('aws-user')
        GITHUB_PAT = credentials('github-pat')
        ECR_REGISTRY = "048271428028.dkr.ecr.ap-northeast-2.amazonaws.com"
        ECR_REPO = "sigma-backend"
    }

    stages{
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/AWS-AI-team3/sigma-BE.git']]
                ])
            }
        }

        stage('Generate Image Tag') {
            steps {
                script {
                    // UNIX timestamp 기반 태그 (충돌 거의 없음)
                    IMAGE_TAG = sh(returnStdout: true, script: "date +%s").trim()
                    env.IMAGE_TAG = IMAGE_TAG
                    echo "Generated IMAGE_TAG: ${IMAGE_TAG}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t $ECR_REGISTRY/$ECR_REPO:$IMAGE_TAG ."
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
                sh "docker push $ECR_REGISTRY/$ECR_REPO:$IMAGE_TAG"
            }
        }

        stage('Update manifest & Push') {
            steps {
                sh """
                sed -i 's#image: ${ECR_REGISTRY}/${ECR_REPO}:.*#image: ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}#' k8s/deployment.yaml
                git config user.email "linda284@naver.com"
                git config user.name "rudalsss"
                git add k8s/deployment.yaml
                git commit -m "[jenkins] Update image tag to ${IMAGE_TAG}" || echo "No changes to commit"

                git remote set-url origin https://${GITHUB_PAT}@github.com/AWS-AI-team3/sigma-BE.git
                git push origin HEAD:main
                """
            }
        }

    }

}