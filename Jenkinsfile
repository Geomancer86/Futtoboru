// Futtoboru CI on the local Jenkins (same pattern as DarkBlade: labelled agent, parameters, mvn + junit).
// Mirrors .github/workflows/ci.yml so a ws/ or b3/ branch can be checked here before it is merged into develop.
// Jenkins setup (once): a Pipeline job "FUTTOBORU" with "Pipeline script from SCM" -> this repo, script path "Jenkinsfile".
pipeline {

    agent {
        label 'EXECUTOR_BACKUP'
    }

    parameters {
        string(
            name: 'BUILD_BRANCH',
            defaultValue: 'develop',
            description: 'Branch to build: develop, main, or a ws/... or b3/... branch before merging it'
        )
        booleanParam(
            name: 'RUN_DATA_CHECKS',
            defaultValue: true,
            description: 'Run pytest tools/data and tools/data/validate.py (needs python3 + pytest on the agent)'
        )
    }

    tools {
        jdk 'OpenJDK 17'
    }

    // GitHub webhooks can't reach the LAN Jenkins: poll develop instead (cheap, no build when nothing changed)
    triggers {
        pollSCM('H/15 * * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.BUILD_BRANCH}"]],
                    extensions: [[$class: 'CleanBeforeCheckout']],
                    userRemoteConfigs: [[
                        credentialsId: 'GITHUB_GEOMANCER86',
                        url: 'https://github.com/Geomancer86/Futtoboru.git'
                    ]]
                ])
                script {
                    env.GIT_COMMIT_SHORT = isUnix()
                        ? sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        : bat(script: '@git rev-parse --short HEAD', returnStdout: true).trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.BUILD_BRANCH} @ ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Maven verify') {
            steps {
                script {
                    if (isUnix()) {
                        // libGDX tests need a display on Linux agents: xvfb-run (installed on EXECUTOR_BACKUP)
                        sh '''
                            chmod +x mvnw
                            if command -v xvfb-run >/dev/null 2>&1; then
                                xvfb-run -a ./mvnw -B -ntp clean verify
                            else
                                ./mvnw -B -ntp clean verify
                            fi
                        '''
                    } else {
                        bat 'mvnw.cmd -B -ntp clean verify'
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                }
                success {
                    archiveArtifacts artifacts: 'futtoboru-desktop/target/*-jar-with-dependencies.jar', fingerprint: true
                }
            }
        }

        stage('Game data') {
            when { expression { params.RUN_DATA_CHECKS } }
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            PY=python3
                            if ! $PY -m pytest --version >/dev/null 2>&1; then
                                python3 -m venv .ci-venv && .ci-venv/bin/pip install -q pytest
                                PY=.ci-venv/bin/python
                            fi
                            $PY -m pytest -q tools/data tools/art
                            $PY tools/data/validate.py
                            $PY docs/data/check_results_1888-89.py
                        '''
                    } else {
                        bat 'python -m pytest -q tools/data tools/art && python tools/data/validate.py'
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Futtoboru build ${env.BUILD_NUMBER}: branch ${params.BUILD_BRANCH} @ ${env.GIT_COMMIT_SHORT ?: 'N/A'} -> ${currentBuild.currentResult}"
        }
    }
}
