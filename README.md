This repo consolidates thee other repos: secure-image-api, secure-image-ios, and secure-image-android. Those repos are archived and will soon be deleted. This README will be updated shortly.


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