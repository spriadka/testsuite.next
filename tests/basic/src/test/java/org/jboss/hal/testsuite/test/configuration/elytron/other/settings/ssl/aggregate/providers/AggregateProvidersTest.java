package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.aggregate.providers;

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
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AGGREGATE_PROVIDERS_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AGG_PRV_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AGG_PRV_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.AGG_PRV_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROVIDERS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE2;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PROV_LOAD_UPDATE3;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.aggregateProvidersAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.providerLoaderAddress;

@RunWith(Arquillian.class)
public class AggregateProvidersTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        Values aggValues = Values.ofList(PROVIDERS, PROV_LOAD_UPDATE, PROV_LOAD_UPDATE2);
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE)).assertSuccess();
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE2)).assertSuccess();
        operations.add(providerLoaderAddress(PROV_LOAD_UPDATE3)).assertSuccess();
        operations.add(aggregateProvidersAddress(AGG_PRV_DELETE), aggValues).assertSuccess();
        operations.add(aggregateProvidersAddress(AGG_PRV_UPDATE), aggValues).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(aggregateProvidersAddress(AGG_PRV_DELETE));
            operations.removeIfExists(aggregateProvidersAddress(AGG_PRV_UPDATE));
            operations.removeIfExists(aggregateProvidersAddress(AGG_PRV_CREATE));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE2));
            operations.removeIfExists(providerLoaderAddress(PROV_LOAD_UPDATE3));
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
        console.verticalNavigation().selectSecondary(SSL_ITEM, AGGREGATE_PROVIDERS_ITEM);
        table = page.getAggregateProvidersTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(aggregateProvidersAddress(AGG_PRV_CREATE), table, f -> {
            f.text(NAME, AGG_PRV_CREATE);
            f.list(PROVIDERS).add(PROV_LOAD_UPDATE).add(PROV_LOAD_UPDATE2);
        });
    }

    @Test
    public void tryCreate() {
        crud.createWithErrorAndCancelDialog(table, AGG_PRV_CREATE, PROVIDERS);
    }

    @Test
    public void editProviders() throws Exception {
        FormFragment form = page.getAggregateProvidersForm();
        table.bind(form);
        table.select(AGG_PRV_UPDATE);

        crud.update(aggregateProvidersAddress(AGG_PRV_UPDATE), form, f -> f.list(PROVIDERS).add(PROV_LOAD_UPDATE3),
            verify -> verify.verifyListAttributeContainsValue(PROVIDERS, PROV_LOAD_UPDATE3));
    }

    @Test
    public void delete() throws Exception {
        crud.delete(aggregateProvidersAddress(AGG_PRV_DELETE), table, AGG_PRV_DELETE);
    }
}
