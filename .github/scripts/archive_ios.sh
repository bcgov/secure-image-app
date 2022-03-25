#!/bin/bash

set -eo pipefail

sudo xcodebuild -workspace $PWD/ios/SecureImage.xcworkspace -scheme SecureImage -sdk iphoneos -configuration AppStoreDistribution archive -archivePath $PWD/build/SecureImage.xcarchive clean archive
