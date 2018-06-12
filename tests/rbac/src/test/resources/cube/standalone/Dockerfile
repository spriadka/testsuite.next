FROM halconsole/hal-wildfly-nightly

ENV SETUP_DIR=/opt/jboss/setup

RUN sed -i 's/<access-control provider="simple"/<access-control provider="rbac"/g' ${JBOSS_HOME}/standalone/configuration/standalone-full-ha.xml

COPY ./mgmt-users.properties ${JBOSS_HOME}/standalone/configuration

COPY ./rbac-setup-standalone.batch ${SETUP_DIR}/
COPY ./prepare-server.sh ${SETUP_DIR}/

USER root

RUN chown jboss:jboss -R /opt/jboss && \
    chmod +x ${SETUP_DIR}/prepare-server.sh
USER jboss

RUN ${SETUP_DIR}/prepare-server.sh

CMD ["/opt/jboss/wildfly/bin/standalone.sh","-c","standalone-full-ha.xml","-b","0.0.0.0","-bmanagement","0.0.0.0"]