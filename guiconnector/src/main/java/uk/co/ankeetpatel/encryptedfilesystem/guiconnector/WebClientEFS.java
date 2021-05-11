package uk.co.ankeetpatel.encryptedfilesystem.guiconnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.GenericServerSideException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.InvalidCredentialsException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.NotFoundException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.UnauthorizedException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.File;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.UserDetails;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.*;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.*;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util.CipherUtility;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util.fileconverter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;


public class WebClientEFS {

    WebClient webClient;

    CipherUtility cipherUtility = CipherUtility.getInstance();

    public WebClientEFS(WebClient webClient) throws NoSuchAlgorithmException {
        this.webClient = webClient;
    }

    public Mono<UserDetails> login(String username, String password) {
        Mono<UserDetails> userDetails = webClient.post()
                .uri("/api/auth/signin")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(new login(username, password)))
                .retrieve().bodyToMono(UserDetails.class)
                .onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });

        return userDetails;
    }

    public Mono<FilesResponse> files(UserDetails userDetails) {
        Mono<FilesResponse> files = webClient.get().uri("/api/files/all")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userDetails.getTokenType() + " " + userDetails.getAccessToken())
                .retrieve().bodyToMono(FilesResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return files;
    }

    public Mono<FileResponse> file(UserDetails userDetails, Long id) {
        Mono<FileResponse> file = webClient.get().uri("/api/files/" + id)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userDetails.getTokenType() + " " + userDetails.getAccessToken())
                .retrieve().bodyToMono(FileResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return file;
    }

    public UploadResponse uploadFile(UserDetails userDetails, java.io.File file) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {

        UploadRequestResponse uploadRequestResponse = webClient.post().uri("/api/files/upload/request")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userDetails.getTokenType() + " " + userDetails.getAccessToken())
                .body(BodyInserters.fromValue(new UploadRequestBody(file.getName())))
                .retrieve().bodyToMono(UploadRequestResponse.class).block();

        fileconverter fc = new fileconverter(file);
        ArrayList<byte[]> bytes = cipherUtility.encryption(fc.convertFile(), cipherUtility.decodePublicKey(uploadRequestResponse.getPublicKey()));

        UploadResponse uploadResponse = webClient.post().uri("/api/files/upload")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userDetails.getTokenType() + " " + userDetails.getAccessToken())
                .body(BodyInserters.fromValue(new UploadBody(uploadRequestResponse.getId(), file.getName(), bytes)))
                .retrieve().bodyToMono(UploadResponse.class).block();

        return uploadResponse;
    }

    public Mono<DownloadResponse> downloadFile(UserDetails userDetails, Long id, KeyPair keys) {

        String publicKey = cipherUtility.encodeKey(keys.getPublic());

        Mono<DownloadResponse> downloadResponse = webClient.post().uri("/api/files/download")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, userDetails.getTokenType() + " " + userDetails.getAccessToken())
                .body(BodyInserters.fromValue(new DownloadRequest(id, publicKey)))
                .retrieve().bodyToMono(DownloadResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });

        return downloadResponse;
    }

    public Mono<MessageResponse> deleteFile(UserDetails details, long fileID) {
        Mono<MessageResponse> response = webClient.delete().uri("/api/files/delete/" + fileID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, details.getTokenType() + " " + details.getAccessToken())
                .retrieve().bodyToMono(MessageResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return response;
    }

    public Mono<UserPermissionResponse> getUserPermissions(UserDetails details, UserPermissionRequest userPermissionRequest) {
        Mono<UserPermissionResponse> response = webClient.post().uri("/api/files/userpermissions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, details.getTokenType() + " " + details.getAccessToken())
                .body(BodyInserters.fromValue(userPermissionRequest))
                .retrieve().bodyToMono(UserPermissionResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return response;
    }

    public Mono<RolesResponse> getUserRoles(UserDetails details, String username) {
        Mono<RolesResponse> response = webClient.get().uri("/api/users/" + username + "/roles")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, details.getTokenType() + " " + details.getAccessToken())
                .retrieve().bodyToMono(RolesResponse.class).onErrorResume(e -> {
                    System.out.println(e.getLocalizedMessage());
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    } else if (e.getLocalizedMessage().contains("404")) {
                        return Mono.error(new NotFoundException(HttpStatus.NOT_FOUND, "Not Found", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return response;
    }

    public Mono<MessageResponse> setUserRoles(UserDetails details, RolesRequest rolesRequest) {
        Mono<MessageResponse> response = webClient.post().uri("/api/users/roles")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, details.getTokenType() + " " + details.getAccessToken())
                .body(BodyInserters.fromValue(rolesRequest)).retrieve()
                .bodyToMono(MessageResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return response;
    }

    public Mono<MessageResponse> updatePermissions(UserDetails details, PermissionRequest permissionRequest) {
        Mono<MessageResponse> response = webClient.post().uri("/api/files/updatepermission")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, details.getTokenType() + " " + details.getAccessToken())
                .body(BodyInserters.fromValue(permissionRequest))
                .retrieve().bodyToMono(MessageResponse.class).onErrorResume(e -> {
                    if(e.getLocalizedMessage().contains("Connection refused")) {
                        return Mono.error(new GenericServerSideException(HttpStatus.SERVICE_UNAVAILABLE, "Connection refused.", e));
                    } else if (e.getLocalizedMessage().contains("Forbidden")) {
                        return Mono.error(new UnauthorizedException(HttpStatus.FORBIDDEN, "Forbidden", e));
                    }
                    return Mono.error(new InvalidCredentialsException(HttpStatus.UNAUTHORIZED, "Username/Password is invalid.", e));
                });
        return response;
    }
}
