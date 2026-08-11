FROM quay.io/keycloak/keycloak:22.0.3

# Realm definition (clients, roles, seed users) imported on start.
COPY --chown=keycloak:keycloak ums-realm.json /opt/keycloak/data/import/ums-realm.json

ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin

EXPOSE 8000

CMD ["start-dev", "--http-port=8000", "--import-realm"]

