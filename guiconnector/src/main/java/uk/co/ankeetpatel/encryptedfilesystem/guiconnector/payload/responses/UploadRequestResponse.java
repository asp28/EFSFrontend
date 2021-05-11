package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequestResponse {

    private String publicKey;
    private Long id;
}
