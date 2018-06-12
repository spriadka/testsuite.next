package org.jboss.hal.testsuite;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authentication {

    private static Map<WebDriver, Boolean> loginMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Console.class);
    private boolean authenticated = false;

    private WebDriver browser;

    @ArquillianResource
    private URL url;

    public Authentication with(WebDriver browser) {
        this.browser = browser;
        if (loginMap.containsKey(browser)) {
            authenticated = loginMap.get(browser);
        } else {
            loginMap.put(browser, authenticated);
        }
        return this;
    }

    public void authenticate(String username, String password) {
        if (authenticated) {
            log.debug("# Already Logged in. Trying to Logout");
            logout();
        }
        log.debug("# Trying to authenticate using following credentials");
        log.debug("# username: " + username);
        log.debug("# password: " + password);

        String authUrl = url.getHost() + ":" + url.getPort() + "/management/";
        String protocol = "http";
        browser.get(protocol + "://" + username + ":" + password + "@" + authUrl);
        authenticated = true;
        loginMap.replace(browser, true);
    }

    public void logout() {
        authenticated = false;
        loginMap.replace(browser, false);
    }

    public void authenticate(RbacRole role) {
        authenticate(role.username, role.password);
    }
}
