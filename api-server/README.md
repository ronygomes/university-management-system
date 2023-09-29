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
