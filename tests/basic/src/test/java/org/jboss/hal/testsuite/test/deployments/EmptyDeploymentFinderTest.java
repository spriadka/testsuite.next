package org.jboss.hal.testsuite.test.deployments;

import java.io.IOException;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.finder.ColumnFragment;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
public class EmptyDeploymentFinderTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    private static final Operations operations = new Operations(client);

    private static final String DEPLOYMENT_CREATE = "deployment-to-be-created-"
        + RandomStringUtils.randomAlphanumeric(7);

    private static final String DEPLOYMENT_VIEW = "deployment-to-be-viewed-"
        + RandomStringUtils.randomAlphanumeric(7);

    private static final String DEPLOYMENT_REFRESH = "deployment-to-be-refreshed-"
        + RandomStringUtils.randomAlphanumeric(7);

    private static final String DEPLOYMENT_DELETE = "deployment-to-be-deleted-"
        + RandomStringUtils.randomAlphanumeric(7);

    private static final String DEPLOYMENT_ENABLE = "deployment-to-be-enabled-"
        + RandomStringUtils.randomAlphanumeric(7);

    private static final String DEPLOYMENT_DISABLE = "deployment-to-be-disabled-"
        + RandomStringUtils.randomAlphanumeric(7);

    @BeforeClass
    public static void setUp() throws IOException {
        createEmptyDeploymentAndEnable(DEPLOYMENT_VIEW, false);
        createEmptyDeploymentAndEnable(DEPLOYMENT_DELETE, false);
        createEmptyDeploymentAndEnable(DEPLOYMENT_ENABLE, false);
        createEmptyDeploymentAndEnable(DEPLOYMENT_DISABLE, true);
    }

    private static void createEmptyDeploymentAndEnable(String deploymentName, boolean enable) throws IOException {
        operations.add(Address.deployment(deploymentName),
            Values.of("content",
                new ModelNodeGenerator.ModelNodeListBuilder(new ModelNodeGenerator.ModelNodePropertiesBuilder()
                    .addProperty("empty", new ModelNode(true)).build()).build()).and("enabled", enable));
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(Address.deployment(DEPLOYMENT_VIEW));
            operations.removeIfExists(Address.deployment(DEPLOYMENT_CREATE));
            operations.removeIfExists(Address.deployment(DEPLOYMENT_REFRESH));
            operations.removeIfExists(Address.deployment(DEPLOYMENT_DELETE));
            operations.removeIfExists(Address.deployment(DEPLOYMENT_ENABLE));
        } finally {
            client.close();
        }
    }

    @Inject
    private Console console;

    @Inject
    private CrudOperations crudOperations;

    private ColumnFragment deploymentsColumn;

    @Before
    public void initColumn() {
        deploymentsColumn = console.finder(NameTokens.DEPLOYMENTS)
            .column("deployment");
    }

    @Test
    public void view() {
        deploymentsColumn.selectItem(Ids.deployment(DEPLOYMENT_VIEW))
            .view();
        console.verify(new PlaceRequest.Builder().nameToken(NameTokens.DEPLOYMENT)
            .with(NameTokens.DEPLOYMENT, DEPLOYMENT_VIEW)
            .build());
    }

    @Test
    public void create() throws Exception {
        deploymentsColumn.dropdownAction(Ids.DEPLOYMENT_ADD_ACTIONS, Ids.DEPLOYMENT_EMPTY_CREATE);
        AddResourceDialogFragment addEmptyDeploymentFragment = console.addResourceDialog();
        addEmptyDeploymentFragment.getForm().text("name", DEPLOYMENT_CREATE);
        addEmptyDeploymentFragment.add();
        console.verifySuccess();
        Assert.assertTrue("Newly created empty deployment should be present in the table",
            deploymentsColumn.containsItem(Ids.deployment(DEPLOYMENT_CREATE)));
        new ResourceVerifier(Address.deployment(DEPLOYMENT_CREATE), client)
            .verifyExists();
    }

    @Test
    public void delete() throws Exception {
        Assert.assertTrue("Deployment to be removed should be present in the column before removal",
            deploymentsColumn.containsItem(Ids.deployment(DEPLOYMENT_DELETE)));
        deploymentsColumn.selectItem(Ids.deployment(DEPLOYMENT_DELETE))
            .dropdown()
            .click("Remove");
        console.confirmationDialog().confirm();
        console.verifySuccess();
        Assert.assertFalse("Freshly removed deployment should not be present in the table anymore",
            deploymentsColumn.containsItem(Ids.deployment(DEPLOYMENT_DELETE)));
        new ResourceVerifier(Address.deployment(DEPLOYMENT_DELETE), client).verifyDoesNotExist();
    }

    @Test
    public void enable() throws Exception {
        deploymentsColumn.selectItem(Ids.deployment(DEPLOYMENT_ENABLE))
            .dropdown()
            .click("Enable");
        console.verifySuccess();
        new ResourceVerifier(Address.deployment(DEPLOYMENT_ENABLE), client).verifyAttribute("enabled", true);
    }

    @Test
    public void disable() throws Exception {
        deploymentsColumn.selectItem(Ids.deployment(DEPLOYMENT_DISABLE))
            .dropdown()
            .click("Disable");
        new ResourceVerifier(Address.deployment(DEPLOYMENT_DISABLE), client).verifyAttribute("enabled", false);
    }

    @Test
    public void refresh() throws Exception {
        Assert.assertFalse("Deployments column should not contain deployment to be refreshed",
            deploymentsColumn.containsItem(Ids.deployment(DEPLOYMENT_REFRESH)));
        createEmptyDeploymentAndEnable(DEPLOYMENT_REFRESH, false);
        new ResourceVerifier(Address.deployment(DEPLOYMENT_REFRESH), client).verifyExists();
        deploymentsColumn.dropdownAction(Ids.DEPLOYMENT_ADD_ACTIONS, Ids.DEPLOYMENT_REFRESH);
        Assert.assertTrue("Newly created deployment should be present in the table after refresh",
            deploymentsColumn.containsItem(Ids.deployment(DEPLOYMENT_REFRESH)));
    }
}