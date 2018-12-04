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
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SIZE_ROTATING_FILE_AUDIT_LOG_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SIZ_LOG_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SIZ_LOG_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SIZ_LOG_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.sizeRotatingFileAuditLogAddress;

@RunWith(Arquillian.class)
public class SizeRotatingLogTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(sizeRotatingFileAuditLogAddress(SIZ_LOG_DELETE), Values.of(PATH, ANY_STRING)).assertSuccess();
        operations.add(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE), Values.of(PATH, ANY_STRING)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_DELETE));
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE));
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_CREATE));
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
        console.verticalNavigation().selectSecondary(LOGS_ITEM, SIZE_ROTATING_FILE_AUDIT_LOG_ITEM);
    }

    @Test
    public void create() throws Exception {
        TableFragment table = page.getSizeRotatingFileAuditLogTable();

        crud.create(sizeRotatingFileAuditLogAddress(SIZ_LOG_CREATE), table, f -> {
            f.text(NAME, SIZ_LOG_CREATE);
            f.text(PATH, ANY_STRING);
        });
    }

    @Test
    public void tryCreate() {
        TableFragment table = page.getSizeRotatingFileAuditLogTable();
        crud.createWithErrorAndCancelDialog(table, NAME, PATH);
    }

    @Test
    public void editPath() throws Exception {
        TableFragment table = page.getSizeRotatingFileAuditLogTable();
        FormFragment form = page.getSizeRotatingFileAuditLogForm();
        table.bind(form);
        table.select(SIZ_LOG_UPDATE);
        crud.update(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE), form, PATH);
    }

    @Test
    public void tryEditPath() {
        TableFragment table = page.getSizeRotatingFileAuditLogTable();
        FormFragment form = page.getSizeRotatingFileAuditLogForm();
        table.bind(form);
        table.select(SIZ_LOG_UPDATE);
        crud.updateWithError(form, f -> f.clear(PATH), PATH);
    }

    @Test
    public void delete() throws Exception {
        TableFragment table = page.getSizeRotatingFileAuditLogTable();
        crud.delete(sizeRotatingFileAuditLogAddress(SIZ_LOG_DELETE), table, SIZ_LOG_DELETE);
    }
}
