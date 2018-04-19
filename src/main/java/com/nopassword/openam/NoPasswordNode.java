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

import com.nopassword.openam.utils.AuthHelper;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.authentication.callbacks.PollingWaitCallback;
import org.forgerock.openam.core.CoreWrapper;
import org.forgerock.openam.utils.Time;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.forgerock.json.JsonValue;

import static org.forgerock.openam.auth.node.api.Action.send;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

/**
 * An authentication node integrating with iProov face recognition solution.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = NoPasswordNode.Config.class)
public class NoPasswordNode extends AbstractDecisionNode {

    /**
     * Configuration for the node.
     */
    public interface Config {

        @Attribute(order = 100)
        String genericAPIkey();

    }

    private final Config config;
    private final CoreWrapper coreWrapper;
    private final static String DEBUG_FILE_NAME = "NoPasswordNode";
    protected Debug DEBUG = Debug.getInstance(DEBUG_FILE_NAME);
    private static final String TIMEOUT = "60000";
    private static final String TIMEWAIT = "2000";
    private static final String START = "start";
    private static final String FUTURE = "future";
    private static final String AUTH_URL = "https://api.nopassword.com/v2/ID";

    /**
     * Guice constructor.
     *
     * @param config The node configuration.
     * @param coreWrapper
     * @throws NodeProcessException If there is an error reading the
     * configuration.
     */
    @Inject
    public NoPasswordNode(@Assisted Config config, CoreWrapper coreWrapper) throws NodeProcessException {
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
            DEBUG.message("user not found: " + username);
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
                    DEBUG.message("user email not found: " + username);
                    return goTo(false).build();
                }

                email = emailSet.iterator().next();
            }
        } catch (Exception ex) {
            DEBUG.error("An error ocurred when getting user email: " + username, ex);
            return goTo(false).build();
        }

        if (AuthHelper.authenticateUser(email, AUTH_URL, config.genericAPIkey())) {
            return goTo(true).build();
        } else {
            return goTo(false).build();
        }
    }
}
