package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.server.ssl.context;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_MANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.JKS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_MAN_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.KEY_ST_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SERVER_SSL_CONTEXT_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SRV_SSL_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SRV_SSL_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SRV_SSL_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.keyManagerAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.keyStoreAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.serverSslContextAddress;

@RunWith(Arquillian.class)
public class ServerSSLContextTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values ksParams = Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credRef);
        operations.add(keyStoreAddress(KEY_ST_UPDATE), ksParams).assertSuccess();
        Values keyManagerValues = Values.of(KEY_STORE, KEY_ST_UPDATE)
            .andObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(keyManagerAddress(KEY_MAN_UPDATE), keyManagerValues).assertSuccess();
        Values serverSslContextValues = Values.of(KEY_MANAGER, KEY_MAN_UPDATE);
        operations.add(serverSslContextAddress(SRV_SSL_DELETE), serverSslContextValues).assertSuccess();
        operations.add(serverSslContextAddress(SRV_SSL_UPDATE), serverSslContextValues).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(serverSslContextAddress(SRV_SSL_UPDATE));
            operations.removeIfExists(serverSslContextAddress(SRV_SSL_CREATE));
            operations.removeIfExists(serverSslContextAddress(SRV_SSL_DELETE));
            operations.removeIfExists(keyManagerAddress(KEY_MAN_UPDATE));
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
        console.verticalNavigation().selectSecondary(SSL_ITEM, SERVER_SSL_CONTEXT_ITEM);
        table = page.getServerSslContextTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(serverSslContextAddress(SRV_SSL_CREATE), table, f -> {
            f.text(NAME, SRV_SSL_CREATE);
            f.text(KEY_MANAGER, KEY_MAN_UPDATE);
        });
    }

    @Test
    public void tryCreate() {
        crud.createWithErrorAndCancelDialog(table, SRV_SSL_CREATE, KEY_MANAGER);
    }

    @Test
    public void editProviderName() throws Exception {
        FormFragment form = page.getServerSslContextForm();
        table.bind(form);
        table.select(SRV_SSL_UPDATE);

        crud.update(serverSslContextAddress(SRV_SSL_UPDATE), form, PROVIDER_NAME, ANY_STRING);
    }

    @Test
    public void tryEditKeyManager() {
        FormFragment form = page.getServerSslContextForm();
        table.bind(form);
        table.select(SRV_SSL_UPDATE);

        crud.updateWithError(form, f -> f.clear(KEY_MANAGER), KEY_MANAGER);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(serverSslContextAddress(SRV_SSL_DELETE), table, SRV_SSL_DELETE);
    }
}
