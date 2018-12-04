package org.jboss.hal.testsuite.test.configuration.elytron.other.settings;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.configuration.ElytronOtherSettingsPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

public class AbstractOtherSettingsTest {

    protected static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    protected static final Operations operations = new Operations(client);
    protected static final String PROPERTY_DELIMITER = ".";

    @BeforeClass
    public static void beforeTests() throws Exception {

        // used in key-store, as trust-manager requires a key-store with providers attribute set


        operations.add(dirContextAddress(DIR_UPDATE), Values.of(URL, ANY_STRING));
        operations.add(dirContextAddress(DIR_DELETE), Values.of(URL, ANY_STRING));

        Values dirCtxParams = Values.of(URL, ANY_STRING)
                .andObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(dirContextAddress(DIR_CR_CRT), Values.of(URL, ANY_STRING));
        operations.add(dirContextAddress(DIR_CR_UPD), dirCtxParams);
        operations.add(dirContextAddress(DIR_CR_DEL), dirCtxParams);

        operations.add(authenticationConfigurationAddress(AUT_CF_UPDATE));
        operations.add(authenticationConfigurationAddress(AUT_CF_DELETE));

        Values autParams = Values.ofObject(CREDENTIAL_REFERENCE, Values.of(CLEAR_TEXT, ANY_STRING));
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_CRT));
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_UPD), autParams);
        operations.add(authenticationConfigurationAddress(AUT_CF_CR_DEL), autParams);

        operations.add(authenticationContextAddress(AUT_CT_DELETE));
        operations.add(authenticationContextAddress(AUT_CT_UPDATE));
        ModelNode matchRuleUpdate = new ModelNode();
        matchRuleUpdate.get(MATCH_ABSTRACT_TYPE).set(AUT_CT_MR_UPDATE);
        ModelNode matchRuleDelete = new ModelNode();
        matchRuleDelete.get(MATCH_ABSTRACT_TYPE).set(AUT_CT_MR_DELETE);
        operations.add(authenticationContextAddress(AUT_CT_UPDATE2),
                Values.ofList(MATCH_RULES, matchRuleUpdate, matchRuleDelete));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {

            operations.removeIfExists(dirContextAddress(DIR_UPDATE));
            operations.removeIfExists(dirContextAddress(DIR_DELETE));
            operations.removeIfExists(dirContextAddress(DIR_CREATE));
            operations.removeIfExists(dirContextAddress(DIR_CR_CRT));
            operations.removeIfExists(dirContextAddress(DIR_CR_UPD));
            operations.removeIfExists(dirContextAddress(DIR_CR_DEL));
            // SSL
            // removeIfExists() the server-ssl-context before removing key-manager
            // key-store is a dependency on key-manager and trust-manager, removeIfExists() it after key-manager and trust-manager
            operations.removeIfExists(keyStoreAddress(KEY_ST_UPDATE));
            operations.removeIfExists(keyStoreAddress(KEY_ST_CR_UPDATE));
            operations.removeIfExists(filesystemRealmAddress(FILESYS_RLM_UPDATE));
            operations.removeIfExists(filesystemRealmAddress(FILESYS_RLM_CREATE));
            operations.removeIfExists(constantPrincipalTransformerAddress(CONS_PRI_TRANS_UPDATE));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_UPDATE));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_UPDATE2));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_DELETE));
            operations.removeIfExists(authenticationContextAddress(AUT_CT_CREATE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CREATE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_UPDATE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_DELETE));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_CRT));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_UPD));
            operations.removeIfExists(authenticationConfigurationAddress(AUT_CF_CR_DEL));
            // removeIfExists() the aggregate-security-event-listener first, as they require size audit log and syslog
            operations.removeIfExists(policyAddress(POL_CREATE));
        } finally {
            client.close();
        }
    }

    @Page
    protected ElytronOtherSettingsPage page;
    @Inject
    protected Console console;
    @Inject
    protected CrudOperations crud;

    public AbstractOtherSettingsTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        page.navigate();
    }

}
