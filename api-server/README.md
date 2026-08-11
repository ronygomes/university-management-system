# University Management System - API Server

Primary api-server, data is consumed by web clients.

**Port**: 8100

## Build a Docker Image

A multi-stage [`Dockerfile`](./Dockerfile) builds the executable jar inside the
image (Amazon Corretto 25 and Gradle 9.6.0 wrapper), so no local JDK or Gradle
is required.

```shell
$ docker build -t ronygomes/ums-api-server .

$ docker tag ronygomes/ums-api-server:latest ronygomes/ums-api-server:0.1.0
$ docker push ronygomes/ums-api-server:0.1.0
```

## Get Access Token and Query API

```shell
$ JWT_ACCESS_TOKEN=$(curl -s 'http://localhost:8000/realms/ums/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'grant_type=password' \
-d 'username=admin' \
-d 'password=12345' \
-d 'client_id=ums-client-webapp' \
-d 'redirect_uri=http://localhost:3000/' | jq -r '.access_token')

$ curl -sH "Authorization: Bearer $JWT_ACCESS_TOKEN" http://localhost:8100/v1/departments/CSE | jq .name

"Computer Science & Engineering"

# For accessing HAL forms, need to add `application/prs.hal-forms+json` as Accept header
$ curl -H "Accept: application/prs.hal-forms+json" http://localhost:8100/v1/departments/CSE
```

## Query Database

```shell
# Password 12345
$ docker run -it --rm \
    --network api-server_default postgres:16.0 \
     psql -h ums_postgres -U postgres

postgres=# \connect ums;
ums=# \dt  " List all tables
ums=# \quit
```

## Backup Database

```shell
# Requires -it as prompts for password and need to enter.
# Can't use regular redirect (>) for password input
$ docker exec -it ums_postgres \
     pg_dump -h ums_postgres -U postgres ums | tee ums_dump.sql
```

## Docker Compose Profile

Run docker compose with following profile:

* `dev` - Starts postgres, keycloak images
* `dev-auth-local` - postgres only, expects a Keycloak server running locally on :8000
* `standalone`- Runs postgres, keycloak, api-server docker images

```
$ docker compose --profile <name> up
```
