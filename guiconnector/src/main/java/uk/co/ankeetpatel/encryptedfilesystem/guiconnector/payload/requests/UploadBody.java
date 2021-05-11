package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests;

import java.util.ArrayList;

public class UploadBody {

    private Long id;
    private String fileName;
    private ArrayList<byte[]> bytes;

    public UploadBody(Long id, String fileName, ArrayList<byte[]> bytes) {
        this.id = id;
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<byte[]> getBytes() {
        return bytes;
    }

    public void setBytes(ArrayList<byte[]> bytes) {
        this.bytes = bytes;
    }
}
