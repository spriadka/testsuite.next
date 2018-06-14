package org.jboss.hal.testsuite.test;

import java.io.IOException;

import org.apache.commons.lang.RandomStringUtils;
import org.arquillian.reporter.api.event.Standalone;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Authentication;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.RBACOperations;
import org.jboss.hal.testsuite.RbacRole;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.finder.FinderPath;
import org.jboss.hal.testsuite.page.RoleAssignmentPage;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DeployerGroupRoleAssignmentTest {

    private static final OnlineManagementClient client =
        ManagementClient.onlineLazy(
            OnlineOptions.standalone()
                .hostAndPort(ConfigUtils.get("suite.controller.ip", "localhost"),
                    9990)
                .auth(RbacRole.ADMINISTRATOR.username, RbacRole.ADMINISTRATOR.password)
                .connectionTimeout(50000)
                .build());

    private static final RBACOperations rbacOps = new RBACOperations(client);

    @Drone
    private WebDriver browser;

    @Page
    private RoleAssignmentPage page;

    @Inject
    private CrudOperations crudOperations;

    @Inject
    private Console console;

    @AfterClass
    public static void afterClass() throws IOException {
        client.close();
    }

    @Before
    public void setUp() {
        Authentication.with(browser).authenticate(RbacRole.ADMINISTRATOR);
    }

    @Test
    public void createGroupDeployerVerifyAssignment() throws Exception {
        String groupName = RandomStringUtils.randomAlphanumeric(7);
        String realmName = RandomStringUtils.randomAlphanumeric(7);
        Address groupInDeployerRoleAddress = rbacOps.getGroupIncludedInRole(groupName, realmName, "Deployer");
        ResourceVerifier groupInDeployerRoleVerifier = new ResourceVerifier(groupInDeployerRoleAddress, client);
        groupInDeployerRoleVerifier.verifyDoesNotExist();
        console.finder(NameTokens.ACCESS_CONTROL, new FinderPath().append(Ids.ACCESS_CONTROL_BROWSE_BY, "groups"))
            .column(Ids.GROUP).action(Ids.ROLE_ADD);
        AddResourceDialogFragment addResourceDialogFragment = console.addResourceDialog();
        FormFragment addGroupForm = addResourceDialogFragment.getForm();
        addGroupForm.text("name", groupName);
        addGroupForm.list("include").add("Deployer");
        addResourceDialogFragment.add();
        console.verifySuccess();
        groupInDeployerRoleVerifier.verifyAttribute("type", "GROUP");
        groupInDeployerRoleVerifier.verifyExists();

        //page.removeGroup(groupName, realmName);
        groupInDeployerRoleVerifier.verifyDoesNotExist();
    }
}
