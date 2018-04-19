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
package com.nopassword.openam.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.identity.shared.debug.Debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author NoPassword
 */
public class AuthHelper {

    private final static Debug DEBUG = Debug.getInstance("AuthHelper");
    private static final Pattern API_KEY_REGEX = Pattern.compile("[a-z0-9-]{36,36}");

    /**
     * Pure JSE REST client
     *
     * @param <T>
     * @param url URL
     * @param o Data
     * @param resultType Class
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static <T> T doPost(String url, Object o, Class<T> resultType) throws MalformedURLException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(o);
        URL urlx = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlx.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream out = conn.getOutputStream();
        out.write(payload.getBytes());
        out.flush();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed: HTTP error code " + conn.getResponseCode());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder input = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            input.append(line);
        }
        conn.disconnect();
        return mapper.readValue(input.toString(), resultType);
    }

    public static boolean authenticateUser(String username, String authURL, String genericAPIKey) {
        try {
            Map request = makeAuthRequest(username, genericAPIKey);
            Map<String, String> response = doPost(authURL, request, Map.class);
            boolean result = Constants.SUCCESS.equals(response.get(Constants.AUTH_STATUS));
            if (!result) {
                DEBUG.message(
                        String.format("User authentication failed - %s: %s",
                                username, response.get(Constants.AUTH_STATUS)));
            }
            return result;
        } catch (IOException ex) {
            DEBUG.message("Error authenticating user: " + username, ex);
            return false;
        } catch(IllegalArgumentException ex) {
            DEBUG.message("invalid authentication request", ex);
            return false;
        }
    }

    public static Map makeAuthRequest(String username, String apiKey) {
        if (!isValidUsername(username)) {
            DEBUG.message("Invalid user: " + username);
            throw new IllegalArgumentException("invalid user");
        }
        
        if(!isValidAPIKey(apiKey)) {
            throw new IllegalArgumentException("invalid API key");
        }

        Map<String, String> request = new HashMap();
        request.put(Constants.API_KEY, apiKey);
        request.put(Constants.USERNAME, username);
        request.put(Constants.COMMAND, Constants.USER_STATUS);
        return request;
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < 3) {
            return false;
        }

        return username.indexOf('@') >= 1;
    }
    
    public static boolean isValidAPIKey(String apiKey){
        return apiKey == null ? false : API_KEY_REGEX.matcher(apiKey).matches();
    }

}
