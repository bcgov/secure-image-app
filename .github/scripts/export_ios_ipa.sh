#!/bin/bash

set -eo pipefail

sudo xcodebuild clean -exportArchive -archivePath $PWD/build/SecureImage.xcarchive -exportPath $PWD/build -allowProvisioningUpdates -exportOptionsPlist ios/exportOptions-adhoc.plist xcpretty