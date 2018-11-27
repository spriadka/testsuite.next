package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.stores.key.store;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.Ids.ELYTRON_KEY_STORE;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.JKS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_STORE_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_CR_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROVIDERS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.STORES_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.keyStoreAddress;

@RunWith(Arquillian.class)
public class KeyStoreTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values ksParams = Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credRef);
        operations.add(keyStoreAddress(KEY_ST_UPDATE), ksParams);
        operations.add(keyStoreAddress(KEY_ST_CR_UPDATE), ksParams);
        operations.add(keyStoreAddress(KEY_ST_DELETE), ksParams);
        operations.writeAttribute(keyStoreAddress(KEY_ST_UPDATE), PROVIDERS, PROV_LOAD_UPDATE);
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(keyStoreAddress(KEY_ST_CREATE));
            operations.removeIfExists(keyStoreAddress(KEY_ST_DELETE));
        } finally {
            client.close();
        }
    }

    @Drone
    private WebDriver browser;

    @Page
    private ElytronOtherSettingsPage page;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Before
    public void navigate() {
        page.navigate();
    }

    @Test
    public void tryCreate() {
        console.verticalNavigation().selectSecondary(STORES_ITEM, KEY_STORE_ITEM);
        crud.createWithErrorAndCancelDialog(page.getKeyStoreTable(), f -> f.text(NAME, KEY_ST_CREATE), TYPE);
    }

    @Test
    public void create() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, KEY_STORE_ITEM);
        TableFragment table = page.getKeyStoreTable();

        crud.create(keyStoreAddress(KEY_ST_CREATE), table, f -> {
            f.text(NAME, KEY_ST_CREATE);
            f.text(TYPE, JKS);
            f.text(CLEAR_TEXT, ANY_STRING);
        });
    }

    @Test
    public void editPath() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, KEY_STORE_ITEM);
        page.getKeyStoreTab().select(Ids.build(ELYTRON_KEY_STORE, ATTRIBUTES, TAB));
        TableFragment table = page.getKeyStoreTable();
        FormFragment form = page.getKeyStoreForm();
        table.bind(form);
        table.select(KEY_ST_UPDATE);
        crud.update(keyStoreAddress(KEY_ST_UPDATE), form, PATH);
    }

    @Test
    public void editCredentialReference() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, KEY_STORE_ITEM);
        TableFragment table = page.getKeyStoreTable();
        FormFragment form = page.getKeyStoreCredentialReferenceForm();
        table.bind(form);
        table.select(KEY_ST_CR_UPDATE);
        page.getKeyStoreTab().select(Ids.build(ELYTRON_KEY_STORE, CREDENTIAL_REFERENCE, TAB));
        crud.update(keyStoreAddress(KEY_ST_UPDATE), form, f -> f.text(CLEAR_TEXT, ANY_STRING),
            ver -> ver.verifyAttribute(CREDENTIAL_REFERENCE + "." + CLEAR_TEXT, ANY_STRING));
    }

    @Test
    public void delete() throws Exception {
        console.verticalNavigation().selectSecondary(STORES_ITEM, KEY_STORE_ITEM);
        TableFragment table = page.getKeyStoreTable();
        crud.delete(keyStoreAddress(KEY_ST_DELETE), table, KEY_ST_DELETE);
    }

}
