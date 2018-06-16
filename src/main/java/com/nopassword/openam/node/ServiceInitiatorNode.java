/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017 ForgeRock AS.
 */
/**
 * Portions Copyright 2018 Wiacts Inc.
 */
package com.nopassword.openam.node;

import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOException;
import com.nopassword.openam.node.AuthHelper.AuthStatus;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

/**
 * An authentication node integrating with iProov face recognition solution.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = ServiceInitiatorNode.Config.class)
public class ServiceInitiatorNode extends AbstractDecisionNode {

    private final Config config;
    private final CoreWrapper coreWrapper;
    private final static String DEBUG_FILE_NAME = ServiceInitiatorNode.class.getSimpleName();
    private final Debug DEBUG = Debug.getInstance(DEBUG_FILE_NAME);
    public static final String ASYNC_LOGIN_TOKEN = "AsyncLoginToken";

    /**
     * Configuration for the node.
     */
    public interface Config {

        @Attribute(order = 100)
        String noPasswordLoginKey();

        @Attribute(order = 200)
        String authEndpoint();

    }

    /**
     * Guice constructor.
     *
     * @param config The node configuration.
     * @param coreWrapper
     * @throws NodeProcessException If there is an error reading the
     * configuration.
     */
    @Inject
    public ServiceInitiatorNode(@Assisted Config config, CoreWrapper coreWrapper) throws NodeProcessException {
        this.config = config;
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        String username = context.sharedState.get(USERNAME).asString();
        AMIdentity userIdentity
                = coreWrapper.getIdentity(
                        username,
                        context.sharedState.get(REALM).asString());

        if (userIdentity == null) {
            DEBUG.error("user not found: " + username);
            return goTo(false).build();
        }

        String email;
        try {
            email = getEmail(userIdentity);
        } catch (Exception ex) {
            DEBUG.error("Error retrieving user email", ex);
            return goTo(false).build();
        }

        DEBUG.message("email="+email);
        
        Map<String, Object> result = AuthHelper.authenticateUser(
                email, context.request.clientIp, config.authEndpoint(), config.noPasswordLoginKey());
        DEBUG.message(result.toString());
        String status = (String) result.get(Constants.AUTH_STATUS);
        if (AuthStatus.WaitingForResponse.name().equals(status)) {
            context.sharedState.add(ASYNC_LOGIN_TOKEN, (String) result.get(Constants.ASYNC_LOGIN_TOKEN));
            return goTo(true).build();
        } else {
            return goTo(false).build();
        }
    }

    private String getEmail(AMIdentity userIdentity) throws AuthLoginException, IdRepoException, SSOException {
        String email = "";
        Set<String> a = new HashSet<>();
        a.add("mail");
        a.add("email");
        Map attrs = userIdentity.getAttributes(a);
        HashSet<String> emailSet = (HashSet) attrs.get("mail");

        //check mail and email attributes
        if (!emailSet.isEmpty()) {
            email = emailSet.iterator().next();
        } else {
            emailSet = (HashSet) attrs.get("email");
            if (!emailSet.isEmpty()) {
                email = emailSet.iterator().next();
            }
        }

        //if both mail and email are empty, then get email from dn
        if (email == null || email.isEmpty()) {
            Set<String> dnSet = userIdentity.getAttribute("dn");
            email = getEmailFromDN(dnSet.iterator().next());    //userIdentity.getDn() return null!!!
        }
        return email;
    }

    private String getEmailFromDN(String dn) {
        if (dn == null || !dn.contains("dc=")) {
            return "";
        }

        String[] dc = dn.split(",dc=");
        int eqIdx = dn.indexOf('=');
        StringBuilder sb = new StringBuilder();
        sb.append(dn.substring(eqIdx + 1, dn.indexOf(',', eqIdx)))
                .append('@');

        for (int i = 1; i < dc.length; i++) {
            sb.append(dc[i]);

            if (i < dc.length - 1) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

}
