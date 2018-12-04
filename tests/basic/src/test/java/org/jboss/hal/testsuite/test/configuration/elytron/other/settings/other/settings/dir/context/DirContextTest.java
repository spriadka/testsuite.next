package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.other.settings.dir.context;

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
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.resources.Ids.ELYTRON_DIR_CONTEXT;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_CONTEXT_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_CR_CRT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_CR_DEL;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_CR_UPD;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.DIR_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.OTHER_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PRINCIPAL;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.dirContextAddress;

@RunWith(Arquillian.class)
public class DirContextTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        Values dirCtxParams = Values.of(URL, ANY_STRING)
            .andObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(dirContextAddress(DIR_CR_CRT), Values.of(URL, ANY_STRING));
        operations.add(dirContextAddress(DIR_CR_UPD), dirCtxParams).assertSuccess();
        operations.add(dirContextAddress(DIR_CR_DEL), dirCtxParams).assertSuccess();
        operations.add(dirContextAddress(DIR_UPDATE), Values.of(URL, ANY_STRING)).assertSuccess();
        operations.add(dirContextAddress(DIR_DELETE), Values.of(URL, ANY_STRING)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(dirContextAddress(DIR_UPDATE));
            operations.removeIfExists(dirContextAddress(DIR_DELETE));
            operations.removeIfExists(dirContextAddress(DIR_CREATE));
            operations.removeIfExists(dirContextAddress(DIR_CR_CRT));
            operations.removeIfExists(dirContextAddress(DIR_CR_UPD));
            operations.removeIfExists(dirContextAddress(DIR_CR_DEL));
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

    @Before
    public void before() {
        page.navigate();
        console.verticalNavigation().selectSecondary(OTHER_ITEM, DIR_CONTEXT_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getDirContextTable();
        crud.create(dirContextAddress(DIR_CREATE), table, f -> {
            f.text(NAME, DIR_CREATE);
            f.text(URL, ANY_STRING);
        });
    }

    @Test
    public void tryCreate() {
        TableFragment table = page.getDirContextTable();

        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, DIR_CREATE), URL);
    }

    @Test
    public void editPrincipal() throws Exception {
        TableFragment table = page.getDirContextTable();
        FormFragment form = page.getDirContextForm();
        table.bind(form);
        table.select(DIR_UPDATE);
        crud.update(dirContextAddress(DIR_UPDATE), form, PRINCIPAL);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getDirContextTable();
        crud.delete(dirContextAddress(DIR_DELETE), table, DIR_DELETE);
    }

    @Test
    public void addCredentialReference() throws Exception {
        TableFragment table = page.getDirContextTable();
        FormFragment form = page.getDirContextCredentialReferenceForm();
        table.bind(form);
        table.select(DIR_CR_CRT);
        page.getDirContextTabs().select(Ids.build(ELYTRON_DIR_CONTEXT, CREDENTIAL_REFERENCE, TAB));
        form.emptyState().mainAction();
        console.verifySuccess();
        // the UI "add" operation adds a credential-reference with no inner attributes, as they are not required
        ModelNodeResult actualResult = operations.readAttribute(dirContextAddress(DIR_CR_CRT), CREDENTIAL_REFERENCE);
        Assert.assertTrue("attribute credential-reference should exist", actualResult.value().isDefined());
    }

    @Test
    public void editCredentialReference() throws Exception {
        TableFragment table = page.getDirContextTable();
        FormFragment form = page.getDirContextCredentialReferenceForm();
        table.bind(form);
        table.select(DIR_CR_UPD);
        page.getDirContextTabs().select(Ids.build(ELYTRON_DIR_CONTEXT, CREDENTIAL_REFERENCE, TAB));
        crud.update(dirContextAddress(DIR_CR_UPD), form, f -> f.text(CLEAR_TEXT, ANY_STRING),
            ver -> ver.verifyAttribute(CREDENTIAL_REFERENCE + "." + CLEAR_TEXT, ANY_STRING));
    }

    @Test
    public void deleteCredentialReference() throws Exception {
        TableFragment table = page.getDirContextTable();
        FormFragment form = page.getDirContextCredentialReferenceForm();
        table.bind(form);
        table.select(DIR_CR_DEL);
        page.getDirContextTabs().select(Ids.build(ELYTRON_DIR_CONTEXT, CREDENTIAL_REFERENCE, TAB));
        crud.deleteSingleton(dirContextAddress(DIR_CR_DEL), form,
            ver -> ver.verifyAttributeIsUndefined(CREDENTIAL_REFERENCE));
    }

}
