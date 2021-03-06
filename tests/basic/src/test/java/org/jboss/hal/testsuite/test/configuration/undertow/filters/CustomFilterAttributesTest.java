package org.jboss.hal.testsuite.test.configuration.undertow.filters;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.configuration.UndertowFiltersPage;
import org.jboss.hal.testsuite.test.configuration.undertow.UndertowFiltersFixtures;
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

@RunWith(Arquillian.class)
public class CustomFilterAttributesTest {

    @Inject
    private Console console;

    @Inject
    private CrudOperations crudOperations;

    @Drone
    private WebDriver browser;

    @Page
    private UndertowFiltersPage page;

    private static final String CUSTOM_FILTER_EDIT =
        "custom-filter-to-be-edited-" + RandomStringUtils.randomAlphanumeric(7);

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();

    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(UndertowFiltersFixtures.customFilterAddress(CUSTOM_FILTER_EDIT),
            Values.of("class-name", Random.name()).and("module", Random.name()));
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        operations.removeIfExists(UndertowFiltersFixtures.customFilterAddress(CUSTOM_FILTER_EDIT));
    }

    @Before
    public void initPage() {
        page.navigate();
        console.verticalNavigation()
            .selectPrimary(Ids.build("undertow-custom-filter", "item"));
        page.getCustomFilterTable().select(CUSTOM_FILTER_EDIT);
    }

    @Test
    public void editClassName() throws Exception {
        crudOperations.update(UndertowFiltersFixtures.customFilterAddress(CUSTOM_FILTER_EDIT), page.getCustomFilterForm(),
            "class-name");
    }

    @Test
    public void editModule() throws Exception {
        crudOperations.update(UndertowFiltersFixtures.customFilterAddress(CUSTOM_FILTER_EDIT),
            page.getCustomFilterForm(), "module");
    }

    @Test
    public void editParameters() throws Exception {
        String key = Random.name();
        String value = Random.name();
        ModelNode exp = new ModelNode();
        exp.get(key).set(value);
        crudOperations.update(UndertowFiltersFixtures.customFilterAddress(CUSTOM_FILTER_EDIT), page.getCustomFilterForm(),
            form -> form.properties("parameters").add(key, value),
            resourceVerifier -> resourceVerifier.verifyAttribute("parameters", exp));
    }
}
