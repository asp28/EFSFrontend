package uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.File;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.Role;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.FileResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.FilesResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.UserDetails;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util.CipherUtility;

import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

@Component
public class DataStorage {

    private KeyPair keys;

    private CipherUtility cipherUtility = CipherUtility.getInstance();

    private UserDetails userDetails;

    private FilesResponse filesResponse;

    private FileResponse fileToAccess;

    public DataStorage() throws NoSuchAlgorithmException {
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public FilesResponse getFilesResponse() {
        return filesResponse;
    }

    public void setFilesResponse(FilesResponse filesResponse) {
        this.filesResponse = filesResponse;
    }

    public FileResponse getFileToAccess() {
        return fileToAccess;
    }

    public void setFileToAccess(FileResponse fileToAccess) {
        this.fileToAccess = fileToAccess;
    }

    public KeyPair getKeys() {
        return keys;
    }

    public void setKeys(KeyPair keys) {
        this.keys = keys;
    }

    public CipherUtility getCipherUtility() {
        return cipherUtility;
    }


    public ArrayList<String> getRolesAsStrings() {
        ArrayList<String> roles = new ArrayList<>();
        for (Role r : userDetails.getRoles()) {
            roles.add(r.getAuthority());
        }
        return roles;
    }
}
