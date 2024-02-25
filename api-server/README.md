# University Management System - API Server

Port 8100

```shell
$ JWT_ACCESS_TOKEN=$(curl -s 'http://localhost:8000/realms/ums/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'grant_type=password' \
-d 'username=john' \
-d 'password=12345' \
-d 'client_id=ums-client-webapp' \
-d 'redirect_uri=http://localhost:8100/' | jq -r '.access_token')

$ curl -H "Authorization: Bearer $JWT_ACCESS_TOKEN" http://localhost:8100/home

Welcome from Home!
```


```shell
# Password 12345
$ docker run -it --rm \
    --network api-server_default postgres:16.0 \
     psql -h ums_postgres -U postgres

postgres=# \connect ums;
ums=# \dt  " List all tables
ums=# \quit
```

```shell
# Requires -it as prompts for password and need to enter.
# Can't use regular redirect (>) for password input
$ docker exec -it ums_postgres \
     pg_dump -h ums_postgres -U postgres ums | tee ums_dump.sql
```