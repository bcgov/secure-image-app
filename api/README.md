
## About

This is the API component to the Secure Image application suite. Secure Image is designed to provide a simple yet secure way for people to take sensitive photographs (or image related documentation) and store it outside of the device's camera roll. It also provides a convenient way to extract the images by providing a secure URL to download an album.

## Build

This component builds using GitHub Actions in combination with the OpenShift Container Platform (OCP). The GitHub Actions workflow will build and test the project against various node versions and, once successful, if the changes are on the `master` branch trigger an OCP image build using a service account with highly restricted access.

There are two steps for setting up the CICD pipeline:
1. Setup a OCP service account with restricted access and add the credentials to GitHub;
2. Setup the OCP image build.

### Step 1: CICD Service Account

To setup the CICD pipeline GitHub needs to be able to trigger an image build in OCP. We do this by setting up a service account that only has access to trigger and image build.

In the root directory of the repo there is an directory containing common OpenShift templates. Use the following command to create a CICD service account for GitHub.

```console
oc process -f ../openshift/templates/cicd.yaml \
-p NAMESPACE=$(oc project --short) | \
oc apply -f -  
```

| Parameter          | Optional      | Description   |
| ------------------ | ------------- | ------------- |
| NAMESPACE            | NO            | The namespace used for all components of the credentials. 


You'll see some output from the above command similar to this:

```console
serviceaccount/github-cicd created
role.authorization.openshift.io/github-cicd created
rolebinding.authorization.openshift.io/github-cicd created
```

You can list your secrets with the following command:

```console
oc get secrets
```

Again, you'll see output similar to this:

```console
NAME                          TYPE                                  DATA   AGE
github-cicd-dockercfg-gsd7b   kubernetes.io/dockercfg               1      85s
github-cicd-token-9jnw6       kubernetes.io/service-account-token   4      85s
github-cicd-token-gr9pf       kubernetes.io/service-account-token   4      85s
```

Pick either of the secrets beginning with `github-cicd-token` and get the `token` field from it. You can do this via the Web UI or with the following command providing you have `jq` installed:

```
oc get secret/github-cicd-token-9jnw6 -o json | \
jq '.data.token' | \
tr -d "\""
```

Pro Tip 🤓: If you have a mac, add `| pbcopy` after the above command to copy the token directly to your clipboard. 

Once you have your token go to the Settings of your GitHub repo and create the following [encrypted secrets](https://docs.github.com/en/actions/configuring-and-managing-workflows/creating-and-storing-encrypted-secrets):

| Name               | Optional | Value   |
| :----------------- | :------- | :------------ |
| OPENSHIFTTOKEN     | NO       | The token from from the command above.|
| OPENSHIFTSERVERURL | NO       | The URL for the OpenShift console (everything up to the port number).|

These secrets are used by the official RedHat GitHub actions to trigger the image build. You can see them in the workflow file [here](../.github/workflows/api.yml) and in the excerpt below:

```yaml
   steps:
      - name: S2I Build
        uses: redhat-developer/openshift-actions@v1.1
        with:
          version: "latest"
          openshift_server_url: ${{ secrets.OpenShiftServerURL}}
          parameters: '{"apitoken": "${{ secrets.OpenShiftToken }}", "acceptUntrustedCerts": "true"}'
          cmd: |
            'version'
            'start-build secure-image-api-master-build --follow -n devex-mpf-secure-tools'
```

### Step 2: OCP Image Build

The second and final step is to setup the image build on the OCP platform. The [workflow](../.github/workflows/api.yml) is setup to only run this step when changes impact the `master` branch.

Use the following command to setup the image `BuildConfig` in your tools namespace:

```console
oc process -f openshift/templates/build.yaml | oc apply -f -
```

You can trigger this build manually to check that it works as expected:

```console
oc start-build bc/secure-image-api-master-build --follow
```

## Deployment

This component (and sub components) deploy throuh applying an OpenShift template and subsaquently through image tags. The deployment manifest will deploy the API (from the build process) as well as an instance of [minio](https://min.io/).

There are three steps to deploy the API component:
1. Create the secrets necessary for access control;
2. Create a `ConfigMap` where frequently changed application config is stored;
3. Deploy the application (and sub components).

### Step 1: Secrets

The secrets are separated form the main deployment manifest so that `oc apply` can be used without regenerating the secrets. Run the following command to create the necessary OCP secrets:

```console
oc process -f secret.yaml -p SSO_SHARED_SECRET=<THE_SECRET> | \
oc create -f -
```

| Name               | Optional | Value   |
| :----------------- | :------- | :------------ |
| SSO_SHARED_SECRET  | NO       | The credentials copied from the SSO realm client (via the Web UI). |

### Step 2: The ConfigMap

Next, create a config map with application configuration. Adjust the information in the YAML file as needed for each environment.

```console
oc process -f config.yaml | \
oc apply -f -
```

The following table describes the settings in the config file:

| Name               | Description  |
| :----------------- | :----------- |
| temporaryUploadPath| Directory on local storage where uploaded media is temporarily stored.|
| archiveFileBaseName| A prefix given to archived images in the downloadable file. |
| templates | The path to the HTML served by the application |
| albumExpirationInDays | The duration in days that an album can be downloaded |
| minio | Minio server config. See minio documentation for details.
| session | HTTP session configuration |
| sso | SSO server config. |


### Step 3: Deployment

There are several deployment parameters embedded in the manifest with defaults that do not need to be specified. Some however, must be supplied at deployment time. They are shown in the command below as well as outlined in [deploy.yaml](./openshift/templates/deploy.yaml) manifest and table below. Use the following command to trigger a deployment; all components will deploy immediately providing the images are available.


```console
oc process -f openshift/templates/deploy.yaml \
-p SOURCE_IMAGE_NAMESPACE=<NAMESPACE_HERE> \
-p SOURCE_IMAGE_TAG=<dev | test | prod> \
-p NAMESPACE=$(oc project --short) \
-p MINIO_VOLUME_CAPACITY=<SIZE> | \
oc apply -f -
```

You'll see all the components get created and the deployment will begin providing a image with the appropriate tag exists:

```console
route.route.openshift.io/secure-image-api configured
persistentvolumeclaim/minio-data configured
service/minio configured
service/secure-image-api configured
deploymentconfig.apps.openshift.io/minio configured
deploymentconfig.apps.openshift.io/secure-image-api configured
```

| Name               | Optional | Value   |
| :----------------- | :------- | :------------ |
| SOURCE_IMAGE_NAMESPACE  | NO | Typically your tools namespace where the image will be pulled from |
| SOURCE_IMAGE_TAG  | NO | The tag OCP monitor for images changes to trigger a deployment. This typically matches the environment postfix (dev, test, prod) |
| NAMESPACE  | NO | The namespace currently being deployed to |
| MINIO_VOLUME_CAPACITY  | NO | The size of the minio volume. For example (1G, 10G, 32G, etc) |


## Local Installation for Development

There are two steps to running this project locally for development:

1. Minio

Run a local minio docker image (find them [here](https://hub.docker.com/r/minio/minio/)). The sample command below is using a docker volume named `minio_data` to store data; see the Docker documentation on how to do this if you're interested. When minio starts it will print the `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` needed for step two.

```console
docker run -p 9000:9000 --name minio -v minio_data:/data minio/minio server /data
```

2. API

Create a file called `.env` in the root project folder and populate it with the following environment variables; update them as needed.

```console
NODE_ENV=development
MINIO_ACCESS_KEY="XXXXXXXX"
MINIO_SECRET_KEY="YYYYYYYYYYYYYYYY"
MINIO_ENDPOINT="localhost"
SSO_CLIENT_SECRET="00000000-aaaa-aaaa-aaaa-000000000000"
SESSION_SECRET="abc123"
APP_URL="http://localhost:8000"
```

Run the node application with the following command:

```console
npm run dev
```

## Project Status / Goals / Roadmap

This project is completed. 

Progress to date, known issues, or new features will be documented on our publicly available Trello board [here](https://trello.com/b/UYJpEzrT/secure-image-app).

## Getting Help or Reporting an Issue

Send a note to bcdevexchange@gov.bc.ca and you'll get routed to the right person to help you out.


## How to Contribute

*If you are including a Code of Conduct, make sure that you have a [CODE_OF_CONDUCT.md](SAMPLE-CODE_OF_CONDUCT.md) file, and include the following text in here in the README:*
"Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms."

## License

Detailed guidance around licenses is available 
[here](/BC-Open-Source-Development-Employee-Guide/Licenses.md)

Attach the appropriate LICENSE file directly into your repository before you do anything else!

The default license For code repositories is: Apache 2.0

Here is the boiler-plate you should put into the comments header of every source code file as well as the bottom of your README.md:

    Copyright 2015 Province of British Columbia

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at 

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
   
For repos that are made up of docs, wikis and non-code stuff it's Creative Commons Attribution 4.0 International, and should look like this at the bottom of your README.md:

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons Licence" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/80x15.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">YOUR REPO NAME HERE</span> by <span xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">the Province of Britich Columbia</span> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.

and the code for the cc 4.0 footer looks like this:

    <a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons Licence"
    style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/80x15.png" /></a><br /><span
    xmlns:dct="http://purl.org/dc/terms/" property="dct:title">YOUR REPO NAME HERE</span> by <span
    xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">the Province of Britich Columbia
    </span> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">
    Creative Commons Attribution 4.0 International License</a>.
