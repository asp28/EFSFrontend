package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class fileconverter {
    private java.io.File file;
    private String filePath;

    public fileconverter(String filePath) {
        this.filePath = filePath;
        file = new File(filePath);
    }

    public fileconverter(File file) {
        this.file = file;
    }


    public byte[] convertFile() {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = fis.readAllBytes();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static void rebuildFile(byte[] bytes, String pathName) {
        try (FileOutputStream fos = new FileOutputStream(pathName)) {
            fos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
