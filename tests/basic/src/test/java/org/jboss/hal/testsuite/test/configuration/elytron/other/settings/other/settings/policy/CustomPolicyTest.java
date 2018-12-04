package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.other.settings.policy;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.EmptyState;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM_POLICY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.testsuite.Selectors.contains;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ADD_CUSTOM_POLICY;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.OTHER_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.POLICY_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.POL_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.policyAddress;

@RunWith(Arquillian.class)
public class CustomPolicyTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(policyAddress(POL_CREATE));
        } finally {
            client.close();
        }
    }

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Drone
    private WebDriver browser;

    @Page
    private ElytronOtherSettingsPage page;

    @Test
    public void create() throws Exception {
        operations.removeIfExists(policyAddress(POL_CREATE));
        page.navigate();
        console.verticalNavigation().selectSecondary(OTHER_ITEM, POLICY_ITEM);
        EmptyState emptyState = page.getEmptyPolicy();
        By selector = ByJQuery.selector("button" + contains(ADD_CUSTOM_POLICY));
        emptyState.getRoot().findElement(selector).click();

        AddResourceDialogFragment addDialog = console.addResourceDialog();
        addDialog.getForm().text(NAME, POL_CREATE);
        addDialog.getForm().text(CLASS_NAME, ANY_STRING);
        addDialog.add();

        console.verifySuccess();
        new ResourceVerifier(policyAddress(POL_CREATE), client).verifyExists();
    }

    @Test
    public void editModule() throws Exception {
        if (!operations.exists(policyAddress(POL_CREATE))) {
            ModelNode customPolicy = new ModelNode();
            customPolicy.get(CLASS_NAME).set(ANY_STRING);
            operations.add(policyAddress(POL_CREATE), Values.of(CUSTOM_POLICY, customPolicy));
        }
        page.navigate();
        console.verticalNavigation().selectSecondary(OTHER_ITEM, POLICY_ITEM);
        FormFragment form = page.getPolicyCustomForm();
        String module = Random.name();
        crud.update(policyAddress(POL_CREATE), form, f -> f.text(MODULE, module),
            verify -> verify.verifyAttribute("custom-policy.module", module));
    }

}
