
// The provisining profile must be located in `~/Library/MobileDevice/Provisioning Profiles`
// if it does not exist `mkdir -p ~/Library/MobileDevice/Provisioning Profiles` and copy
// your profile in place. If you don't have it, grab it from your Apple Developer account.
// PROVISIONING_PROFILE_SPECIFIER needs to match the `name` key of the provisioning profile
// not the UUID or anything else.

def CONFIGURATION = ""
def SDK = ""
def PROJECT_NAME = "SecureImage"
def PROVISIONING_PROFILE_NAME = "Mobile Pathfinder In House"
def BUILD_DIR = "./build"
def TIMESTAMP = (new Date()).format("yyyyMMdd-HHmm", TimeZone.getTimeZone('UTC'))
def GIT_BRANCH_NAME = ("${env.JOB_BASE_NAME}".contains("master")) ? "master" : "develop"

// A release will only come from the `master` branch. For all others
// a Debug configuration is done.
if( "master" == GIT_BRANCH_NAME.toLowerCase() ) {
  CONFIGURATION = "Release"
  SDK="iphoneos"
} else {
  CONFIGURATION = "Debug"
  SDK="iphonesimulator"
}

node('xcode') {

  // Extract any sensative data from secrets
  // ANDROID_DECRYPT_KEY = sh (
  //   script: """oc get secret/android-decrypt-key -o template --template="{{.data.decryptKey}}" | base64 --decode""",
  //   returnStdout: true).trim()
  // BDD_DEVICE_FARM_USER = sh (
  //   script: """oc get secret/bdd-credentials -o template --template="{{.data.username}}" | base64 --decode""",
  //   returnStdout: true).trim()
  // BDD_DEVICE_FARM_PASSWD = sh (
  //   script: """oc get secret/bdd-credentials -o template --template="{{.data.password}}" | base64 --decode""",
  //   returnStdout: true).trim()

  // def UPLOAD_URL = "curl -u ${BDD_DEVICE_FARM_USER}:${BDD_DEVICE_FARM_PASSWD} -X POST https://api.browserstack.com/app-automate/upload -F file=@${APP_PATH}"


  stage('Checkout') {
    echo "Checking out source"
    checkout scm

    GIT_COMMIT_SHORT_HASH = sh (
      script: """git describe --always""",
      returnStdout: true).trim()
    GIT_COMMIT_AUTHOR = sh (
      script: """git show -s --pretty=%an""",
      returnStdout: true).trim()
  }

  stage('Setup') {
    echo "Build setup"

    // If Cocoapods is not setup yet, initalize the environment. This will clone
    // the master repo; it will take a while to do your first build.
    sh """if [ ! -d "${env.HOME}/.cocoapods" ]; then LANG=en_US.UTF-8 pod setup; fi"""

    // Decrypt the Android keystore properties file.
    // sh "/usr/bin/openssl aes-256-cbc -d -a -in keystore.properties.enc -out keystore.properties -pass pass:${ANDROID_DECRYPT_KEY}"
    // sh "/usr/bin/openssl aes-256-cbc -d -a -in app/fabric.properties.enc -out app/fabric.properties -pass pass:${ANDROID_DECRYPT_KEY}"
  }
  
  stage('Build') {
    echo "Build: ${BUILD_ID}"

    // Do we just want a build, or should we produce an archive?
    def command = "debug".equalsIgnoreCase(CONFIGURATION) ? 'build' : 'archive'

    // `archivePath` is only used if `archive` is specified; no impact when
    // build is used.
    sh """
      export PATH=\$PATH:/usr/local/bin && \
      LANG=en_US.UTF-8 pod install && \
      xcodebuild \
        -workspace ${PROJECT_NAME}.xcworkspace \
        -scheme ${PROJECT_NAME} \
        -configuration ${CONFIGURATION} \
        -xcconfig ${CONFIGURATION.toLowerCase()}-ci.xcconfig \
        -derivedDataPath ${BUILD_DIR} \
        -archivePath "${PROJECT_NAME}-${TIMESTAMP}" \
        clean ${command}
    """

    // A Production release will only come from the master branch. For 'dev' builds
    // we can safley exit here.
    if("debug".equalsIgnoreCase(CONFIGURATION)) {
      sh "exit 0"
    }



    // DEBUG_APK_PATH = sh (
    //   script: """find . -iname 'app-debug.apk'""",
    //   returnStdout: true).trim()
    // echo "Found DEBUG APK in ${DEBUG_APK_PATH}"
    // RELEASE_APK_PATH = sh (
    //   script: """find . -iname 'app-release.apk'""",
    //   returnStdout: true).trim()
    // echo "Found RELEASE APK in ${RELEASE_APK_PATH}"
  }

  stage('Test') {

    echo "Test ${BUILD_ID}"
    // dir('bdd/geb-mobile') {
    //   echo "Upload the sample app to cloud server"
    //   // // App hash (bs/md5), could be used to reference in test task. Using the app package name for now.
    //   // def APP_HASH = sh (
    //   //   script: "$UPLOAD_URL",
    //   //   returnStdout: true).trim()

    //   // echo "the has is: ${APP_HASH}"

    //   // // Abort the build if not uploaded successfully:
    //   // if (APP_HASH.contains("Warning")) {
    //   //     currentBuild.result = 'ABORTED'
    //   //     error('Error uploading app to account storage')
    //   // }
    //   // echo "Successfully uploaded the app..."
      
    //   echo "Start functional testing with mobile-BDDStack, running sample test case"
    //   try {
    //     sh './gradlew --debug --stacktrace androidOnBrowserStack'
    //   } finally { 
    //     // stash includes: 'geb-mobile-test-spock/build/reports/**/*', name: 'reports'


    //     archiveArtifacts allowEmptyArchive: true, artifacts: 'geb-mobile-test-spock/build/reports/**/*'
    //     archiveArtifacts allowEmptyArchive: true, artifacts: 'geb-mobile-test-spock/build/test-results/**/*'
    //     archiveArtifacts allowEmptyArchive: true, artifacts: 'geb-mobile-test-spock/screenshots/*'
    //     junit 'geb-mobile-test-spock/build/test-results/**/*.xml'
    //   }
    }
}

