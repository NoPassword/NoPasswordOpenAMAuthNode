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
 * Portions Copyright 2018 NoPassword Inc.
 */
package com.nopassword.openam.node;

import java.util.List;

/**
 *
 * @author NoPassword
 */
public class Callback {

    private String type;
    private List<NameValuePair> input;
    private List<NameValuePair> output;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NameValuePair> getInput() {
        return input;
    }

    public void setInput(List<NameValuePair> input) {
        this.input = input;
    }

    public List<NameValuePair> getOutput() {
        return output;
    }

    public void setOutput(List<NameValuePair> output) {
        this.output = output;
    }

    public void setInputValue(final String value) {
        input.get(0).setValue(value);
    }

    @Override
    public String toString() {
        return "Callback{" +
                "type='" + type + '\'' +
                ", input=" + input +
                ", output=" + output +
                '}';
    }
}