package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests;

public class DownloadRequest {

    private Long id;
    private String publicKey;

    public DownloadRequest(Long id, String publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
