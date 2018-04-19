package com.nopassword.openam;

import java.util.List;

/**
 *
 * @author NoPassword
 */
public class NoPasswordAuthCallback {

    private String authId;
    private String template;
    private String stage;
    private String header;
    private List<Callback> callbacks;

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<Callback> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    public void setUsername(final String username) {
        this.getCallbacks().get(0).setInputValue(username);
    }

    @Override
    public String toString() {
        return "NoPasswordAuthCallback{"
                + "authId='" + authId + '\''
                + ", template='" + template + '\''
                + ", stage='" + stage + '\''
                + ", header='" + header + '\''
                + ", callbacks=" + callbacks
                + '}';
    }
}
