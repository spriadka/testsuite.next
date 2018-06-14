package org.jboss.hal.testsuite;

import java.io.IOException;

import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 * A convenience for common model related operations needed in RBAC use cases.
 */
public class RBACOperations {

    private static final String
        INCLUDE = "include",
        ROLE_MAPPING = "role-mapping",
        REALM = "realm",
        TYPE = "type",
        NAME = "name",
        GROUP = "GROUP",
        USER = "USER";

    private static final Address AUTHORIZATION_ADDRESS =
        Address.coreService("management").and("access", "authorization");

    private final Operations ops;

    public RBACOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
    }

    public Address getUserIncludedInRole(String user, String realm, String role) {
        return getPrincipalIncludedInRole("user-" + user + "@" + realm, role);
    }

    public Address getGroupIncludedInRole(String group, String realm, String role) {
        return getPrincipalIncludedInRole("group-" + group + "@" + realm, role);
    }

    public Address getPrincipalIncludedInRole(String principal, String role) {
        return AUTHORIZATION_ADDRESS.and(ROLE_MAPPING, role).and(INCLUDE, principal);
    }

    public Address addUserIncludedInRole(String user, String realm, String role) throws IOException {
        return addPrincipalIncludedInRole(user, realm, role, USER);
    }

    public Address addGroupIncludedInRole(String group, String realm, String role) throws IOException {
        return addPrincipalIncludedInRole(group, realm, role, GROUP);
    }

    public void removePrincipalFromRole(String principal, String role) throws IOException {
        ops.remove(getPrincipalIncludedInRole(principal, role));
    }

    private Address addPrincipalIncludedInRole(String principal, String realm, String role, String type)
        throws IOException {
        Address principalAddress = getPrincipalIncludedInRole(principal, role);
        ops.add(principalAddress, Values.of(NAME, principal).and(TYPE, type).and(REALM, realm)).assertSuccess();
        return principalAddress;
    }
}
