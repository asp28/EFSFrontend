package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests;

public class UploadRequestBody {

    private String fileName;

    public UploadRequestBody(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
