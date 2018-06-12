#!/bin/sh

function wait_for_server() {
  until `${JBOSS_HOME}/bin/jboss-cli.sh -c --user=admin --password=admin "/host=master:read-attribute(name=host-state)" 2> /dev/null | grep -q running`; do
    sleep 1
  done
}

if [[ "${controller}" = "domain" ]]
then
    echo "Creating admin user"
    ${JBOSS_HOME}/bin/add-user.sh --silent -e -u ${WILDFLY_MANAGEMENT_USER} -p ${WILDFLY_MANAGEMENT_PASSWORD}
    sed -i "s/@WILDFLY_MANAGEMENT_USER@/${WILDFLY_MANAGEMENT_USER}/" ${JBOSS_HOME}/domain/configuration/host-slave.xml
    sed -i "s/@WILDFLY_MANAGEMENT_PASSWORD@/`echo ${WILDFLY_MANAGEMENT_PASSWORD} | base64`/" ${JBOSS_HOME}/domain/configuration/host-slave.xml
    echo "Setting up domain controller"
    exec nohup ${JBOSS_HOME}/bin/domain.sh > /dev/null 2>&1 &
    wait_for_server
    echo "RBAC-setup-process: running batch"
    ${JBOSS_HOME}/bin/jboss-cli.sh --user=admin --password=admin -c --file="${SETUP_DIR}/rbac-setup-domain.batch"
    ${JBOSS_HOME}/bin/jboss-cli.sh --user=admin --password=admin -c "/host=master:shutdown"
    echo "Cleaning up history"
    rm -rf "${JBOSS_HOME}/domain/configuration/domain_xml_history/current"
    rm -rf "${JBOSS_HOME}/domain/configuration/host_xml_history/current"
fi