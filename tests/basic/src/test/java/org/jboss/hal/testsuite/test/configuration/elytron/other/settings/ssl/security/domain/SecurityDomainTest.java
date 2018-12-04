package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.ssl.security.domain;

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
import org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures;
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

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_REALM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REALM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REALMS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CONSTANT;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CONS_PRI_TRANS_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.FILESYS_RLM_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.FILESYS_RLM_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.OUTFLOW_SECURITY_DOMAINS;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.PRINCIPAL_TRANSFORMER;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SECURITY_DOMAIN_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SEC_DOM_CREATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SEC_DOM_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SEC_DOM_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SEC_DOM_UPDATE2;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SEC_DOM_UPDATE3;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.SSL_ITEM;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.constantPrincipalTransformerAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.filesystemRealmAddress;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.securityDomainAddress;

@RunWith(Arquillian.class)
public class SecurityDomainTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void setUp() throws IOException {
        operations.add(constantPrincipalTransformerAddress(CONS_PRI_TRANS_UPDATE), Values.of(CONSTANT, ANY_STRING)).assertSuccess();
        operations.add(filesystemRealmAddress(FILESYS_RLM_CREATE), Values.of(PATH, ANY_STRING)).assertSuccess();
        operations.add(filesystemRealmAddress(FILESYS_RLM_UPDATE), Values.of(PATH, ANY_STRING)).assertSuccess();
        ModelNode realmNode1 = new ModelNode();
        realmNode1.get(REALM).set(FILESYS_RLM_UPDATE);
        ModelNode realmNode2 = new ModelNode();
        realmNode2.get(REALM).set(FILESYS_RLM_CREATE);
        Values secDomainParams = Values.of(DEFAULT_REALM, FILESYS_RLM_UPDATE).andList(REALMS, realmNode1);
        operations.add(securityDomainAddress(SEC_DOM_UPDATE), secDomainParams).assertSuccess();
        operations.add(securityDomainAddress(SEC_DOM_UPDATE2), secDomainParams).assertSuccess();
        operations.add(securityDomainAddress(SEC_DOM_UPDATE3),
            Values.of(DEFAULT_REALM, FILESYS_RLM_UPDATE).andList(REALMS, realmNode1, realmNode2)).assertSuccess();
        operations.add(securityDomainAddress(SEC_DOM_DELETE)).assertSuccess();
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(securityDomainAddress(SEC_DOM_UPDATE));
            operations.removeIfExists(securityDomainAddress(SEC_DOM_UPDATE2));
            operations.removeIfExists(securityDomainAddress(SEC_DOM_UPDATE3));
            operations.removeIfExists(securityDomainAddress(SEC_DOM_DELETE));
            operations.removeIfExists(securityDomainAddress(SEC_DOM_CREATE));
            operations.removeIfExists(filesystemRealmAddress(FILESYS_RLM_CREATE));
            operations.removeIfExists(filesystemRealmAddress(FILESYS_RLM_UPDATE));
            operations.removeIfExists(constantPrincipalTransformerAddress(CONS_PRI_TRANS_UPDATE));
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
        console.verticalNavigation().selectSecondary(SSL_ITEM, SECURITY_DOMAIN_ITEM);
        table = page.getSecurityDomainTable();
    }

    @Test
    public void create() throws Exception {
        crud.create(securityDomainAddress(SEC_DOM_CREATE), table, f -> {
            f.text(NAME, SEC_DOM_CREATE);
            f.text(DEFAULT_REALM, FILESYS_RLM_UPDATE);
        });
    }

    @Test
    public void editOutflowSecurityDomains() throws Exception {
        FormFragment form = page.getSecurityDomainForm();
        table.bind(form);
        table.select(SEC_DOM_UPDATE);
        crud.update(securityDomainAddress(SEC_DOM_UPDATE), form,
            f -> f.list(OUTFLOW_SECURITY_DOMAINS).add(SEC_DOM_UPDATE2),
            verify -> verify.verifyListAttributeContainsValue(OUTFLOW_SECURITY_DOMAINS, SEC_DOM_UPDATE2));
    }

    @Test
    public void delete() throws Exception {
        crud.delete(securityDomainAddress(SEC_DOM_DELETE), table, SEC_DOM_DELETE);
    }

    @Test
    public void createRealm() throws Exception {
        TableFragment realmsTable = page.getSecurityDomainRealmsTable();
        table.action(SEC_DOM_UPDATE, ElytronFixtures.REALMS);
        waitGui().until().element(realmsTable.getRoot()).is().visible();
        try {
            crud.create(securityDomainAddress(SEC_DOM_UPDATE), realmsTable, f -> f.text(REALM, FILESYS_RLM_CREATE),
                vc -> vc.verifyListAttributeContainsSingleValue(REALMS, REALM, FILESYS_RLM_CREATE));
        } finally {
            // getting rid of action selection
            page.getSecurityDomainPages().breadcrumb().getBackToMainPage();
        }
    }

    @Test
    public void tryCreateRealm() {
        TableFragment realmsTable = page.getSecurityDomainRealmsTable();
        table.action(SEC_DOM_UPDATE, ElytronFixtures.REALMS);
        waitGui().until().element(realmsTable.getRoot()).is().visible();

        try {
            crud.createWithErrorAndCancelDialog(realmsTable, f -> f.text("role-decoder", ANY_STRING), REALM);
        } finally {
            // getting rid of action selection
            page.getSecurityDomainPages().breadcrumb().getBackToMainPage();
        }
    }

    @Test
    public void editRealm() throws Exception {
        TableFragment realmsTable = page.getSecurityDomainRealmsTable();
        FormFragment form = page.getSecurityDomainRealmsForm();
        table.action(SEC_DOM_UPDATE2, ElytronFixtures.REALMS);
        waitGui().until().element(realmsTable.getRoot()).is().visible();
        realmsTable.bind(form);
        realmsTable.select(FILESYS_RLM_UPDATE);

        try {
            crud.update(securityDomainAddress(SEC_DOM_UPDATE2), form,
                f -> f.text(PRINCIPAL_TRANSFORMER, CONS_PRI_TRANS_UPDATE),
                vc -> vc.verifyListAttributeContainsSingleValue(REALMS, PRINCIPAL_TRANSFORMER, CONS_PRI_TRANS_UPDATE));
        } finally {
            // getting rid of action selection
            page.getSecurityDomainPages().breadcrumb().getBackToMainPage();
        }
    }

    @Test
    public void deleteRealm() throws Exception {
        TableFragment realmsTable = page.getSecurityDomainRealmsTable();
        table.action(SEC_DOM_UPDATE3, ElytronFixtures.REALMS);
        waitGui().until().element(realmsTable.getRoot()).is().visible();
        try {
            crud.delete(securityDomainAddress(SEC_DOM_UPDATE3), realmsTable, FILESYS_RLM_CREATE,
                vc -> vc.verifyListAttributeDoesNotContainSingleValue(REALMS, REALM, FILESYS_RLM_CREATE));
        } finally {
            // getting rid of action selection
            page.getSecurityDomainPages().breadcrumb().getBackToMainPage();
        }
    }

}
