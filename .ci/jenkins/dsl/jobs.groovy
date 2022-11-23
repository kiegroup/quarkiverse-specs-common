/*
* This file is describing all the Jenkins jobs in the DSL format (see https://plugins.jenkins.io/job-dsl/)
* needed by the Kogito pipelines.
*
* The main part of Jenkins job generation is defined into the https://github.com/kiegroup/kogito-pipelines repository.
*
* This file is making use of shared libraries defined in
* https://github.com/kiegroup/kogito-pipelines/tree/main/dsl/seed/src/main/groovy/org/kie/jenkins/jobdsl.
*/

import org.kie.jenkins.jobdsl.model.Folder
import org.kie.jenkins.jobdsl.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoJobUtils
import org.kie.jenkins.jobdsl.Utils

jenkins_path = '.ci/jenkins'

///////////////////////////////////////////////////////////////////////////////////////////
// Global Quarkiverse Specs Common project jobs
///////////////////////////////////////////////////////////////////////////////////////////

jenkins_path_project = "${jenkins_path}/project"

// Init branch
createProjectSetupBranchJob()

// Nightly jobs
setupProjectNightlyJob()

// Release jobs
setupProjectReleaseJob()

// Tools
KogitoJobUtils.createMainQuarkusUpdateToolsJob(this,
        [ 'quarkiverse-specs-common' ],
        [ 'fjtirado' ]
)

void createProjectSetupBranchJob() {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, '0-setup-branch', Folder.SETUP_BRANCH, "${jenkins_path_project}/Jenkinsfile.setup-branch", 'Quarkiverse Specs Common Project Setup Branch')
    jobParams.env.putAll([
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_BRANCH_NAME: "${GIT_BRANCH}",
        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",

        IS_MAIN_BRANCH: "${Utils.isMainBranch(this)}"
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('QUARKIVERSE_SPECS_COMMON_VERSION', '', 'Quarkiverse Specs Common version')
        }
    }
}

void setupProjectNightlyJob() {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'quarkiverse-specs-common-nightly', Folder.NIGHTLY, "${jenkins_path_project}/Jenkinsfile.nightly", 'Quarkiverse Specs Common Nightly')
    jobParams.triggers = [cron : '@midnight']
    jobParams.env.putAll([
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_BRANCH_NAME: "${GIT_BRANCH}",
        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",

        MAVEN_SETTINGS_CONFIG_FILE_ID: "${MAVEN_SETTINGS_FILE_ID}",
        ARTIFACTS_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            booleanParam('SKIP_TESTS', false, 'Skip all tests')
        }
    }
}

void setupProjectReleaseJob() {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'quarkiverse-specs-common-release', Folder.RELEASE, "${jenkins_path_project}/Jenkinsfile.release", 'Quarkiverse Specs Common Release')
    jobParams.env.putAll([
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_BRANCH_NAME: "${GIT_BRANCH}",
        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",

        DEFAULT_STAGING_REPOSITORY: "${MAVEN_NEXUS_STAGING_PROFILE_URL}",
        ARTIFACTS_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('RESTORE_FROM_PREVIOUS_JOB', '', 'URL to a previous stopped release job which needs to be continued')

            stringParam('QUARKIVERSE_SPECS_COMMON_VERSION', '', 'Project version of Quarkiverse Specs Common to release as Major.minor.micro')
            stringParam('QUARKIVERSE_SPECS_COMMON_RELEASE_BRANCH', '', '(optional) Use to override the release branch name deduced from the QUARKIVERSE_SPECS_COMMON_VERSION')

            booleanParam('SKIP_TESTS', false, 'Skip all tests')
        }
    }
}

///////////////////////////////////////////////////////////////////////////////////////////
// Quarkiverse Common Specs repository only jobs
///////////////////////////////////////////////////////////////////////////////////////////

Map getMultijobPRConfig(Folder jobFolder) {
    def jobConfig = [
        parallel: true,
        buildchain: true,
        jobs : [
            [
                id: 'quarkiverse-specs-common',
                primary: true,
                env : [
                    DISABLE_SONARCLOUD: true,
                ]
            ]
        ]
    ]
    return jobConfig
}

// PR checks
KogitoJobUtils.createAllEnvsPerRepoPRJobs(this) { jobFolder -> getMultijobPRConfig(jobFolder) }

// Init branch
createSetupBranchJob()

// Nightly jobs
setupDeployJob(Folder.NIGHTLY)

// Release jobs
setupDeployJob(Folder.RELEASE)
setupPromoteJob(Folder.RELEASE)

KogitoJobUtils.createQuarkusUpdateToolsJob(this, 'quarkiverse-specs-common', [
  compare_deps_remote_poms: [ 'io.quarkus:quarkus-bom' ],
  properties: [ 'quarkus.version' ],
])

/////////////////////////////////////////////////////////////////
// Methods
/////////////////////////////////////////////////////////////////

void createSetupBranchJob() {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'quarkiverse-specs-common', Folder.SETUP_BRANCH, "${jenkins_path}/Jenkinsfile.setup-branch", 'Quarkiverse Specs Common Init branch')
    KogitoJobUtils.setupJobParamsDefaultMavenConfiguration(this, jobParams)
    jobParams.env.putAll([
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",
        AUTHOR_CREDS_ID: "${GIT_AUTHOR_CREDENTIALS_ID}",

        MAVEN_SETTINGS_CONFIG_FILE_ID: "${MAVEN_SETTINGS_FILE_ID}",
        MAVEN_DEPENDENCIES_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
        MAVEN_DEPLOY_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",

        IS_MAIN_BRANCH: "${Utils.isMainBranch(this)}"
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')

            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')

            stringParam('QUARKIVERSE_SPECS_COMMON_VERSION', '', 'Quarkiverse Specs Common version to set.')

            booleanParam('SEND_NOTIFICATION', false, 'In case you want the pipeline to send a notification on CI channel for this run.')
        }
    }
}

void setupDeployJob(Folder jobFolder) {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'quarkiverse-specs-common-deploy', jobFolder, "${jenkins_path}/Jenkinsfile.deploy", 'Quarkiverse Specs Common Deploy')
    KogitoJobUtils.setupJobParamsDefaultMavenConfiguration(this, jobParams)
    jobParams.env.putAll([
        PROPERTIES_FILE_NAME: 'deployment.properties',
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",
        AUTHOR_CREDS_ID: "${GIT_AUTHOR_CREDENTIALS_ID}",
        GITHUB_TOKEN_CREDS_ID: "${GIT_AUTHOR_TOKEN_CREDENTIALS_ID}",

        MAVEN_SETTINGS_CONFIG_FILE_ID: "${MAVEN_SETTINGS_FILE_ID}",
        MAVEN_DEPENDENCIES_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
        MAVEN_DEPLOY_REPOSITORY: "${MAVEN_ARTIFACTS_UPLOAD_REPOSITORY_URL}",
        MAVEN_REPO_CREDS_ID: "${MAVEN_ARTIFACTS_UPLOAD_REPOSITORY_CREDS_ID}",
    ])
    if (jobFolder.isRelease()) {
        jobParams.env.putAll([
            NEXUS_RELEASE_URL: "${MAVEN_NEXUS_RELEASE_URL}",
            NEXUS_RELEASE_REPOSITORY_ID: "${MAVEN_NEXUS_RELEASE_REPOSITORY}",
            NEXUS_STAGING_PROFILE_ID: "${MAVEN_NEXUS_STAGING_PROFILE_ID}",
            NEXUS_BUILD_PROMOTION_PROFILE_ID: "${MAVEN_NEXUS_BUILD_PROMOTION_PROFILE_ID}",
        ])
    }
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')

            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')

            booleanParam('SKIP_TESTS', false, 'Skip tests')

            stringParam('QUARKIVERSE_SPECS_COMMON_VERSION', '', 'Optional if not RELEASE. If RELEASE, cannot be empty.')

            booleanParam('SEND_NOTIFICATION', false, 'In case you want the pipeline to send a notification on CI channel for this run.')
        }
    }
}

void setupPromoteJob(Folder jobFolder) {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'quarkiverse-specs-common-promote', jobFolder, "${jenkins_path}/Jenkinsfile.promote", 'Quarkiverse Specs Common Promote')
    KogitoJobUtils.setupJobParamsDefaultMavenConfiguration(this, jobParams)
    jobParams.env.putAll([
        PROPERTIES_FILE_NAME: 'deployment.properties',
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",
        AUTHOR_CREDS_ID: "${GIT_AUTHOR_CREDENTIALS_ID}",
        GITHUB_TOKEN_CREDS_ID: "${GIT_AUTHOR_TOKEN_CREDENTIALS_ID}",

        MAVEN_SETTINGS_CONFIG_FILE_ID: "${MAVEN_SETTINGS_FILE_ID}",
        MAVEN_DEPENDENCIES_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
        MAVEN_DEPLOY_REPOSITORY: "${MAVEN_ARTIFACTS_UPLOAD_REPOSITORY_URL}",
        MAVEN_REPO_CREDS_ID: "${MAVEN_ARTIFACTS_UPLOAD_REPOSITORY_CREDS_ID}",
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')
            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')
            // Deploy job url to retrieve deployment.properties
            stringParam('DEPLOY_BUILD_URL', '', 'URL to jenkins deploy build to retrieve the `deployment.properties` file. If base parameters are defined, they will override the `deployment.properties` information')
            // Release information which can override `deployment.properties`
            stringParam('QUARKIVERSE_SPECS_COMMON_VERSION', '', 'Override `deployment.properties`. Optional if not RELEASE. If RELEASE, cannot be empty.')
            stringParam('GIT_TAG', '', 'Git tag to set, if different from QUARKIVERSE_SPECS_COMMON_VERSION')
            booleanParam('SEND_NOTIFICATION', false, 'In case you want the pipeline to send a notification on CI channel for this run.')
        }
    }
}
