package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.logs;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
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
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.HOSTNAME;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.LOCALHOST;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.LOGS_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SERVER_ADDRESS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SYSLOG_AUDIT_LOG_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SYS_LOG_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SYS_LOG_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SYS_LOG_TRY_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SYS_LOG_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.syslogAuditLogAddress;

@RunWith(Arquillian.class)
public class SyslogAuditLogTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        Values syslogParams = Values.of(HOSTNAME, ANY_STRING).and(PORT, Random.number()).and(SERVER_ADDRESS, LOCALHOST);
        operations.add(syslogAuditLogAddress(SYS_LOG_UPDATE), syslogParams).assertSuccess();
        operations.add(syslogAuditLogAddress(SYS_LOG_TRY_UPDATE), syslogParams).assertSuccess();
        operations.add(syslogAuditLogAddress(SYS_LOG_DELETE), syslogParams).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_DELETE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_CREATE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_UPDATE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_TRY_UPDATE));
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
        console.verticalNavigation().selectSecondary(LOGS_ITEM, SYSLOG_AUDIT_LOG_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getSyslogAuditLogTable();

        crud.create(syslogAuditLogAddress(SYS_LOG_CREATE), table, f -> {
            f.text(NAME, SYS_LOG_CREATE);
            f.text(HOSTNAME, ANY_STRING);
            f.number(PORT, Random.number());
            f.text(SERVER_ADDRESS, LOCALHOST);
        });
    }

    @Test
    public void tryCreate() {
        TableFragment table = page.getSyslogAuditLogTable();
        crud.createWithErrorAndCancelDialog(table, NAME, HOSTNAME);
    }

    @Test
    public void editPort() throws Exception {
        TableFragment table = page.getSyslogAuditLogTable();
        FormFragment form = page.getSyslogAuditLogForm();
        table.bind(form);
        table.select(SYS_LOG_UPDATE);
        crud.update(syslogAuditLogAddress(SYS_LOG_UPDATE), form, PORT, Random.number());
    }

    @Test
    public void tryEditHostname() {
        TableFragment table = page.getSyslogAuditLogTable();
        FormFragment form = page.getSyslogAuditLogForm();
        table.bind(form);
        table.select(SYS_LOG_TRY_UPDATE);
        crud.updateWithError(form, f -> f.clear(HOSTNAME), HOSTNAME);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getSyslogAuditLogTable();
        crud.delete(syslogAuditLogAddress(SYS_LOG_DELETE), table, SYS_LOG_DELETE);
    }
}
