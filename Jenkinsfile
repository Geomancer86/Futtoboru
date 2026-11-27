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
                    archiveArtifacts 'futtoboru-desktop/target/*.jar'
                    echo "Maven Build Archive Artifacts Done"
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

        // stage('Sonar') {
            
        //         steps {
        //             echo 'Sonar Analysis'
                    
        //             //wrap([$class: 'Xvfb', screen: '1920x1080x32']) {
        //             wrap([$class: 'Xvfb']) {
        //                 // Run Sonar (URL and token come from Jenkins credentials, never literals)
        //                 withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN'),
        //                                  string(credentialsId: 'sonar-host-url', variable: 'SONAR_HOST_URL')]) {
        //                     sh 'mvn clean verify -Djacoco.skip=false sonar:sonar -Dsonar.projectKey=FuttoboruDEV -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN'
        //                 }
        //             }        
        //         }
                       
        //     post {
        //         // If Maven was able to run the tests, even if some of the test
        //         // failed, record the test results and archive the jar file.
        //         success {
        //             echo "Sonar Gateway Passed"
        //         }
        //     }
        // }    
    }
}