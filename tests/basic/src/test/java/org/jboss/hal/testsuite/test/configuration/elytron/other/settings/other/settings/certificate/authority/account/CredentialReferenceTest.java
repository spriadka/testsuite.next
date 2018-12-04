package org.jboss.hal.testsuite.test.configuration.elytron.other.settings.other.settings.certificate.authority.account;

import java.io.IOException;
import java.util.function.Consumer;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
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

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCATION;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.ANY_STRING;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CRED_ST_DELETE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.CRED_ST_UPDATE;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.credentialStoreAddress;

@RunWith(Arquillian.class)
public class CredentialReferenceTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    private static final String KEY_STORE = "key-store-with-path-" + Random.name();

    private static final String
        CERTIFICATE_AUTHORITY_ACCOUNT_WITH_CREDENTIAL_REFERENCE =
        "certificate-authority-account-with-credential-reference" + Random.name();
    private static final String CERTIFICATE_AUTHORITY_ACCOUNT_WITHOUT_CREDENTIAL_REFERENCE =
        "certificate-authority-account-without-credential-reference-" + Random.name();
    private static final String
        CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT =
        "certificate-authority-account-with-credential-reference-to-be-edited-" + Random.name();

    @BeforeClass
    public static void initResources() throws IOException {
        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values credParams = Values.of(CREATE, true).and(CREDENTIAL_REFERENCE, credRef).and(LOCATION, ANY_STRING);
        operations.add(credentialStoreAddress(CRED_ST_UPDATE), credParams).assertSuccess();
        operations.add(credentialStoreAddress(CRED_ST_DELETE), credParams).assertSuccess();
        ModelNode credentialReference = new ModelNode();
        credentialReference.get("clear-text").set(Random.name());
        operations.add(ElytronFixtures.keyStoreAddress(KEY_STORE),
            Values.of(ElytronFixtures.CREDENTIAL_REFERENCE_TYPE, "JKS")
                .and(ElytronFixtures.CREDENTIAL_REFERENCE, credentialReference)
                .and("path", Random.name())
                .and("relative-to", "jboss.server.config.dir")).assertSuccess();
        operations.add(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_WITH_CREDENTIAL_REFERENCE),
            Values.of(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, Random.name())
                .and(ModelDescriptionConstants.KEY_STORE, KEY_STORE)
                .and(ElytronFixtures.CREDENTIAL_REFERENCE, new ModelNode().setEmptyObject())).assertSuccess();
        operations.add(ElytronFixtures.certificateAuthorityAccountAddress(
            CERTIFICATE_AUTHORITY_ACCOUNT_WITHOUT_CREDENTIAL_REFERENCE),
            Values.of(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, Random.name())
                .and(ModelDescriptionConstants.KEY_STORE, KEY_STORE)).assertSuccess();
        operations.add(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT),
            Values.of(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, Random.name())
                .and(ModelDescriptionConstants.KEY_STORE, KEY_STORE)
                .and(ElytronFixtures.CREDENTIAL_REFERENCE, new ModelNode().setEmptyObject())).assertSuccess();
    }

    @AfterClass
    public static void cleanUp() throws IOException, OperationException {
        operations.removeIfExists(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_WITH_CREDENTIAL_REFERENCE));
        operations.removeIfExists(
            ElytronFixtures.certificateAuthorityAccountAddress(
                CERTIFICATE_AUTHORITY_ACCOUNT_WITHOUT_CREDENTIAL_REFERENCE));
        operations.removeIfExists(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT));
        operations.removeIfExists(ElytronFixtures.keyStoreAddress(KEY_STORE));
        operations.removeIfExists(credentialStoreAddress(CRED_ST_DELETE));
        operations.removeIfExists(credentialStoreAddress(CRED_ST_UPDATE));
    }

    @Drone
    private WebDriver browser;

    @Page
    private ElytronOtherSettingsPage page;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crud;

    @Before
    public void navigateToCertificateAuthorityAccount() {
        page.navigate();
        console.verticalNavigation()
            .selectSecondary(ElytronFixtures.OTHER_ITEM, ElytronFixtures.CERTIFICATE_AUTHORITY_ACCOUNT_ITEM);
    }

    @Test
    public void create() throws Exception {
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_WITHOUT_CREDENTIAL_REFERENCE);
        crud.createSingleton(ElytronFixtures.certificateAuthorityAccountAddress(
            CERTIFICATE_AUTHORITY_ACCOUNT_WITHOUT_CREDENTIAL_REFERENCE),
            page.getCertificateAuthorityAccountCredentialReferenceForm(), null,
            resourceVerifier -> resourceVerifier.verifyAttribute(ElytronFixtures.CREDENTIAL_REFERENCE,
                new ModelNode().setEmptyObject()));
    }

    @Test
    public void remove() throws Exception {
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_WITH_CREDENTIAL_REFERENCE);
        crud.deleteSingleton(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_WITH_CREDENTIAL_REFERENCE),
            page.getCertificateAuthorityAccountCredentialReferenceForm(),
            resourceVerifier -> resourceVerifier.verifyAttributeIsUndefined(ElytronFixtures.CREDENTIAL_REFERENCE));
    }

    @Test
    public void editAlias() throws Exception {
        String alias = Random.name();
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT);
        crud.update(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT),
            page.getCertificateAuthorityAccountCredentialReferenceForm(), clearFields.andThen(formFragment -> {
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, alias);
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_STORE, ElytronFixtures.CRED_ST_UPDATE);
            }), resourceVerifier -> resourceVerifier.verifyAttribute("credential-reference.alias", alias));
    }

    private Consumer<FormFragment> clearFields = formFragment -> {
        formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, "");
        formFragment.text("clear-text", "");
        formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_STORE, "");
        formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_TYPE, "");
    };

    @Test
    public void editClearText() throws Exception {
        String clearText = Random.name();
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT);
        crud.update(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT),
            page.getCertificateAuthorityAccountCredentialReferenceForm(),
            clearFields.andThen(formFragment -> formFragment.text("clear-text", clearText)),
            resourceVerifier -> resourceVerifier.verifyAttribute("credential-reference.clear-text", clearText));
    }

    @Test
    public void editStore() throws Exception {
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT);
        crud.update(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT),
            page.getCertificateAuthorityAccountCredentialReferenceForm(), clearFields.andThen(formFragment -> {
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, Random.name());
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_STORE, ElytronFixtures.CRED_ST_DELETE);
            }), resourceVerifier -> resourceVerifier.verifyAttribute("credential-reference.store",
                ElytronFixtures.CRED_ST_DELETE));
    }

    @Test
    public void editType() throws Exception {
        String type = Random.name();
        page.getCertificateAuthorityAccountTable().select(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT);
        crud.update(
            ElytronFixtures.certificateAuthorityAccountAddress(CERTIFICATE_AUTHORITY_ACCOUNT_CREDENTIAL_REFERENCE_EDIT),
            page.getCertificateAuthorityAccountCredentialReferenceForm(), clearFields.andThen(formFragment -> {
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_ALIAS, Random.name());
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_STORE, ElytronFixtures.CRED_ST_UPDATE);
                formFragment.text(ElytronFixtures.CREDENTIAL_REFERENCE_TYPE, type);
            }), resourceVerifier -> resourceVerifier.verifyAttribute("credential-reference.type", type));
    }
}
