# University Management System

A application to manage University student details like schedule class, enrolment, publish and manage result.

## Port Summary

| Application Name     | Default Port |
|----------------------|--------------|
| authorization-server |   8000       |
| api-server           |   8100       |
| web-client (Pending) |   3000       |

## Project Structure

The project is dividend into 3 sub-projects:

* [authorization-server][1]: Contains configuration related to KeyCloak.
* [api-server][2]: API backend build using Spring Boot
* [web-client][3]: Web client build using React.js and MUI7

## Docker Compose Profile

Run docker compose with following profile:

* `dev` - Starts postgres, keycloak, api-server images

```
$ docker compose --profile <name> up
```

[1]: ./authorization-server
[2]: ./api-server
[3]: ./web-client
