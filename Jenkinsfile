/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pipeline {
  agent any

  environment {
    PROJECT_NAME = 'runtime-ce'
    BUILD_VERSION_NAME= "1.7.1-${BRANCH_NAME}"
    VERSION_NAME= "${BRANCH_NAME}"
  }

  tools {
    // the 'Maven' is the name pre-configured in Global Tool Configuration
    maven 'Maven'
  }

  stages {
    stage('NOCI check') {
      steps {
        abortPreviousBuilds()

        noci action: 'check'

        load_envs_common()
        load_envs_runtime()
      }
    }

    stage('Print information') {
      steps {
        printInformation()
      }
    }

    stage('Compile') {
      steps {
        sh """
          mvn clean
          mvn build-helper:parse-version versions:set -DgenerateBackupPoms=false -DnewVersion=${env.BUILD_VERSION_NAME}
          if [[ "${BRANCH_NAME}" =~ '^PR.*' ]]; then
            mvn -T 4 install -Dorchsym.product.version=${env.VERSION_NAME} ${env.MVN_EXTRA_OPTS};
          else
            mvn -T 4 install -Dorchsym.product.version=${env.VERSION_NAME} -DskipTests -Dmaven.test.failure.ignore=true ${env.MVN_EXTRA_OPTS};
          fi
          echo "\n${env.PROJECT_NAME}-${env.VERSION_NAME}.tar.gz" >> ${env.BUILD_OUTPUT_FILE}
        """

        sendNotifications("INFO", "Compile finished")
      }
    }

    stage('Copy to Ansible host') {
      when { not { expression { BRANCH_NAME ==~ '^PR.*' } } }

      steps {
        copyServiceFileToAnsible("orchsym/orchsym-assembly/target/${env.PROJECT_NAME}-${env.VERSION_NAME}.tar.gz")
      }
    }

    stage('Deploy to Dev') {
      when { branch "${env.RUNTIME_DEV_VERSION}" }

      steps {
        deployToEnvironment(
          "${env.RUNTIME_DEV_NAME}",
          "${env.VERSION_NAME}",
          "${env.RUNTIME_DEV_ENVIRONMENT}"
        )
      }
    }

    stage('Deploy to Test') {
      when { branch "${env.RUNTIME_TEST_VERSION}" }

      steps {
        deployToEnvironment(
          "${env.RUNTIME_TEST_NAME}",
          "${env.VERSION_NAME}",
          "${env.RUNTIME_TEST_ENVIRONMENT}"
        )
      }
    }

    stage('Deploy to Stage') {
      when { branch "${env.RUNTIME_STAGE_VERSION}" }

      steps {
        deployToEnvironment(
          "${env.RUNTIME_STAGE_NAME}",
          "${env.VERSION_NAME}",
          "${env.RUNTIME_STAGE_ENVIRONMENT}"
        )
      }
    }

    stage('Deploy to Prod') {
      when { branch "${env.RUNTIME_PROD_VERSION}" }

      steps {
        deployToEnvironment(
          "${env.RUNTIME_PROD_NAME}",
          "${env.VERSION_NAME}",
          "${env.RUNTIME_PROD_ENVIRONMENT}"
        )
      }
    }

    // Enable it later
    // stage('Build/Push docker image') {
    //   when { not { expression { BRANCH_NAME ==~ '^PR.*' } } }

    //   steps {
    //     buildAndPushDockerImage("--build-arg VERSION_NAME=${env.VERSION_NAME} --pull -f Dockerfile .")
    //   }
    // }

	// upload S3 faster than Samba, so upload first
    stage('Upload to s3') {
      when { not { expression { BRANCH_NAME ==~ '^PR.*' } } }

      steps {
        uploadServiceFile("orchsym/orchsym-assembly/target/${env.PROJECT_NAME}-${env.VERSION_NAME}.tar.gz")
      }
    }

    stage('Upload to Samba') {
      when { not { expression { BRANCH_NAME ==~ '^PR.*' } } }

      steps {
        uploadServiceFileToSamba("orchsym/orchsym-assembly/target/${env.PROJECT_NAME}-${env.VERSION_NAME}.tar.gz")
      }
    }

    stage('Capture Output') {
      steps {
        captureBuildOutput()
      }
    }
  }

  post {
    always {
      noci action: 'postProcess'
    }

    aborted {
      sendNotifications("ABORTED", "Build aborted")
    }

    unstable {
      sendNotifications("UNSTABLE", "Build unstable")
    }

    success {
      sendNotifications("SUCCESS", "Build succeed", "${env.build_output}")
    }

    failure {
      sendNotifications("FAILURE", "Build failed")
    }
  }
}
