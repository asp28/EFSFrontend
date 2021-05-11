package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.File;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private File file;

    private ArrayList<Integer> permissions;
}
