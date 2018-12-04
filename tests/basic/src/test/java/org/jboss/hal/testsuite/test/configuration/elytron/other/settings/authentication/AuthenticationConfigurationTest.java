package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.authentication;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.resources.Ids.ELYTRON_AUTHENTICATION_CONFIGURATION;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUTHENTICATION_CONFIGURATION_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUTHENTICATION_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUTHENTICATION_NAME;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_CR_CRT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_CR_DEL;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_CR_UPD;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AUT_CF_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.authenticationConfigurationAddress;

@RunWith(Arquillian.class)
public class AuthenticationConfigurationTest {

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(authenticationConfigurationAddress(AUT_CF_UPDATE)).assertSuccess();
        operations.add(authenticationConfigurationAddress(AUT_CF_DELETE)).assertSuccess();
        Values autParams = Values.ofObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_CRT)).assertSuccess();
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_UPD), autParams).assertSuccess();
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_DEL), autParams).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CREATE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_UPDATE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_DELETE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_CRT));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_UPD));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_DEL));
        } finally {
            client.close();
        }
    }

    @Page
    private ElytronOtherSettingsPage page;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Drone
    private WebDriver browser;

    @Before
    public void before() {
        page.navigate();
        console.verticalNavigation().selectSecondary(AUTHENTICATION_ITEM, AUTHENTICATION_CONFIGURATION_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        crud.create(authenticationConfigurationAddress(AUT_CF_CREATE), table, AUT_CF_CREATE);
    }

    @Test
    public void editAuthenticationName() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        FormFragment form = page.getAuthenticationConfigurationForm();
        table.bind(form);
        table.select(AUT_CF_UPDATE);
        crud.update(authenticationConfigurationAddress(AUT_CF_UPDATE), form, AUTHENTICATION_NAME);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        crud.delete(authenticationConfigurationAddress(AUT_CF_DELETE), table, AUT_CF_DELETE);
    }

    @Test
    public void addCredentialReference() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        FormFragment form = page.getAuthConfigCredentialReferenceForm();
        table.bind(form);
        table.select(AUT_CF_CR_CRT);
        page.getAuthenticationConfigurationTabs().select(
            Ids.build(ELYTRON_AUTHENTICATION_CONFIGURATION, CREDENTIAL_REFERENCE, TAB));
        form.emptyState().mainAction();
        console.verifySuccess();
        // the UI "add" operation adds a credential-reference with no inner attributes, as they are not required
        ModelNodeResult actualResult = operations.readAttribute(authenticationConfigurationAddress(AUT_CF_CR_CRT),
            CREDENTIAL_REFERENCE);
        Assert.assertTrue("attribute credential-reference should exist", actualResult.value().isDefined());
    }

    @Test
    public void editCredentialReference() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        FormFragment form = page.getAuthConfigCredentialReferenceForm();
        table.bind(form);
        table.select(AUT_CF_CR_UPD);
        page.getAuthenticationConfigurationTabs()
            .select(Ids.build(ELYTRON_AUTHENTICATION_CONFIGURATION, CREDENTIAL_REFERENCE, TAB));
        crud.update(authenticationConfigurationAddress(AUT_CF_CR_UPD), form, f -> f.text(CLEAR_TEXT, ANY_STRING),
            ver -> ver.verifyAttribute(CREDENTIAL_REFERENCE + "." + CLEAR_TEXT, ANY_STRING));
    }

    @Test
    public void deleteCredentialReference() throws Exception {
        TableFragment table = page.getAuthenticationConfigurationTable();
        FormFragment form = page.getAuthConfigCredentialReferenceForm();
        table.bind(form);
        table.select(AUT_CF_CR_DEL);
        page.getAuthenticationConfigurationTabs()
            .select(Ids.build(ELYTRON_AUTHENTICATION_CONFIGURATION, CREDENTIAL_REFERENCE, TAB));
        crud.deleteSingleton(authenticationConfigurationAddress(AUT_CF_CR_DEL), form,
            ver -> ver.verifyAttributeIsUndefined(CREDENTIAL_REFERENCE));
    }

}
