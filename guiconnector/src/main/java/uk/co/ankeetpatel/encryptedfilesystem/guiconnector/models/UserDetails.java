package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {

    private String accessToken;
    private String tokenType;
    private String username;
    private List<Role> roles;


    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUsername() {
        return username;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                '}';
    }
}
