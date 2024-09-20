# University Management System - Authorization Server

Contains configuration to run KeyCloak as Authorization Server using Docker Compose.
Following data is imported as initial data:

**Port**: 8000 \
**Client**: ums-client-webapp \
**Realm**: ums \
**Users:**

* **Username:** admin, **Password:** 12345, **Email:** admin@ums.dev, **Role:** admin
* **Username:** teacher, **Password:** 12345, **Email:** teacher@ums.dev, **Role:** teacher
* **Username:** student, **Password:** 12345, **Email:** student@ums.dev, **Role:** student


## Run Server

Following command will start the server in development mode:
```
$ docker compose up -d
```

## Export KeyCloak Configuration

```shell
$ docker exec $CONTAINER_NAME /bin/sh 
    -c "/opt/keycloak/bin/kc.sh export
    --file /tmp/ums-realm.json --realm=ums"

$ docker exec $CONTAINER_NAME cat /tmp/ums-realm.json > ums-realm.json
```

or from Administrator Console `Realm settings > Action > Partial export`

## KeyCloak JWK Endpoints

These are some important KeyCloak URLs invoked by Spring Security while configuration.

* http://localhost:8000/realms/ums/
```
{
  "realm": "ums",
  "public_key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtTxF0QpaQgPxTW0Fx7RFEhMXHNq3oe0Q5mm8kdXNzMZ1QtJKgzowte+9L78EKL9oL7PBfSxqSAV76M64QMRnA5JMU/BqgVn8lb34d1KapWcs11wXQQPbiyONarUmNQEw2UcuQrmXhLVvJxxZygQR/V/HTiEzzdq575cRt6j4C4FyAWbyzwSFiKEczMz/tMKOH4lJj8sNNsqZgHaxqMXd+561b83YUp3tqVzUQAFhLMQoQYUxe4OglGbloRarbx85DlBrUko10Nz7WWjyAwZxJ6e3/wxa9HWnkjccuZkgYSd6p392vAPMviEbfdqZMJd8VjQ6zFhOqMC2GHQ1VfeqPwIDAQAB",
  "token-service": "http://localhost:8000/realms/ums/protocol/openid-connect",
  "account-service": "http://localhost:8000/realms/ums/account",
  "tokens-not-before": 0
}
```

* http://localhost:8000/realms/ums/.well-known/openid-configuration

* http://localhost:8000/realms/ums/protocol/openid-connect/certs
