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

import java.util.Map;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;

/**
 *
 * @author NoPassword
 */
public class AuthHelperTest {

    @Test
    public void makeValidAuthRequest() {
        Map<String, String> request
                = AuthHelper.makeAuthRequest("user@example.com", "00000000-0000-0000-0000-000000000000");

        Assert.assertNotEquals(request.get(Constants.USERNAME).length(), 0);
        Assert.assertEquals(request.get(Constants.API_KEY).length(), 36);
        Assert.assertNotNull(request.get(Constants.BROWSER_ID));
        Assert.assertNotNull(request.get(Constants.DEVICE_NAME));
    }

    @Test
    public void makeInvalidAuthRequest() {
        ThrowingRunnable tr = () -> {
            AuthHelper.makeAuthRequest("user.com", "00000000-0000-0000-0000-000000000000");
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);

        tr = () -> {
            AuthHelper.makeAuthRequest("user@example.com", "");
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);
        

        tr = () -> {
            AuthHelper.makeAuthRequest(null, null);
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);
    }

}
