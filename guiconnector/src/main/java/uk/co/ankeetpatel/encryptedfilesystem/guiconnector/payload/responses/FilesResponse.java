package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.File;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilesResponse {

    private ArrayList<FileResponse> filesResponse;

}
