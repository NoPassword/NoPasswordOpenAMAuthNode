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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.ResourceBundle;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome;
import org.forgerock.util.i18n.PreferredLocales;

/**
 *
 * @author NoPassword
 */
public abstract class NoPasswordDecisionNode implements Node {

    private static final String BUNDLE = NoPasswordDecisionNode.class.getName().replace(".", "/");
    protected static final String TRUE_OUTCOME = "true";
    protected static final String FALSE_OUTCOME = "false";
    protected static final String UNANSWERED_OUTCOME = "unanswered";

    public static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue jsonValue) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, NoPasswordDecisionNode.class.getClassLoader());

            return ImmutableList.of(
                    new Outcome(TRUE_OUTCOME, bundle.getString(TRUE_OUTCOME)),
                    new Outcome(FALSE_OUTCOME, bundle.getString(FALSE_OUTCOME)),
                    new Outcome(UNANSWERED_OUTCOME, bundle.getString(UNANSWERED_OUTCOME))
            );
        }
    }

    protected Action goTo(String action) {
        return Action.goTo(action).build();
    }

}
