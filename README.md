This repo consolidates thee other repos: secure-image-api, secure-image-ios, and secure-image-android. Those repos are archived and will soon be deleted. This README will be updated shortly.

BUILD

➜  secure-image-app git:(master) ✗ oc process -f openshift/templates/cicd.yaml -p NAMESPACE=$(oc project --short) | oc apply -f -  serviceaccount/github-cicd created
role.authorization.openshift.io/github-cicd created
rolebinding.authorization.openshift.io/github-cicd created

open the secret, `github-cicd-dockercfg-xxxxx`

copy the password

create a secret in the repo names `OPENSHIFTTOKEN`
create a secret in the repo named `OPENSHIFTSERVERURL`

make sure namespace and build name is correct in api.yml

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

TODO: Make sure the route name matches the prod route name when it is deployed.


Deployment

oc create secret docker-registry rh-registry \\n--docker-server=registry.redhat.io \\n--docker-username=$RHNAME \\n--docker-password=RHPWD \\n--docker-email=unused

oc secrets add sa/builder secrets/rh-registry  --for=pull

➜  templates git:(master) ✗ oc process -f config.yaml| oc apply -f -
configmap/secure-image-api-config created


Apply secrets, they are removed from the deployment manifest so the deploument can be run with `oc apply` for updates. If it were not seperated then the secrets would be regenerated each time `oc apply` is used.

```console
oc process -f secret.yaml -p SSO_SHARED_SECRET=xxxxx | oc create -f -
```


➜  api git:(master) ✗ oc process -f openshift/templates/deploy.yaml -p SOURCE_IMAGE_NAMESPACE=devex-mpf-secure-tools -p SOURCE_IMAGE_TAG=dev -p NAMESPACE=$(oc project --short) -p MINIO_VOLUME_CAPACITY=1G | oc apply -f -
route.route.openshift.io/secure-image-api configured
persistentvolumeclaim/minio-data unchanged
service/minio unchanged
service/secure-image-api unchanged
deploymentconfig.apps.openshift.io/minio unchanged
deploymentconfig.apps.openshift.io/secure-image-api configured