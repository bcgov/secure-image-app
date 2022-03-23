#!/bin/bash

set -eo pipefail

xcodebuild -workspace ios/SecureImage.xcworkspace -scheme SecureImage -sdk iphoneos -configuration AppStoreDistribution archive -archivePath $PWD/build/SecureImage.xcarchive clean archive
