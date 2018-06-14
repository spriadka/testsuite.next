FROM halconsole/hal-wildfly

ARG controller=host

ENV WILDFLY_MANAGEMENT_USER admin
ENV WILDFLY_MANAGEMENT_PASSWORD admin
ENV SERVER_GROUP main-server-group
ENV SERVER_NAME server-one
ENV SETUP_DIR=/opt/jboss/setup
# Add domain specific config files
COPY domain/configuration/* /opt/jboss/wildfly/domain/configuration/

COPY mgmt-users.properties /opt/jboss/wildfly/domain/configuration

# Add the docker entrypoint script
COPY entrypoint.sh /opt/jboss/wildfly/bin/entrypoint.sh

COPY rbac-setup-domain.batch ${SETUP_DIR}/
COPY prepare-server.sh ${SETUP_DIR}/

# Change the ownership of added files/dirs to `jboss`
USER root
RUN chown -R jboss:jboss /opt/jboss/wildfly && \
    chmod +x /opt/jboss/wildfly/bin/entrypoint.sh && \
    chmod +x ${SETUP_DIR}/prepare-server.sh
USER jboss

RUN ${SETUP_DIR}/prepare-server.sh

EXPOSE 8080 9990 9999

ENTRYPOINT ["/opt/jboss/wildfly/bin/entrypoint.sh"]
CMD ["-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]