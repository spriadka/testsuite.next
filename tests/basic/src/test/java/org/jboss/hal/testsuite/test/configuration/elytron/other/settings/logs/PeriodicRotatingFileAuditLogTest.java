package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.logs;

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
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.LOGS_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PERIODIC_ROTATING_FILE_AUDIT_LOG_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PER_LOG_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PER_LOG_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PER_LOG_TRY_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PER_LOG_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SUFFIX;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SUFFIX_LOG;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.periodicRotatingFileAuditLogAddress;

@RunWith(Arquillian.class)
public class PeriodicRotatingFileAuditLogTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        Values params = Values.of(PATH, ANY_STRING).and(SUFFIX, SUFFIX_LOG);
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_DELETE), params).assertSuccess();
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE), params).assertSuccess();
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_TRY_UPDATE), params).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_TRY_UPDATE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_DELETE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_CREATE));
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
        console.verticalNavigation().selectSecondary(LOGS_ITEM, PERIODIC_ROTATING_FILE_AUDIT_LOG_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getPeriodicRotatingFileAuditLogTable();

        crud.create(periodicRotatingFileAuditLogAddress(PER_LOG_CREATE), table, f -> {
            f.text(NAME, PER_LOG_CREATE);
            f.text(PATH, ANY_STRING);
            f.text(SUFFIX, SUFFIX_LOG);
        });
    }

    @Test
    public void tryCreate() {
        TableFragment table = page.getPeriodicRotatingFileAuditLogTable();
        crud.createWithErrorAndCancelDialog(table, NAME, PATH);
    }

    @Test
    public void editPath() throws Exception {
        TableFragment table = page.getPeriodicRotatingFileAuditLogTable();
        FormFragment form = page.getPeriodicRotatingFileAuditLogForm();
        table.bind(form);
        table.select(PER_LOG_UPDATE);
        crud.update(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE), form, PATH);
    }

    @Test
    public void tryEditPath() {
        TableFragment table = page.getPeriodicRotatingFileAuditLogTable();
        FormFragment form = page.getPeriodicRotatingFileAuditLogForm();
        table.bind(form);
        table.select(PER_LOG_TRY_UPDATE);
        crud.updateWithError(form, f -> f.clear(PATH), PATH);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getPeriodicRotatingFileAuditLogTable();
        crud.delete(periodicRotatingFileAuditLogAddress(PER_LOG_DELETE), table, PER_LOG_DELETE);
    }
}
