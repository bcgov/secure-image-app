
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


## Local Development

Local development for this component is made easy by using the included [docker-compose](./docker-compose.yaml) file. This will start a local server that can be used for development on any of the components.

Create a local directory within the `api` sub-folder that the minio container will use to store data. When an album is uploaded it will appear in this directory.

```console
mkdir minio_data
```

Create a `.env` file in the `api` sub-folder with the entries show below. See `src/config/index.js` and `docker-compose.yaml` to understand how they are consumed by the application:

```
NODE_ENV=development

# Local Docker
MINIO_ACCESS_KEY="xxxx"
MINIO_SECRET_KEY="yyyy"
SSO_CLIENT_SECRET="zzzz"
SESSION_SECRET="aaa"
APP_URL=https://f9d9fbb72386.ngrok.io
```

| Name               | Optional | Value   |
| :----------------- | :------- | :------------ |
| MINIO_ACCESS_KEY  | NO | The minio access key (line username) |
| MINIO_SECRET_KEY  | NO | The minio secret key (like password) |
| SSO_CLIENT_SECRET  | NO | This comes from the client in the SSO Web UI |
| SESSION_SECRET  | NO | Used to secure HTTP sessions |
| APP_URL | NO | The application (API) endpoint |

Pro Tip 🤓: 
 * Use `openssl rand -hex 6` to generate MINIO_ACCESS_KEY, MINIO_SECRET_KEY, and SESSION_SECRET.
 * iOS prefers SSL due to AST. You an use `npx ngrok http 8080` to setup a local proxy that will provide an external URL providing both `http` and `https` protocols.
 * Make sure the URL is registered in your SSO realm as a valid callback URL.

Now, bring up the docker stack / images using `docker-compose`:

```console
docker-compose up
```

At this point your docker stack will be running. See the ProTip above about using `ngrok` as a proxy. The external URLs provided by this application can be given used for `APP_URL` as well as provided to the iOS and Android applications for local development work.
