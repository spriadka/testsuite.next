#!/bin/sh
function wait_for_server() {
  until `${JBOSS_HOME}/bin/jboss-cli.sh -c ":read-attribute(name=server-state)" 2> /dev/null | grep -q running`; do
    sleep 1
  done
}

$JBOSS_HOME/bin/standalone.sh -c standalone-full-ha.xml > /dev/null &
wait_for_server
${JBOSS_HOME}/bin/jboss-cli.sh -c --file=${SETUP_DIR}/rbac-setup-standalone.batch
${JBOSS_HOME}/bin/jboss-cli.sh -c "shutdown"