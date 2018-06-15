# RBAC Testsuite for HAL

## Requirements

* ``docker`` - The **RBAC** testsuite for HAL uses [Arquillian cube](http://arquillian.org/arquillian-cube/) for starting up and closing down the running **Wildfly** instance. **Arquillian Cube** uses **docker** underneath this process. 
* ``arquillian-universe-bom`` - The **RBAC** testsuite uses **arquillian-universe-bom** to manage the **Arquillian** dependencies across the whole project. To successfully execute tests from this testsuite, please install the [custom arquillian-universe-bom](https://github.com/spriadka/arquillian-universe-bom) into your local maven repository.

## Arquillian Cube configuration

In order to execute the RBAC testsuite in either **standalone** or **domain** mode, we have provided two files:

* ``docker-compose.yml`` - used for standalone mode. Creates the **standalone_network** using docker and bounds the     **wildfly-rbac-standalone** container to it. The **standalone_network** creates an address of ``172.42.2.1`` for the Wildfly standalone.
* ``docker-compose.domain.yml`` - used for domain mode. Creates the **domain_network** using docker and bounds **wildfly-rbac-domain** container to it. The **domain_network** creates an address of ``172.32.2.1`` for the Wildfly domain.
To register and arbitrary amount of hosts, just edit the ``docker-compose.domain.yml`` file in following matter:
```yaml
services:
    wildfly-rbac-domain-master:
        build:
            context: src/test/resources/cube/domain
            args: # this one is needed for the master controller
                controller: domain
        ports:
            - "127.0.0.1:9990:9990"
        networks:
            domain_network:
                ipv4_address: 172.32.2.1
        command: ["--host-config","host-master.xml",
                  "-b","172.32.2.1",
                  "-bmanagement", "172.32.2.1"]
    first-host:
        build:
          context: src/test/resources/cube/domain
        networks:
          domain_network:
            ipv4_address: 172.32.2.2
        command: ["--host-config","host-slave.xml",
                  "-Djboss.host.name=first-host",
                  "-Djboss.domain.master.address=172.32.2.1",
                  "-Djboss.bind.address=172.32.2.2",
                  "-Djboss.bind.address.management=172.32.2.2",
                  "-Djboss.bind.address.unsecure=172.32.2.2"]

networks:
  domain_network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.32.0.0/16
          ip-range: 172.32.2.0/24
          gateway: 172.32.2.254
```
## Execution

* ``mvn -Prbac,firefox clean test -Dcontainer.definition.path=docker-compose.yml -Dsuite.controller.ip=172.42.2.1`` for the standalone mode.
* ``mvn -Prbac,firefox clean test -Dcontainer.definition.path=docker-compose.domain.yml -Dsuite.controller.ip=172.32.2.1`` for the domain mode. The ``container.definition.path`` and ``suite.controller.ip`` must be specified using command-line (for now). 

## TODO

Remove the need to specify the ``container.definition.path`` and ``suite.controller.ip`` properties needed for execution. This could be achieved by converting the executed containers into the [Container object dsl](http://arquillian.org/arquillian-cube/#_arquillian_cube_and_container_object_dsl)
