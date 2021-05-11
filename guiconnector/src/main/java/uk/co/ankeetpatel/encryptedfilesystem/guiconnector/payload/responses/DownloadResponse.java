package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.File;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponse {

    private Long id;
    private ArrayList<byte[]> file;
    private String fileName;


}
