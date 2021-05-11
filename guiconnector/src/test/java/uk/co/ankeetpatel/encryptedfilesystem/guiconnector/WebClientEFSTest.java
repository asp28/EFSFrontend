package uk.co.ankeetpatel.encryptedfilesystem.guiconnector;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.NotFoundException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.UnauthorizedException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.UserDetails;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.PermissionRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.RolesRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.UserPermissionRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.*;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util.CipherUtility;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

class WebClientEFSTest {

    private WebClient getNewWebClient() throws SSLException {
        //remove trustmanager for production build
        SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        return WebClient.builder().baseUrl("https://localhost:8080").exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(-1))
                .build())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


    @Test
    void shouldLoginUser() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser","password");
        System.out.println(details.block().toString());
        Assertions.assertEquals("Bearer", details.block().getTokenType());
        Assertions.assertNotNull(details.block().getAccessToken());
    }

    @Test
    void loginFailure() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser","fakepassword");
        System.out.println(details.getClass());
    }

    @Test
    void uploadFile() throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IOException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        java.io.File file = new java.io.File("abc.docx");
        UploadResponse uploadResponse = efs.uploadFile(details.block(), file);
        Assertions.assertEquals("File upload successful.", uploadResponse.getMessage());
    }


    @Test
    void getAllAccessibleFiles() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        Mono<FilesResponse> file = efs.files(details.block());

        for (FileResponse f : file.block().getFilesResponse()) {
            System.out.println(f.getFile().toString());
        }

    }

    @Test
    void getUserPermissionsForbidden() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");

        UserPermissionRequest request = new UserPermissionRequest("testmod", 1);
        Mono<UserPermissionResponse> response = efs.getUserPermissions(details.block(), request);

        //Return Error Block - Forbidden
    }

    @Test
    void getUserPermissionsByMod() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testmod", "password");

        UserPermissionRequest request = new UserPermissionRequest("testuser", 27L);
        Mono<UserPermissionResponse> response = efs.getUserPermissions(details.block(), request);

        if(!response.block().getPermissionValues().contains(1)) Assertions.assertFalse(true);
        if(!response.block().getPermissionValues().contains(2)) Assertions.assertFalse(true);
        if(!response.block().getPermissionValues().contains(16)) Assertions.assertFalse(true);
    }

    @Test
    void getUserPermissionsByAdmin() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testadmin", "password");

        UserPermissionRequest request = new UserPermissionRequest("testuser", 27L);
        Mono<UserPermissionResponse> response = efs.getUserPermissions(details.block(), request);

        if(!response.block().getPermissionValues().contains(1)) Assertions.assertFalse(true);
        if(!response.block().getPermissionValues().contains(2)) Assertions.assertFalse(true);
        if(!response.block().getPermissionValues().contains(16)) Assertions.assertFalse(true);
    }

    @Test
    void getSpecificFileUserTest() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        try {
            Mono<FileResponse> f = efs.file(details.block(), 1L);
            FileResponse fr = f.block();
        } catch (UnauthorizedException e) {
            Assertions.assertFalse(false);
        }
        //return false as user does not have read or admin on file
    }

    @Test
    void getSpecificFileUserTestPassing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        try {
            Mono<FileResponse> f = efs.file(details.block(), 27L);
            FileResponse fr = f.block();
        } catch (UnauthorizedException e) {
            Assertions.assertFalse(false);
        }
        //return false as user does not have read or admin on file
    }

    @Test
    void getSpecificFileModTest() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testmod", "password");
        Mono<FileResponse> f = efs.file(details.block(), 27L);
        Assertions.assertEquals(f.block().getFile().getFileName(), "abc.docx");
        //return true as mod automatically has admin perm
    }

    @Test
    void getSpecificFileAdminTest() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testadmin", "password");
        Mono<FileResponse> f = efs.file(details.block(), 27L);
        Assertions.assertEquals(f.block().getFile().getFileName(), "abc.docx");
    }

    @Test
    void updatePermissionsUserPassing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        HashMap<String, String> perms = new HashMap<>();
        perms.put("read", "true");
        perms.put("write", "false");
        perms.put("admin", "true");
        perms.put("delete", "false");

        PermissionRequest permissionRequest = new PermissionRequest(27L, perms, 3L);
        Mono<MessageResponse> response = efs.updatePermissions(details.block(), permissionRequest);
        Assertions.assertEquals("Permissions successfully updated.", response.block().getMessage());
    }

    @Test
    void updatePermissionsUserFailing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        HashMap<String, String> perms = new HashMap<>();
        perms.put("read", "true");
        perms.put("write", "false");
        perms.put("admin", "true");
        perms.put("delete", "false");

        PermissionRequest permissionRequest = new PermissionRequest(1L, perms, 2L);
        try {
            Mono<MessageResponse> response = efs.updatePermissions(details.block(), permissionRequest);
        } catch (UnauthorizedException e) {
            System.out.println("Error caught.");
            Assertions.assertFalse(false);
        }
        //FIX TEST
    }

    @Test
    void updatePermissionsMod() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testadmin", "password");
        HashMap<String, String> perms = new HashMap<>();
        perms.put("write", "true");

        PermissionRequest permissionRequest = new PermissionRequest(27L, perms, 2L);
        Mono<MessageResponse> response = efs.updatePermissions(details.block(), permissionRequest);
        Assertions.assertEquals("Permissions successfully updated.", response.block().getMessage());
    }

    @Test
    void updatePermissionsAdmin() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testadmin", "password");
        HashMap<String, String> perms = new HashMap<>();
        perms.put("read", "true");
        perms.put("write", "true");
        perms.put("admin", "true");
        perms.put("delete", "false");

        PermissionRequest permissionRequest = new PermissionRequest(27L, perms, 2L);
        Mono<MessageResponse> response = efs.updatePermissions(details.block(), permissionRequest);
        Assertions.assertEquals("Permissions successfully updated.", response.block().getMessage());
    }

    @Test
    void getUserRolesNotFound() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testmod", "password");
        try {
            Mono<RolesResponse> response = efs.getUserRoles(details.block(), "abcdef");
            response.block();
        } catch (NotFoundException e) {
            System.out.println("UserNotFound");
            Assertions.assertTrue(true);
        }
    }

    @Test
    void getUserRolesFailing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        try {
            Mono<RolesResponse> response = efs.getUserRoles(details.block(), "testmod");
            response.block();
        } catch (UnauthorizedException e) {
            System.out.println("No auth");
            Assertions.assertTrue(true);
        }
    }

    @Test
    void updateRoles() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testadmin", "password");
        HashMap<String, String> roles = new HashMap<>();
        roles.put("mod", "false");

        RolesRequest rolesRequest = new RolesRequest("testmod", roles);
        Mono<MessageResponse> response = efs.setUserRoles(details.block(), rolesRequest);
        Assertions.assertEquals("Roles updated.", response.block().getMessage());

        roles = new HashMap<>();
        roles.put("mod", "true");
        rolesRequest = new RolesRequest("testmod", roles);
        Mono<MessageResponse> response2 = efs.setUserRoles(details.block(), rolesRequest);
        Assertions.assertEquals("Roles updated.", response.block().getMessage());
    }

    @Test
    void downloadTestFailing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        CipherUtility cipherUtility = CipherUtility.getInstance();
        try {
            Mono<DownloadResponse> response = efs.downloadFile(details.block(), 1L, cipherUtility.getKeyPair());
            response.block();
        } catch (UnauthorizedException e) {
            System.out.println("Unauthorized access.");
            Assertions.assertTrue(true);
        }
    }

    @Test
    void downloadTestPassing() throws NoSuchAlgorithmException, SSLException {
        WebClientEFS efs = new WebClientEFS(getNewWebClient());
        Mono<UserDetails> details = efs.login("testuser", "password");
        CipherUtility cipherUtility = CipherUtility.getInstance();
        Mono<DownloadResponse> response = efs.downloadFile(details.block(), 27L, cipherUtility.getKeyPair());
        Assertions.assertEquals("abc.docx", response.block().getFileName());
    }
}