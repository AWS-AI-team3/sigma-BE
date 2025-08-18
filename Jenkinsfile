pipeline {
    agent any

    environment {
        AWS_REGION = "ap-northeast-2"
        AWS_CREDENTIALS = credentials('aws-user')
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
                withCredentials([string(credentialsId: 'github-pat-text', variable: 'GITHUB_PAT')])  {
                    sh """
                      echo "[DEBUG] PAT length: \${#GITHUB_PAT}"

                      sed -i 's#image: ${ECR_REGISTRY}/${ECR_REPO}:.*#image: ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}#' k8s/deployment.yaml

                      git config user.name "rudalsss"
                      git config user.email "linda284@naver.com"
                      git remote set-url origin https://rudalsss:\$GITHUB_PAT@github.com/AWS-AI-team3/sigma-BE.git

                      echo "[DEBUG] Remote after set-url:"
                      git remote -v

                      echo "[DEBUG] Try ls-remote (auth test)"
                      git ls-remote origin || echo "[DEBUG] ls-remote failed"

                      git add k8s/deployment.yaml
                      git commit -m "[jenkins] Update image tag to ${IMAGE_TAG}" || echo "No changes to commit"
                      git push origin HEAD:main
                    """
                }
            }
        }


    }

}