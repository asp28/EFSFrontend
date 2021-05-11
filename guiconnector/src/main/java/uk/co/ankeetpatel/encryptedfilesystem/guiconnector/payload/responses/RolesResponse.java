package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolesResponse {

    private ArrayList<String> roles;
}
