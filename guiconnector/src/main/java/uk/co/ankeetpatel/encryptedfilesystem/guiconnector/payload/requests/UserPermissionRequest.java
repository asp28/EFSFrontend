package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests;

public class UserPermissionRequest {

    private String username;

    private long fileID;

    public UserPermissionRequest(String username, long fileID) {
        this.username = username;
        this.fileID = fileID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getFileID() {
        return fileID;
    }

    public void setFileID(long fileID) {
        this.fileID = fileID;
    }
}
