pipeline {
    agent any

    environment {
        customImage = null
    }

    tools {
        // Install latest Maven "Maven 3.8.5 JENKINS"
        maven "Maven 3.8.5 JENKINS"
        
        // Install latest JDK "OpenJDK 17"
        jdk "OpenJDK 17"
    }

    stages {
        stage('GIT Download') {
            steps {
                echo 'Git download'
                /**
                 *  Landing Page Bitbucket
                 */
                git branch: 'develop',
                    credentialsId: 'GITHUB_GEOMANCER86',
                    url: 'https://github.com/Geomancer86/Futtoboru.git'
            }
        }
        
        stage("Test"){
            steps {
                // Run Maven on a Unix agent.
                wrap([$class: 'Xvfb']) {
                    sh "mvn -Dmaven.test.failure.ignore=true clean package"
                    sh "mvn -Dmaven.test.failure.ignore=true test"
                }
            }
            
            post {
                // If Maven was able to run the tests, even if some of the test
                // failed, record the test results and archive the jar file.
                success {
                    junit 'futtoboru-core/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'futtoboru-core/target/*.jar'
                    echo "Maven Build Archive Artifacts Done"
                }
            }
        }
        
        // 
        stage("Build") {
            steps {
                wrap([$class: 'Xvfb']) {
                    //sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ArtEvolver4---DEV -Dsonar.host.url=http://192.168.0.102:9000 -Dsonar.login=3c0d9e9e2d84467a06d9d0a52c40f527daf0cb81'
                }
            }
        }
        
        stage("Test Reports") {
            steps {
                dir('futtoboru-core') {
                    jacoco(
                            execPattern: 'target/*.exec',
                            classPattern: 'target/classes',
                            sourcePattern: 'src/main/java',
                            exclusionPattern: 'src/test*',
                            changeBuildStatus: true,
                            runAlways: true,
                            minimumBranchCoverage: '0'
                    )
                }
            }
        }

        
            stage('Sonar') {
                
                    steps {
                        echo 'Sonar Analysis'
                        
                        //wrap([$class: 'Xvfb', screen: '1920x1080x32']) {
                        wrap([$class: 'Xvfb']) {
                            // Run Sonar 
                            // sh "mvn clean verify -Djacoco.skip=false sonar:sonar -Dsonar.projectKey=ArtEvolver4---DEV -Dsonar.host.url=http://192.168.0.102:9000 -Dsonar.login=3c0d9e9e2d84467a06d9d0a52c40f527daf0cb81"
                        }        
                    }
                           
                post {
                    // If Maven was able to run the tests, even if some of the test
                    // failed, record the test results and archive the jar file.
                    success {
                        echo "Sonar Gateway Passed"
                    }
                }
            }
        
        
 //       stage('Docker') {
 //           steps {
 //               echo 'Building Container..'
  //              /**
 //                * Build Landing Page DEVELOPMENT Docker Image
  //               * 
  //               * rndmodgames/landing-page
  //               */
 //               //script {
 //              //    customImage = docker.build("rndmodgames/landing-page:${env.BUILD_ID}")
  //              //    customImage.tag('latest');
 //               //}
 //           }
 //      }
        //stage('Deploy'){
        //    steps {
        //        echo 'Running Container..'
//
 //               // ensure container is not already running
 //               //sh 'docker ps -f name=rndmodgames-landing-page -q | xargs --no-run-if-empty docker container stop'
 //               //sh 'docker container ls -a -fname=rndmodgames-landing-page -q | xargs -r docker container rm'
 //               
  //              // run the container named rndmodgames/backoffice
  //              // sh "docker run --name=rndmodgames-landing-page -d -p 8081:8080 rndmodgames/landing-page:${env.BUILD_ID}" 
  //              //sh "docker run --net preprod_network --ip 172.18.0.2 --name=rndmodgames-landing-page -d -p 8081:8080 rndmodgames/landing-page:${env.BUILD_ID}"
   //         }
  //      }
        
        //stage('Check Availability') {
        //    options {
        //        timeout(time: 30, unit: "SECONDS")
        //    }
        //    steps {  
        //        waitUntil {
        //            script {
        //                try {
        //                    sh "curl -s --head  --request GET  localhost:8081/landing | grep '200'"
        //                    echo "Landing Page is ONLINE"
        //                    return true;
        //                    //echo "Started stage A"
        //                    //sleep(time: 5, unit: "SECONDS")
        //                } catch (Throwable e) {
        //                    // echo "Caught ${e.toString()}"
        //                    // currentBuild.result = "SUCCESS" 
        //                    return false
        //                }
        //            }
        //        }
        //   }
        //}
    }
}
