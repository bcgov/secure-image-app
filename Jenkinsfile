// See https://github.com/jenkinsci/kubernetes-plugin

// The JVM / Kotlin Daemon will quite often fail in memory constraind environments. Givng the JVM
// 4g allows for the maxumum reqested / recommended by gradle. This is passed in the pod spec as a
// an environment variable.
def APP_PATH = "demo_apps/WikipediaSample.apk"
def APP_NAME = "SampleAPP.apk"


node('xcode') {

  // Extract any sensative data from secrets
  ANDROID_DECRYPT_KEY = sh (
    script: """oc get secret/android-decrypt-key -o template --template="{{.data.decryptKey}}" | base64 --decode""",
    returnStdout: true).trim()
  BDD_DEVICE_FARM_USER = sh (
    script: """oc get secret/bdd-credentials -o template --template="{{.data.username}}" | base64 --decode""",
    returnStdout: true).trim()
  BDD_DEVICE_FARM_PASSWD = sh (
    script: """oc get secret/bdd-credentials -o template --template="{{.data.password}}" | base64 --decode""",
    returnStdout: true).trim()

  def UPLOAD_URL = "curl -u ${BDD_DEVICE_FARM_USER}:${BDD_DEVICE_FARM_PASSWD} -X POST https://api.browserstack.com/app-automate/upload -F file=@${APP_PATH}"


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
    // Decrypt the Android keystore properties file.
    // sh "/usr/bin/openssl aes-256-cbc -d -a -in keystore.properties.enc -out keystore.properties -pass pass:${ANDROID_DECRYPT_KEY}"
    // sh "/usr/bin/openssl aes-256-cbc -d -a -in app/fabric.properties.enc -out app/fabric.properties -pass pass:${ANDROID_DECRYPT_KEY}"
  }
  
  stage('Build') {
    echo "Build: ${BUILD_ID}"

    xcodebuild -workspace SecureImage.xcworkspace -scheme SecureImage -configuration DEBUG clean build
    // sh """
    //   JAVA_HOME=\$(dirname \$( readlink -f \$(which java) )) && \
    //   JAVA_HOME=\$(realpath "$JAVA_HOME"/../) && \
    //   export JAVA_HOME && \
    //   export ANDROID_HOME=/opt/android && \
    //   ./gradlew build -x test -x lint
    //   """

    // echo "Assemble build"
    // sh "./gradlew assembleDebug"

    // // Find API for informational purposes.
    // sh "find . -iname '*.apk'"

    // // Packages are typically located in the following locations.
    // // ./app/build/outputs/apk/debug/app-debug.apk
    // // ./app/build/outputs/apk/release/app-release-unsigned.apk
    // // ./app/release/app-release.apk

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
}

