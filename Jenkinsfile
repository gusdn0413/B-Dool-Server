pipeline {
    agent {
        kubernetes {
            label 'kubernetes-CICD'
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/label: kubernetes-CICD
spec:
  ttlSecondsAfterFinished: 600
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
    resources:
      limits:
        memory: 1Gi
        cpu: 1
      requests:
        memory: 1Gi
        cpu: 1
    volumeMounts:
    - mountPath: "/home/jenkins/agent"
      name: workspace-volume
  - name: kubectl
    image: bitnami/kubectl:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - mountPath: "/home/jenkins/agent"
      name: workspace-volume
  - name: buildkit
    image: moby/buildkit:latest
    securityContext:
      privileged: true
    args:
    - --addr=unix:///run/buildkit/buildkitd.sock
    volumeMounts:
    - name: workspace-volume
      mountPath: /home/jenkins/agent
    - name: buildkit-socket
      mountPath: /run/buildkit
  volumes:
  - name: workspace-volume
    emptyDir: {}
  - name: buildkit-socket
    emptyDir: {}
"""
        }
    }

    environment {
        DOCKER_CREDENTIALS_ID = 'docker-hub'
        REPO_URL = 'https://github.com/jungmin7315/B-Dool-Server.git'
        DOCKER_HUB_URL = 'kang1521'
        IMAGE_NAME = "${DOCKER_HUB_URL}/chat"
    }

    stages {
        stage('Clone Repository') {
            steps {
                git url: "${REPO_URL}", branch: 'master'
            }
        }

        stage('Build') {
            steps {
                dir('ChatService') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build'
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                dir('ChatService') {
                    container('buildkit') {
                        withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                            sh """
                            # Docker Hub 로그인
                            mkdir -p ~/.docker
                            echo '{ "auths": { "https://index.docker.io/v1/": { "username": "'$DOCKER_USERNAME'", "password": "'$DOCKER_PASSWORD'" } } }' > ~/.docker/config.json

                            # Dockerfile 복사
                            cp Dockerfile copied_Dockerfile

                            # BuildKit을 사용하여 이미지 빌드 및 푸시
                            buildctl --addr unix:///run/buildkit/buildkitd.sock build \
                                --frontend dockerfile.v0 \
                                --local context=. \
                                --local dockerfile=. \
                                --opt filename=copied_Dockerfile \
                                --output type=image,name=${IMAGE_NAME}:${BUILD_NUMBER},push=true

                            buildctl --addr unix:///run/buildkit/buildkitd.sock build \
                                --frontend dockerfile.v0 \
                                --local context=. \
                                --local dockerfile=. \
                                --opt filename=copied_Dockerfile \
                                --output type=image,name=${IMAGE_NAME}:latest,push=true
                            """
                        }
                    }
                }
            }
        }

//         stage('Deploy') {
//             steps {
//                 echo 'Deploying ChatService to Kubernetes...'
//                 container('kubectl') {
//                     withCredentials([
//                         string(credentialsId: 'ncloud_access_key', variable: 'NCLOUD_ACCESS_KEY'),
//                         string(credentialsId: 'ncloud_secret_key', variable: 'NCLOUD_SECRET_KEY'),
//                         string(credentialsId: 'ncloud_cluster_uuid', variable: 'CLUSTER_UUID')
//                     ]) {
//                         withKubeConfig([credentialsId: 'jungmin-kubeconfig']) {
//                             sh """
//                                 /home/jenkins/agent/kubectl get pods
//                                 /home/jenkins/agent/kubectl set image deployment/chat-service chat-service=${IMAGE_NAME}:${BUILD_NUMBER} -n default
//                                 /home/jenkins/agent/kubectl rollout status deployment/chat-service -n default
//                             """
//                         }
//                     }
//                 }
//             }
//         }
    }

    post {
        success {
            echo 'ChatService successfully built, pushed, and deployed.'
        }
        failure {
            echo 'Build or push failed.'
        }
    }
}