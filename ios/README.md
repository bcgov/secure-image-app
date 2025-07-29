# TL;DR

This sub-directory contains the iOS app that is one of the three components to the application suite. The following document will illustrate key configuration for iOS development and release.

# Release builds

1. Make sure certificates and profiles are valid for distribution builds.

    * Update provisioning profile name in:

        * [exportOptions-adhoc.plist](exportOptions-adhoc.plist)
        * [SecureImage Signing & Capabilites section](SecureImage.xcodeproj/project.pbxproj) (via XCode)

    * Update version and build numbers in:
    
        * [SecureImage Signing & Capabilites section](SecureImage.xcodeproj/project.pbxproj) (via XCode)

2. Run the [Archive and Export ipa](../.github/workflows/ios.yml) action on the desired branch (usually master, after all changes have been reviewed and merged).

3. If successful, IPA file will be uploaded to artifactory with a build number matching the action run number.

# Local Development

This document assumes the reader is using a macOS device and has downloaded `xcode` from the macOS App store. It also assumes the reader is familiar with iOS development.

This project uses [CocoaPods](https://cocoapods.org/) for package management. Run `pod install` to install the necessary dependencies. In addition to this, make the following config changes for local development or production releases:

Open the file `Constants.swift` in the xcode IDE and edit the SSO `baseUrl` to match whatever is being used by the API. For local development will probably be `sso-dev` and for production `sso`.

```swift
struct SSO {
    static let baseUrl = URL(string: "https://sso-dev.pathfinder.gov.bc.ca")!
    static let redirectUri = "secure-image://client"
    static let clientId = "secure-image"
    static let realmName = "secimg"
    static let idpHint = "idir"
}
```

In the same file, edit the API `serverURL` to match whatever is being used by the API. The iOS app will use this URL to access the API.

```swift
struct API {
    static let serverURL = URL(string: "https://f9d9fbb72386.ngrok.io/v1/")
    static let createAlbumPath = "album/"
    static let addPhotoToAlbumPath = "album/:id"
    static let getAlbumDownloadUrlPath = "album/:id"
    static let addFieldNotesToAlbumPath = "album/:id/note"
}
```

At this point you will be able to run the app on an iOS device for development. The iOS application will use the `serverURL` to communicate with the API.

Pro Tip 🤓: 
* Make sure you set these values correctly prior to a production build and release; don't ship dev config.
* Mobile applications need to be signed with our corporate certificates prior to release. Search for `signing` at the [DevHub](https://developer.gov.bc.ca/) for more information on the process.

## Azure Pipeline - Experimental

All iOS development must be done on macOS devices; This was not available through GitHub Actions at the time of this project. However, Microsoft's Azure DevOps did offer build time on macOS devices for free.

This project has a configuration file for Azure Pipelines (CICD). This is an experimental feature.

 Teams can setup their own Azure DevOps organization and add this repository to Pipelines. Azure DevOps pipelines will use the included [azure-pipelines.yml](./azure-pipelines.yml). Contact Platform Services for additional support once you have created an organization.
