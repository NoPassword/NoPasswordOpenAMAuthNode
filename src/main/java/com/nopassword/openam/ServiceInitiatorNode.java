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
package com.nopassword.openam;

import com.google.inject.assistedinject.Assisted;
import com.nopassword.openam.AuthHelper.AuthStatus;
import com.sun.identity.idm.AMIdentity;
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
//    public static final String ASYNC_AUTH_URL = AuthHelper.BASE_URL + "/Auth/LoginAsync";
    public static final String ENC_ASYNC_AUTH_URL = AuthHelper.BASE_URL + "/v2/ID/Login/Async";
    public static final String ASYNC_LOGIN_TOKEN = "AsyncLoginToken";
    public static final String AES_KEY = "aesKey";
    public static final String AES_IV = "aesIV";

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
        String email;
        String username = context.sharedState.get(USERNAME).asString();
        AMIdentity userIdentity
                = coreWrapper.getIdentity(
                        username,
                        context.sharedState.get(REALM).asString());

        if (userIdentity == null) {
            DEBUG.error("user not found: " + username);
            return goTo(false).build();
        }

        try {
            Set<String> a = new HashSet<>();
            a.add("mail");
            a.add("email");
            Map attrs = userIdentity.getAttributes(a);
            HashSet<String> emailSet = (HashSet) attrs.get("mail");

            if (!emailSet.isEmpty()) {
                email = emailSet.iterator().next();
            } else {
                emailSet = (HashSet) attrs.get("email");

                if (emailSet.isEmpty()) {
                    DEBUG.error("user email not found: " + username);
                    return goTo(false).build();
                }

                email = emailSet.iterator().next();
            }
        } catch (Exception ex) {
            DEBUG.error("An error ocurred when getting user email: " + username, ex);
            return goTo(false).build();
        }

        Map<String, Object> result = AuthHelper.authenticateUser(email, config.authEndpoint(), config.noPasswordLoginKey());
        DEBUG.message(result.toString());
        String status = (String) result.get(Constants.AUTH_STATUS);
        if (AuthStatus.WaitingForResponse.name().equals(status)) {
            context.sharedState.add(ASYNC_LOGIN_TOKEN, (String) result.get(Constants.ASYNC_LOGIN_TOKEN));
            return goTo(true).build();
        } else {
            return goTo(false).build();
        }
    }

}
