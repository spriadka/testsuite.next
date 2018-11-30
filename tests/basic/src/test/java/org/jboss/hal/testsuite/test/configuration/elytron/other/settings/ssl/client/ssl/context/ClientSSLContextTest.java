package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.client.ssl.context;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_NAME;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CLIENT_SSL_CONTEXT_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CLI_SSL_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CLI_SSL_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CLI_SSL_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.clientSslContextAddress;

@RunWith(Arquillian.class)
public class ClientSSLContextTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(clientSslContextAddress(CLI_SSL_DELETE)).assertSuccess();
        operations.add(clientSslContextAddress(CLI_SSL_UPDATE)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(clientSslContextAddress(CLI_SSL_UPDATE));
            operations.removeIfExists(clientSslContextAddress(CLI_SSL_CREATE));
            operations.removeIfExists(clientSslContextAddress(CLI_SSL_DELETE));
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
        console.verticalNavigation().selectSecondary(SSL_ITEM, CLIENT_SSL_CONTEXT_ITEM);
        table = page.getClientSslContextTable();
    }


    @Test
    public void create() throws Exception {
        crud.create(clientSslContextAddress(CLI_SSL_CREATE), table, CLI_SSL_CREATE);
    }

    @Test
    public void editProviderName() throws Exception {
        FormFragment form = page.getClientSslContextForm();
        table.bind(form);
        table.select(CLI_SSL_UPDATE);
        crud.update(clientSslContextAddress(CLI_SSL_UPDATE), form, PROVIDER_NAME);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(clientSslContextAddress(CLI_SSL_DELETE), table, CLI_SSL_DELETE);
    }

}
