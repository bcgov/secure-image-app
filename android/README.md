# Secure Image Android
SecureImage is an open source application developed for taking, storing and uploading images securely to a protected network. Originally developed for use within the Government of British Columbia's Ministry of Social Development and Poverty Reduction and Ministry of Children and Family Development.

Authentication is handled by this reusable library: [mobile-authentication-android](https://github.com/bcgov/mobile-authentication-android)

## Features
- **Restricted App Access** - User is required to login with their local device credentials (pin, pattern, fingerprint, etc...) to view any app data.
- **Encrypted Data** - All data saved locally is AES encrypted.
- **Take Private Images** - Take images with a built in camera that will only be viewable within the secure image app. 
- **Create Album** - Create album with up to 100 images, album name, and any comments.
- **View Saved Albums** - View all saved albums ordered by most recently created.
- **Delete Album** - Delete album locally off device.
- **Image Management** - View all images, delete selected images, see detailed image view, and continuously add to an album over time.
- **Upload Album** - Upload album to protected server and be returned a download url to email. To upload or download the user is first required to authenticate.

## Requirements
1) Phone needs to have local device protection setup (pin, pattern, fingerprint, etc..) to enter the app.
2) User will need a government issued IDIR account to upload data to the network.
3) Device running Android 6 Marshmallow or greater (API 23+).

## Installation
App can be installed using AirWatch

## Project Status
Pre beta launch

## Goals/Roadmap
Everything being worked on can be found on the public [Secure Image App Trello board](https://trello.com/b/UYJpEzrT/secure-image-app)

## Getting Help or Reporting an Issue
To report bugs/issues/feature requests, please file an [issue.](https://github.com/bcgov/secure-image-android/issues)

## How to Contribute
If you would like to contribute, please see our [CONTRIBUTING guidelines.](https://github.com/bcgov/secure-image-android/blob/master/CONTRIBUTING.md)

Please note that this project is released with a [Contributor Code of Conduct](https://github.com/bcgov/secure-image-android/blob/master/CODE-OF-CONDUCT.md). By participating in this project you agree to abide by its terms.

## License
    Copyright 2017 Province of British Columbia

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at 

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
