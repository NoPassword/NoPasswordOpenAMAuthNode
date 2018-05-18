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
 * Copyright 2011-2017 ForgeRock AS. All Rights Reserved
 */
/**
 * Portions Copyright 2018 Wiacts Inc.
 */
package com.nopassword.openam;

import com.google.inject.assistedinject.Assisted;
import static com.nopassword.openam.AuthHelper.AuthStatus;
import com.sun.identity.shared.debug.Debug;
import javax.inject.Inject;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;

/**
 *
 * @author NoPassword
 */
@Node.Metadata(outcomeProvider = NoPasswordDecisionNode.OutcomeProvider.class,
        configClass = ServiceDecisionNode.Config.class)
public class ServiceDecisionNode extends NoPasswordDecisionNode {

//    public static final String CHECK_LOGIN_TOKEN_URL = AuthHelper.BASE_URL + "/Auth/CheckLoginToken";
    private static final String DEBUG_FILE_NAME = ServiceDecisionNode.class.getSimpleName();
    private final Debug DEBUG = Debug.getInstance(DEBUG_FILE_NAME);
    private final ServiceDecisionNode.Config config;
    private final CoreWrapper coreWrapper;

    /**
     * Configuration for the node.
     */
    public interface Config {
        
        @Attribute(order = 100)
        String loginTokenEndpoint();

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
    public ServiceDecisionNode(@Assisted ServiceDecisionNode.Config config, CoreWrapper coreWrapper) throws NodeProcessException {
        this.config = config;
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) {
        String loginToken = context.sharedState.get(ServiceInitiatorNode.ASYNC_LOGIN_TOKEN).asString();
        AuthStatus status = AuthHelper.checkLoginToken(loginToken, config.loginTokenEndpoint());

        switch (status) {
            case WaitingForResponse:
                return goTo(UNANSWERED_OUTCOME);
            case Success:
                return goTo(TRUE_OUTCOME);
            default:
                return goTo(FALSE_OUTCOME);
        }
    }

}
