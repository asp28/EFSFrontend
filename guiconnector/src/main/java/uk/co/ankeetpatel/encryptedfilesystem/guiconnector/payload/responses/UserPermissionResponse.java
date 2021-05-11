package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {

    private String username;

    private String filename;

    private List<Integer> permissionValues;

    private long fileID;

    private long userID;

}
