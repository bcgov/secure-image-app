#!/bin/bash

set -eo pipefail

xcodebuild clean -exportArchive -archivePath $PWD/build/SecureImage.xcarchive -exportPath $PWD/build -allowProvisioningUpdates -exportOptionsPlist ios/exportOptions-adhoc.plist xcpretty