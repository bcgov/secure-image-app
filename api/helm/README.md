To deploy,

1. Create an environment or release specific values yaml file to override _values.yaml_. Name the file values.<env/release>.local.yaml under this folder. \*_.local.yaml_ files are ignored by git. The file looks something like

   ```
    minio:
      accessKey: '******'
      secretKey: '******'
    route:
      host: ******
    sessionSecret: '******'
    sso:
      clientSecret: '******'

    congJson: |
      {
        "temporaryUploadPath": "uploads",
        "archiveFileBaseName": "IMG_",
        "templates": {
          "path": "templates"
        },
        "albumExpirationInDays": 90,
        "minio": {
          "bucket": "secure-image-pr",
          "port": 9000,
          "secure": false,
          "expiry": 604800,
          "region": "us-east-1"
        },
        "session": {
          "maxAge": 604800000,
          "expires": 604800000,
          "domain": ".gov.bc.ca",
          "memcached": {
            "hosts": "{{ include "secure-image-api.fullname" . }}-memcached:11211"
          }
        },
        "sso": {
          "clientId": "******",
          "callback": "/v1/auth/callback",
          "authUrl": "https://******/auth/realms/******/protocol/openid-connect/auth",
          "tokenUrl": "https://******/auth/realms/******/protocol/openid-connect/token",
          "certsUrl": "https://******/auth/realms/******/protocol/openid-connect/certs"
        }
      }

   ```

   Check _values.yaml_ for other values to override.

2. To install a release for the first time, run following commands from the repo root
   ```
   oc login ...
   oc project ...
   helm install <release> -f api/helm/values.<env/release>.local.yaml api/helm
   ```
3. To update an existing release
   ```
   oc login ...
   oc project ...
   helm upgrade <release> -f api/helm/values.<env/release>.local.yaml api/helm
   ```
4. To delete an existing release
   ```
   oc login ...
   oc project ...
   helm uninstall <release>
   ```
