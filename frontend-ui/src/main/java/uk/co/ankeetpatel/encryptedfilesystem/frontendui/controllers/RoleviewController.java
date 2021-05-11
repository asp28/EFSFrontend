package uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DataStorage;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.StageInitializer;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.GenericServerSideException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.InvalidCredentialsException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.NotFoundException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.UnauthorizedException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.WebClientEFS;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.RolesRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.MessageResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.RolesResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Component
public class RoleviewController implements Initializable {

    @FXML
    public JFXButton rolesButton, ExitButton, HomeButton, logoutButton, uploadButton,
            updateButton, updateAndChooseAnotherButton, cancelButton, searchButton;

    @FXML
    public AnchorPane UserAnchorPane, rootAnchor;

    @FXML
    public StackPane rootPane;

    @FXML
    public JFXToggleButton moderatorToggle, administratorToggle;

    @FXML
    public JFXTextField SearchBar;

    @FXML
    public Text alterRolesText;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private WebClientEFS webClientEFS;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private StageInitializer stageInitializer;


    public RoleviewController(WebClientEFS webClientEFS) {
        this.webClientEFS = webClientEFS;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setRolesButton();
        setExitButton();
        setHomeButton();
        setLogoutButton();
        setPermissionsTabValues();
        setUploadButton();
    }

    private void setExitButton() {
        ExitButton.setOnAction(e-> {
            try {
                FileUtils.cleanDirectory(new File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            applicationContext.close();
            Platform.exit();
        });
    }

    private void setLogoutButton() {
        logoutButton.setOnAction(e-> {
            try {
                FileUtils.cleanDirectory(new File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            dataStorage.setUserDetails(null);
            stageInitializer.switchScene(stageInitializer.getLoginResource());
        });
    }

    private void setHomeButton() {
        HomeButton.setOnAction(e-> {
            stageInitializer.switchScene(stageInitializer.getAllFilesResource());
        });
    }

    private void setUploadButton() {
        uploadButton.setOnAction(e-> {
            //UPDATE
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a file to upload.");
            java.io.File file = chooser.showOpenDialog(uploadButton.getScene().getWindow());
            try {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton confirm = new JFXButton("Confirm");
                JFXButton cancel = new JFXButton("Cancel");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                cancel.setOnAction(ev -> {
                    dialog.close();
                });
                confirm.setOnAction(ev -> {
                    try {
                        webClientEFS.uploadFile(dataStorage.getUserDetails(), file);
                    } catch (InvalidKeySpecException | NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException invalidKeySpecException) {
                        invalidKeySpecException.printStackTrace();
                    }
                    System.out.println("File UPLOAD successful.");
                    stageInitializer.switchScene(stageInitializer.getAllFilesResource());
                    dialog.close();
                });
                dialogLayout.setHeading(new Label("Are you sure you want to upload: " + file.getName()));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(cancel);
                buttonList.add(confirm);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
                //Output that the upload was successful.
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                //Output that the upload failed.
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton cancel = new JFXButton("Ok");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                cancel.setOnAction(ev -> {
                    dialog.close();
                });
                dialogLayout.setHeading(new Label("Failed to upload file."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(cancel);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            }
        });
    }

    private void setRolesButton() {
        ArrayList<String> roles = dataStorage.getRolesAsStrings();
        if(roles.contains("ROLE_MODERATOR") || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPERADMIN")) {
            rolesButton.setOnAction(e -> {
                stageInitializer.switchScene(stageInitializer.getRolesResource());
            });
        } else {
            rolesButton.setDisable(true);
        }
    }

    public void setPermissionsTabValues() {
        UserAnchorPane.setDisable(true);

        searchButton.setOnAction(e -> {
            try {
                Mono<RolesResponse> responseMono = webClientEFS.getUserRoles(dataStorage.getUserDetails(), SearchBar.getText());

                UserAnchorPane.setDisable(false);
                for (String s : responseMono.block().getRoles()) {
                    System.out.println(s);
                    switch (s) {
                        case "ROLE_MODERATOR":
                            moderatorToggle.setSelected(true);
                            break;
                        case "ROLE_ADMIN":
                            administratorToggle.setSelected(true);
                            break;
                    }
                }
                alterRolesText.setText("Alter roles for: " + SearchBar.getText());
            }catch (NotFoundException ex) {
                UserAnchorPane.setDisable(true);
                moderatorToggle.setSelected(false);
                administratorToggle.setSelected(false);
                alterRolesText.setText("Alter roles for: ");
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton exit = new JFXButton("Confirm");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                exit.setOnAction(ev -> {
                    dialog.close();
                });
                dialogLayout.setHeading(new Label("User not found."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(exit);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            }catch (GenericServerSideException ex) {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton exit = new JFXButton("Exit");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                exit.setOnAction(ev -> {
                    try {
                        FileUtils.cleanDirectory(new java.io.File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    applicationContext.close();
                    Platform.exit();
                });
                dialogLayout.setHeading(new Label("Server Side connection refused. Click confirm to close application."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(exit);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            }catch (InvalidCredentialsException ex) {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton cancel = new JFXButton("Confirm");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                cancel.setOnAction(ev -> {
                    dialog.close();
                    dataStorage.setUserDetails(null);
                    stageInitializer.switchScene(stageInitializer.getLoginResource());
                });
                dialogLayout.setHeading(new Label("Login credentials no longer valid."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(cancel);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            }catch (UnauthorizedException ex) {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton cancel = new JFXButton("Confirm");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                cancel.setOnAction(ev -> {
                    dialog.close();
                    dataStorage.setUserDetails(null);
                    stageInitializer.switchScene(stageInitializer.getLoginResource());
                });
                dialogLayout.setHeading(new Label("Cannot access this resource. User logged out."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(cancel);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            }

        });

        updateButton.setOnAction(e -> {
            update();
            stageInitializer.switchScene(stageInitializer.getAllFilesResource());
        });
        updateAndChooseAnotherButton.setOnAction(e -> {
            update();
            UserAnchorPane.setDisable(true);
            alterRolesText.setText("Alter roles for: ");
            administratorToggle.setSelected(false);
            moderatorToggle.setSelected(false);
            SearchBar.setText("");
        });

        cancelButton.setOnAction(e -> {
            UserAnchorPane.setDisable(true);
            alterRolesText.setText("Alter roles for: ");
            moderatorToggle.setSelected(false);
            administratorToggle.setSelected(false);
            SearchBar.setText("");
        });

    }

    private HashMap<String, String> generateHashMap() {
        HashMap<String, String> permissions = new HashMap<>();
        if (moderatorToggle.isSelected() == true) {
            permissions.put("mod", "true");
        } else {
            permissions.put("mod", "false");
        }
        if (administratorToggle.isSelected() == true) {
            permissions.put("admin", "true");
        } else {
            permissions.put("admin", "false");
        }
        return permissions;
    }

    private void update() {
        HashMap<String, String> roles = generateHashMap();
        RolesRequest rolesRequest = new RolesRequest(SearchBar.getText(), roles);
        try {
            System.out.println(rolesRequest.getRoles() + " " + SearchBar.getText());
            System.out.println("ENTERED TRY");
            Mono<MessageResponse> a = webClientEFS.setUserRoles(dataStorage.getUserDetails(), rolesRequest);
            System.out.println(a.block().getMessage());
        }catch (GenericServerSideException e) {
            BoxBlur boxBlur = new BoxBlur(3,3,3);
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            JFXButton exit = new JFXButton("Exit");
            JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            exit.setOnAction(ev -> {
                try {
                    FileUtils.cleanDirectory(new java.io.File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                applicationContext.close();
                Platform.exit();
            });
            dialogLayout.setHeading(new Label("Server Side connection refused. Click confirm to close application."));
            List<JFXButton> buttonList = new ArrayList<>();
            buttonList.add(exit);
            dialogLayout.setActions(buttonList);
            dialog.show();
            dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                rootAnchor.setEffect(null);
            });
            rootAnchor.setEffect(boxBlur);
        }catch (InvalidCredentialsException e) {
            BoxBlur boxBlur = new BoxBlur(3,3,3);
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            JFXButton cancel = new JFXButton("Confirm");
            JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            cancel.setOnAction(ev -> {
                dialog.close();
                dataStorage.setUserDetails(null);
                stageInitializer.switchScene(stageInitializer.getLoginResource());
            });
            dialogLayout.setHeading(new Label("Login credentials no longer valid."));
            List<JFXButton> buttonList = new ArrayList<>();
            buttonList.add(cancel);
            dialogLayout.setActions(buttonList);
            dialog.show();
            dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                rootAnchor.setEffect(null);
            });
            rootAnchor.setEffect(boxBlur);
        }catch (UnauthorizedException e) {
            BoxBlur boxBlur = new BoxBlur(3,3,3);
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            JFXButton cancel = new JFXButton("Confirm");
            JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            cancel.setOnAction(ev -> {
                dialog.close();
                dataStorage.setUserDetails(null);
                stageInitializer.switchScene(stageInitializer.getLoginResource());
            });
            dialogLayout.setHeading(new Label("Cannot access this resource. User logged out."));
            List<JFXButton> buttonList = new ArrayList<>();
            buttonList.add(cancel);
            dialogLayout.setActions(buttonList);
            dialog.show();
            dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                rootAnchor.setEffect(null);
            });
            rootAnchor.setEffect(boxBlur);
        }

    }

}
