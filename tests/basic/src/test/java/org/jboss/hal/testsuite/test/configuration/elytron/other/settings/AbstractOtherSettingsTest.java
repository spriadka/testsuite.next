package org.jboss.hal.testsuite.test.configuration.elytron.other.settings;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
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
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PORT;
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

        // SSL

        // a realm is required for new security-domain

        operations.add(constantPrincipalTransformerAddress(CONS_PRI_TRANS_UPDATE), Values.of(CONSTANT, ANY_STRING));

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

        operations.add(fileAuditLogAddress(FILE_LOG_DELETE), Values.of(PATH, ANY_STRING));
        operations.add(fileAuditLogAddress(FILE_LOG_UPDATE), Values.of(PATH, ANY_STRING));
        operations.add(fileAuditLogAddress(FILE_LOG_TRY_UPDATE), Values.of(PATH, ANY_STRING));

        Values params = Values.of(PATH, ANY_STRING).and(SUFFIX, SUFFIX_LOG);
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_DELETE), params);
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE), params);
        operations.add(periodicRotatingFileAuditLogAddress(PER_LOG_TRY_UPDATE), params);

        operations.add(sizeRotatingFileAuditLogAddress(SIZ_LOG_DELETE), Values.of(PATH, ANY_STRING));
        operations.add(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE), Values.of(PATH, ANY_STRING));

        Values syslogParams = Values.of(HOSTNAME, ANY_STRING).and(PORT, Random.number()).and(SERVER_ADDRESS, LOCALHOST);
        operations.add(syslogAuditLogAddress(SYS_LOG_UPDATE), syslogParams);
        operations.add(syslogAuditLogAddress(SYS_LOG_TRY_UPDATE), syslogParams);
        operations.add(syslogAuditLogAddress(SYS_LOG_DELETE), syslogParams);

        Values secEventParams = Values.ofList(SECURITY_EVENT_LISTENERS, SYS_LOG_UPDATE, SIZ_LOG_UPDATE);
        operations.add(aggregateSecurityEventListenerAddress(AGG_SEC_UPDATE), secEventParams);
        operations.add(aggregateSecurityEventListenerAddress(AGG_SEC_DELETE), secEventParams);
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
            operations.removeIfExists(fileAuditLogAddress(FILE_LOG_DELETE));
            operations.removeIfExists(fileAuditLogAddress(FILE_LOG_UPDATE));
            operations.removeIfExists(fileAuditLogAddress(FILE_LOG_TRY_UPDATE));
            operations.removeIfExists(fileAuditLogAddress(FILE_LOG_CREATE));
            // removeIfExists() the aggregate-security-event-listener first, as they require size audit log and syslog
            operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_UPDATE));
            operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_CREATE));
            operations.removeIfExists(aggregateSecurityEventListenerAddress(AGG_SEC_DELETE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_UPDATE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_TRY_UPDATE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_DELETE));
            operations.removeIfExists(periodicRotatingFileAuditLogAddress(PER_LOG_CREATE));
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_DELETE));
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_UPDATE));
            operations.removeIfExists(sizeRotatingFileAuditLogAddress(SIZ_LOG_CREATE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_DELETE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_CREATE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_UPDATE));
            operations.removeIfExists(syslogAuditLogAddress(SYS_LOG_TRY_UPDATE));
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
