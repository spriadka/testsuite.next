package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.provider.loader;

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

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROVIDER_LOADER_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE2;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE3;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.providerLoaderAddress;

@RunWith(Arquillian.class)
public class ProviderLoaderTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE)).assertSuccess();
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE2)).assertSuccess();
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE3)).assertSuccess();
        operations.add(providerLoaderAddress(PROV_LOAD_DELETE)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE2));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE3));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_CREATE));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_DELETE));
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
        console.verticalNavigation().selectSecondary(SSL_ITEM, PROVIDER_LOADER_ITEM);
        table = page.getProviderLoaderTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(providerLoaderAddress(PROV_LOAD_CREATE), table, PROV_LOAD_CREATE);
    }

    @Test
    public void editPath() throws Exception {
        FormFragment form = page.getProviderLoaderForm();
        table.bind(form);
        table.select(PROV_LOAD_UPDATE);
        crud.update(providerLoaderAddress(PROV_LOAD_UPDATE), form, PATH, ANY_STRING);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(providerLoaderAddress(PROV_LOAD_DELETE), table, PROV_LOAD_DELETE);
    }

}
