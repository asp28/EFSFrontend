package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {

    private Long id;
    private String fileName;
    private String dateUploaded;
    private String dateModified;
    private boolean fileUploaded;

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", dateUploaded='" + dateUploaded + '\'' +
                ", dateModified='" + dateModified + '\'' +
                ", fileUploaded=" + fileUploaded +
                '}';
    }

    public Date convertToDate(String date) throws ParseException {
        Date date1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(date);
        return date1;
    }
}
