package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.key.manager;

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
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.Ids.ELYTRON_KEY_MANAGER;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.JKS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MANAGER_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MAN_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MAN_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MAN_TRY_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MAN_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.keyManagerAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.keyStoreAddress;

@RunWith(Arquillian.class)
public class KeyManagerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values ksParams = Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credRef);
        operations.add(keyStoreAddress(KEY_ST_UPDATE), ksParams);
        Values keyManagerValues = Values.of(KEY_STORE, KEY_ST_UPDATE)
            .andObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(keyManagerAddress(KEY_MAN_UPDATE), keyManagerValues).assertSuccess();
        operations.add(keyManagerAddress(KEY_MAN_TRY_UPDATE), keyManagerValues).assertSuccess();
        operations.add(keyManagerAddress(KEY_MAN_DELETE), keyManagerValues).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(keyManagerAddress(KEY_MAN_CREATE));
            operations.removeIfExists(keyManagerAddress(KEY_MAN_UPDATE));
            operations.removeIfExists(keyManagerAddress(KEY_MAN_TRY_UPDATE));
            operations.removeIfExists(keyManagerAddress(KEY_MAN_DELETE));
            operations.removeIfExists(keyStoreAddress(KEY_ST_UPDATE));
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

    private TableFragment table;

    @Before
    public void before() {
        page.navigate();
        console.verticalNavigation().selectSecondary(SSL_ITEM, KEY_MANAGER_ITEM);
        table = page.getKeyManagerTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(keyManagerAddress(KEY_MAN_CREATE), table, f -> {
            f.text(NAME, KEY_MAN_CREATE);
            f.text(KEY_STORE, KEY_ST_UPDATE);
            f.text(CLEAR_TEXT, ANY_STRING);
        });
    }

    @Test
    public void tryCreate() {
        crud.createWithErrorAndCancelDialog(table, KEY_MAN_CREATE, KEY_STORE);
    }

    @Test
    public void editProviderName() throws Exception {
        FormFragment form = page.getKeyManagerForm();
        table.bind(form);
        table.select(KEY_MAN_UPDATE);
        page.getKeyManagerTab().select(Ids.build(ELYTRON_KEY_MANAGER, ATTRIBUTES, TAB));
        crud.update(keyManagerAddress(KEY_MAN_UPDATE), form, PROVIDER_NAME);
    }

    @Test
    public void tryUpdate() {
        FormFragment form = page.getKeyManagerForm();
        table.bind(form);
        table.select(KEY_MAN_TRY_UPDATE);
        page.getKeyManagerTab().select(Ids.build(ELYTRON_KEY_MANAGER, ATTRIBUTES, TAB));
        crud.updateWithError(form, f -> f.clear(KEY_STORE), KEY_STORE);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(keyManagerAddress(KEY_MAN_DELETE), table, KEY_MAN_DELETE);
    }

    @Test
    public void editCredentialReference() throws Exception {
        FormFragment form = page.getKeyManagerCredentialReferenceForm();
        table.bind(form);
        table.select(KEY_MAN_UPDATE);
        page.getKeyManagerTab().select(Ids.build(ELYTRON_KEY_MANAGER, CREDENTIAL_REFERENCE, TAB));
        crud.update(keyManagerAddress(KEY_MAN_UPDATE), form, f -> f.text(CLEAR_TEXT, ANY_STRING),
            ver -> ver.verifyAttribute(CREDENTIAL_REFERENCE + "." + CLEAR_TEXT, ANY_STRING));
    }
}
