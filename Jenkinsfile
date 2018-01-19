def APP_NAME = 'secure-image-api'
def TAG_NAMES = ['dev', 'test']
def BUILD_CONFIG = APP_NAME
def CMD_PREFIX = 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin'
//def IMAGESTREAM_NAME = APP_NAME

node {

  stage('Checkout') {
    echo "checking out source"
    checkout scm
  }

  stage('Build') {
    echo "Build: ${BUILD_ID}"

    // The version of node in the `node` that comes with OpenShift is too old
    // so I use a generic Linux and install my own node from LTS.
    // sh 'curl https://nodejs.org/dist/v8.9.4/node-v8.9.4-linux-x64.tar.xz | tar -Jx'
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin npm -v'
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin node -v'

    // // setup the node dev environment
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin npm install --only=dev'
    // // not sure if this needs to be added to package.json.
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin npm install escape-string-regexp'

    // // run the build specified in pacage.json
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin npm run build'
    // sh 'PATH=$PATH:$PWD/node-v8.9.4-linux-x64/bin npm run test'

    // run the oc build to package the artifacts into a docker image
    openshiftBuild bldCfg: 'secure-image-api', showBuildLogs: 'true', verbose: 'true'

    // Don't tag with BUILD_ID so the pruner can do it's job; it won't delete tagged images.
    // Tag the images for deployment based on the image's hash
    IMAGE_HASH = sh (
      script: """oc get istag ${IMAGESTREAM_NAME}:latest -o template --template=\"{{.image.dockerImageReference}}\"|awk -F \":\" \'{print \$3}\'""",
      returnStdout: true).trim()
    echo ">> IMAGE_HASH: ${IMAGE_HASH}"
  }

  // stage('deploy-' + TAG_NAMES[0]) {
  //   openshiftTag destStream: IMAGESTREAM_NAME, verbose: 'true', destTag: TAG_NAMES[0], srcStream: IMAGESTREAM_NAME, srcTag: "${IMAGE_HASH}"
  // }
}