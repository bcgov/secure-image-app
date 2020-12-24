# TL;DR

This sub-directory contains the Android app that is one of the three components to the application suite. The following document will illustrate key configuration for development and release.

# Local Development

This document assumes the reader is using a device with at least OpenJDK 1.8 and Android Studio installed. This project uses [Gradle](https://gradle.org) for package management. 

Make the following config changes for local development or production releases:

Open the file `keystore.properties` and edit the values as needed. For example, if you are running the API locally update `DEBUG_SERVER_URL` to point to your local instance as per the API docs there.

Content of `keystore.properties` for production release is
```
DEBUG_SERVER_URL = \"https://api.secure-image.mcf.gov.bc.ca/\"
SSO_BASE_URL = \"https://oidc.gov.bc.ca/\"
SSO_REALM_NAME = \"secimg\"
SSO_AUTH_ENDPOINT = \"https://oidc.gov.bc.ca/auth/realms/secimg/protocol/openid-connect/auth\"
SSO_REDIRECT_URI = \"secure-image://client\"
SSO_CLIENT_ID = \"secure-image\"
SSO_HINT = \"idir\"
```


At this point you will be able to run the app on an Android device for development. The Android application will use the `DEBUG_SERVER_URL` to communicate with the API.

Pro Tip 🤓: 
* Make sure you set these values correctly prior to a production build and release; don't ship dev config.
* Mobile applications need to be signed with our corporate certificates prior to release. Search for `signing` at the [DevHub](https://developer.gov.bc.ca/) for more information on the process.

## CICD Pipeline

This project is configured to run tests automatically via a GitHub Actions [workflow](../.github/workflows/android.yml). Make changes as needed 
