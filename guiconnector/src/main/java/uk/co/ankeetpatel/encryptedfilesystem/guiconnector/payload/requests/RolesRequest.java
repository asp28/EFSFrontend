package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests;

import java.util.HashMap;

public class RolesRequest {

    private String username;

    private HashMap<String, String> roles;

    public RolesRequest(String username, HashMap<String, String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, String> getRoles() {
        return roles;
    }

    public void setRoles(HashMap<String, String> roles) {
        this.roles = roles;
    }
}
