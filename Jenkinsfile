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
    }
}