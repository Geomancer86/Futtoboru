// Futtoboru CI on the local Jenkins (same pattern as DarkBlade: labelled agent, parameters, mvn + junit).
// Mirrors .github/workflows/ci.yml so a ws/ or b3/ branch can be checked here before it is merged into develop.
// Jenkins setup (once): a Pipeline job "Futtoboru DEV" with "Pipeline script from SCM" -> this repo, script path "Jenkinsfile".
// T-01 test tiers (D27, docs/BUILD.md "Test tiers"): TEST_TIER=auto runs
//   a develop commit (pollSCM)  -> full + smoke (-Pfull,smoke, D33); green -> archive playable-latest, move the tag green/develop
//   the nightly (cron, develop) -> full + smoke + coverage (-Pfull,smoke,coverage); red -> bisect the day's merges
//   main                        -> full + smoke
//   any other branch            -> fast tier (ask for TEST_TIER=full to verify a branch before merging it)
// INF-01 (D33): no disableConcurrentBuilds, so branches build side by side on the shared executors. The "Futtoboru DEV"
// job builds develop (and main / a chosen branch by hand); the multibranch job "Futtoboru Branches" builds ws/* and b3/*
// with this same file (BRANCH_NAME set; one job per branch = one build per branch at a time, a newer push aborts the
// older build of that branch through milestone). The triggers below are for the DEV job only.
pipeline {

    agent {
        label 'EXECUTOR_BACKUP'
    }

    parameters {
        string(
            name: 'BUILD_BRANCH',
            defaultValue: 'develop',
            description: 'Branch to build: develop, main, or a ws/... or b3/... branch before merging it (the multibranch job ignores this: it builds its own branch)'
        )
        choice(
            name: 'TEST_TIER',
            choices: ['auto', 'fast', 'slow', 'full', 'smoke'],
            description: 'auto: full+smoke for develop commits and main, full+smoke+coverage at night, fast for other branches. smoke = full + the desktop smoke test'
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
        pollSCM(env.BRANCH_NAME ? '' : 'H/15 * * * *')
        cron(env.BRANCH_NAME ? '' : 'H 3 * * *')   // T-01: the nightly full tier of develop
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    // the multibranch job builds its own branch; the DEV job the BUILD_BRANCH parameter
                    env.BRANCH = env.BRANCH_NAME ?: params.BUILD_BRANCH
                    if (env.BRANCH_NAME) {
                        milestone(1)   // a newer push to this branch aborts this older build
                        checkout([
                            $class: 'GitSCM',
                            branches: scm.branches,
                            extensions: scm.extensions + [[$class: 'CleanBeforeCheckout']],
                            userRemoteConfigs: scm.userRemoteConfigs
                        ])
                    } else {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${params.BUILD_BRANCH}"]],
                            extensions: [[$class: 'CleanBeforeCheckout']],
                            userRemoteConfigs: [[
                                credentialsId: 'GITHUB_GEOMANCER86',
                                url: 'https://github.com/Geomancer86/Futtoboru.git'
                            ]]
                        ])
                    }
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
                        } else if (env.BRANCH == 'develop' || env.BRANCH == 'main') {
                            tier = 'smoke'
                        } else {
                            tier = 'fast'
                        }
                    }
                    env.TIER = tier
                    // T-02: coverage (JaCoCo) only in the long Jenkins runs, never in the fast tier
                    env.MVN_PROFILES = [fast: '', slow: '-Pslow,coverage', full: '-Pfull,coverage', smoke: '-Pfull,smoke',
                                        nightly: '-Pfull,smoke,coverage'][tier]
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.BRANCH} @ ${env.GIT_COMMIT_SHORT} ${tier}"
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

        // D33 G4: a green develop build is "always playable": the fat jar as playable-latest, the tag green/develop moved
        stage('Publish green develop') {
            when {
                allOf {
                    expression { env.BRANCH == 'develop' && !env.BRANCH_NAME }
                    expression { env.TIER in ['smoke', 'nightly'] }
                }
            }
            steps {
                sh '''
                    mkdir -p playable-latest
                    cp futtoboru-desktop/target/*-jar-with-dependencies.jar playable-latest/futtoboru-playable-latest.jar
                    echo "develop @ $GIT_COMMIT_FULL, Jenkins build $BUILD_NUMBER" > playable-latest/BUILD.txt
                '''
                archiveArtifacts artifacts: 'playable-latest/*', fingerprint: true
                // the tag is lightweight and moves with each green develop build; an older build that finishes late never moves it back
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE', message: 'could not move green/develop (credential rights?)') {
                    withCredentials([usernamePassword(credentialsId: 'GITHUB_GEOMANCER86', usernameVariable: 'GH_USER', passwordVariable: 'GH_PASS')]) {
                        sh '''
                            set +x
                            HELPER='!f() { echo username=$GH_USER; echo password=$GH_PASS; }; f'
                            git -c credential.helper="$HELPER" fetch -q origin "+refs/tags/green/develop:refs/tags/green/develop" || true
                            if git rev-parse -q --verify refs/tags/green/develop >/dev/null \
                               && git merge-base --is-ancestor HEAD refs/tags/green/develop; then
                                echo "green/develop is already at or past this commit"
                                exit 0
                            fi
                            git tag -f green/develop HEAD
                            git -c credential.helper="$HELPER" push -q origin "+refs/tags/green/develop:refs/tags/green/develop"
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Futtoboru build ${env.BUILD_NUMBER}: branch ${env.BRANCH} @ ${env.GIT_COMMIT_SHORT ?: 'N/A'} tier ${env.TIER ?: '?'} -> ${currentBuild.currentResult}"
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
