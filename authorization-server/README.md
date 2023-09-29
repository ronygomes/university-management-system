# University Management System - Authorization Server


Port 8000

## Export KeyCloak Configuration

```shell
$ docker exec keycloak /bin/sh 
    -c "/opt/keycloak/bin/kc.sh export
    --file /tmp/uma-realm.json --realm=uma"

$ docker exec keycloak cat /tmp/uma-realm.json > uma-realm.json
```

or from Administrator Console `Realm settings > Action > Partial export`


```
$ docker compose up -d
```

/opt/keycloak/bin/kc.sh export --file /tmp/uma-realm.json --realm=uma-realm