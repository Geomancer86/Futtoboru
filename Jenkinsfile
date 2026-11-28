// Futtoboru CI on the local Jenkins (same pattern as DarkBlade: labelled agent, parameters, mvn + junit).
// Mirrors .github/workflows/ci.yml so a ws/ or b3/ branch can be checked here before it is merged into develop.
// Jenkins setup (once): a Pipeline job "Futtoboru DEV" with "Pipeline script from SCM" -> this repo, script path "Jenkinsfile".
// T-01 test tiers (D27, docs/BUILD.md "Test tiers"): TEST_TIER=auto runs
//   a develop commit (pollSCM)  -> slow tier + coverage (-Pslow,coverage; the fast tier ran at the lane's gate and on GitHub)
//   the nightly (cron, develop) -> full + smoke + coverage (-Pfull,smoke,coverage); red -> bisect the day's merges
//   main                        -> full + smoke + coverage
//   any other BUILD_BRANCH      -> fast tier (ask for TEST_TIER=full to verify a branch before merging it)
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
        choice(
            name: 'TEST_TIER',
            choices: ['auto', 'fast', 'slow', 'full', 'smoke'],
            description: 'auto: slow for develop commits, full+smoke+coverage at night, full+smoke for main, fast for other branches. smoke = full + the desktop smoke test'
        )
        booleanParam(
            name: 'BISECT_ON_RED',
            defaultValue: true,
            description: 'Nightly only: when the full tier fails, git bisect (first parent) between the last green nightly and HEAD'
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
        cron('H 3 * * *')   // T-01: the nightly full tier of develop
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
                    env.GIT_COMMIT_FULL = isUnix()
                        ? sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        : bat(script: '@git rev-parse HEAD', returnStdout: true).trim()
                    // T-01: which tier this build runs
                    env.NIGHTLY = currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause') ? 'true' : 'false'
                    def tier = params.TEST_TIER ?: 'auto'
                    if (tier == 'auto') {
                        if (env.NIGHTLY == 'true') {
                            tier = 'nightly'
                        } else if (params.BUILD_BRANCH == 'develop') {
                            tier = 'slow'
                        } else if (params.BUILD_BRANCH == 'main') {
                            tier = 'smoke'
                        } else {
                            tier = 'fast'
                        }
                    }
                    env.TIER = tier
                    // T-02: coverage (JaCoCo) only in the long Jenkins runs, never in the fast tier
                    env.MVN_PROFILES = [fast: '', slow: '-Pslow,coverage', full: '-Pfull,coverage', smoke: '-Pfull,smoke,coverage',
                                        nightly: '-Pfull,smoke,coverage'][tier]
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.BUILD_BRANCH} @ ${env.GIT_COMMIT_SHORT} ${tier}"
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
                                xvfb-run -a ./mvnw -B -ntp clean verify $MVN_PROFILES
                            else
                                ./mvnw -B -ntp clean verify $MVN_PROFILES
                            fi
                            python3 tools/test_tiers.py || echo "WARNING: an untagged test class over 5 s (tools/test_tiers.py)"
                        '''
                    } else {
                        bat "mvnw.cmd -B -ntp clean verify ${env.MVN_PROFILES}"
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml', allowEmptyResults: true
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
            echo "Futtoboru build ${env.BUILD_NUMBER}: branch ${params.BUILD_BRANCH} @ ${env.GIT_COMMIT_SHORT ?: 'N/A'} tier ${env.TIER ?: '?'} -> ${currentBuild.currentResult}"
        }
        failure {
            script {
                // T-01: a red nightly bisects the merges since the last green nightly (first parent: one step per merge)
                def good = currentBuild.previousSuccessfulBuild?.buildVariables?.GIT_COMMIT_FULL
                if (env.NIGHTLY == 'true' && params.BISECT_ON_RED && good && isUnix()) {
                    sh """
                        git bisect start --first-parent ${env.GIT_COMMIT_FULL} ${good}
                        git bisect run sh -c 'xvfb-run -a ./mvnw -B -ntp -q -o test -Pfull' || true
                        git bisect log | tee bisect.log
                        git bisect reset
                    """
                    archiveArtifacts artifacts: 'bisect.log', allowEmptyArchive: true
                } else {
                    echo "no bisect (nightly=${env.NIGHTLY}, last green=${good ?: 'unknown'})"
                }
            }
        }
    }
}
