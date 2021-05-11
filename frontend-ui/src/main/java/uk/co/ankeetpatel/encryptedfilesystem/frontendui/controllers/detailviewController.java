package uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DataStorage;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DesktopApi;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.StageInitializer;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.GenericServerSideException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.InvalidCredentialsException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.UnauthorizedException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.WebClientEFS;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.PermissionRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.requests.UserPermissionRequest;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.DownloadResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.UserPermissionResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import org.apache.commons.io.FileUtils;


@Component
public class detailviewController implements Initializable {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private WebClientEFS webClientEFS;

    @Autowired
    StageInitializer stageInitializer;

    @Autowired
    DataStorage dataStorage;

    @FXML
    public StackPane rootPane;

    @FXML
    public AnchorPane rootAnchor;

    @FXML
    public JFXButton ExitButton, HomeButton, uploadButton, logoutButton, detaildownload, detaildelete, searchButton,
            updateButton, updateAndChooseAnotherButton, cancelButton, rolesButton;

    @FXML
    public Text detail_filename, detail_uploaded, detail_modified, alterFileText, filenameText;

    @FXML
    public TableView<Permission> detail_permissions;

    @FXML
    public TableColumn<Permission, String> readColumn, writeColumn, adminColumn, deleteColumn;

    @FXML
    public Tab permissionsTab;

    @FXML
    public JFXTextField SearchBar;

    @FXML
    public JFXToggleButton readToggle, writeToggle, adminToggle, deleteToggle;

    @FXML
    public AnchorPane UserAnchorPane;

    private Mono<UserPermissionResponse> userPermissionResponse;


    public detailviewController(WebClientEFS webClientEFS) {
        this.webClientEFS = webClientEFS;
    }


    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setExitButton();
        setHomeButton();
        setLogoutButton();
        setUploadButton();
        setDownloadButton();
        setDeleteButton();
        setFileDetails();
        setPermissionValues();
        setPermissionsTab();
        setPermissionsTabValues();
        setRolesButton();

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

    private void setDownloadButton() {
        detaildownload.setOnAction(e-> {
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
                    Mono<DownloadResponse> downloadResponse = webClientEFS.downloadFile(dataStorage.getUserDetails(), dataStorage.getFileToAccess().getFile().getId(), dataStorage.getKeys());
                    byte[] filebyte = dataStorage.getCipherUtility().decryption(downloadResponse.block().getFile(), dataStorage.getKeys().getPrivate());
                    System.out.println();

                    try (FileOutputStream fos = new FileOutputStream("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files/" + downloadResponse.block().getFileName())) {
                        fos.write(filebyte);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException Exception) {
                    Exception.printStackTrace();
                }catch (GenericServerSideException ex) {
                    JFXDialogLayout dialogLayout2 = new JFXDialogLayout();
                    JFXButton exit = new JFXButton("Exit");
                    JFXDialog dialog2 = new JFXDialog(rootPane, dialogLayout2, JFXDialog.DialogTransition.CENTER);
                    exit.setOnAction(even -> {
                        try {
                            FileUtils.cleanDirectory(new java.io.File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        applicationContext.close();
                        Platform.exit();
                    });
                    dialogLayout2.setHeading(new Label("Server Side connection refused. Click confirm to close application."));
                    List<JFXButton> buttonList = new ArrayList<>();
                    buttonList.add(exit);
                    dialogLayout2.setActions(buttonList);
                    dialog2.show();
                    dialog2.setOnDialogClosed((JFXDialogEvent even) -> {
                        rootAnchor.setEffect(null);
                    });
                    rootAnchor.setEffect(boxBlur);
                }catch (InvalidCredentialsException ex) {
                    JFXDialogLayout dialogLayout2 = new JFXDialogLayout();
                    JFXButton cancel2 = new JFXButton("Confirm");
                    JFXDialog dialog2 = new JFXDialog(rootPane, dialogLayout2, JFXDialog.DialogTransition.CENTER);
                    cancel2.setOnAction(event -> {
                        dialog2.close();
                        dataStorage.setUserDetails(null);
                        stageInitializer.switchScene(stageInitializer.getLoginResource());
                    });
                    dialogLayout2.setHeading(new Label("Login credentials no longer valid."));
                    List<JFXButton> buttonList = new ArrayList<>();
                    buttonList.add(cancel2);
                    dialogLayout2.setActions(buttonList);
                    dialog2.show();
                    dialog2.setOnDialogClosed((JFXDialogEvent event) -> {
                        rootAnchor.setEffect(null);
                    });
                    rootAnchor.setEffect(boxBlur);
                }catch (UnauthorizedException ex) {
                    JFXDialogLayout dialogLayout2 = new JFXDialogLayout();
                    JFXButton cancel2 = new JFXButton("Confirm");
                    JFXDialog dialog2 = new JFXDialog(rootPane, dialogLayout2, JFXDialog.DialogTransition.CENTER);
                    cancel.setOnAction(event -> {
                        dialog2.close();
                        dataStorage.setUserDetails(null);
                        stageInitializer.switchScene(stageInitializer.getLoginResource());
                    });
                    dialogLayout2.setHeading(new Label("Cannot access this resource. User logged out."));
                    List<JFXButton> buttonList = new ArrayList<>();
                    buttonList.add(cancel2);
                    dialogLayout2.setActions(buttonList);
                    dialog2.show();
                    dialog2.setOnDialogClosed((JFXDialogEvent event) -> {
                        rootAnchor.setEffect(null);
                    });
                    rootAnchor.setEffect(boxBlur);
                }
                System.out.println("File DOWNLOAD successful.");

                DesktopApi.edit(new File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files/" + dataStorage.getFileToAccess().getFile().getFileName()));
                dialog.close();
            });
            dialogLayout.setHeading(new Label("Are you sure you want to download:" + dataStorage.getFileToAccess().getFile().getFileName()));
            List<JFXButton> buttonList = new ArrayList<>();
            buttonList.add(cancel);
            buttonList.add(confirm);
            dialogLayout.setActions(buttonList);
            dialog.show();
            dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                rootAnchor.setEffect(null);
            });
            rootAnchor.setEffect(boxBlur);
        });
    }

    private void setDeleteButton() {
        //if user has delete permission enable delete
        if(dataStorage.getFileToAccess().getPermissions().contains(8)) {
            detaildelete.setOnAction(e-> {
                //send delete command
                try {
                    webClientEFS.deleteFile(dataStorage.getUserDetails(), dataStorage.getFileToAccess().getFile().getId());
                    stageInitializer.switchScene(stageInitializer.getAllFilesResource());
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
        }else {
            detaildelete.setDisable(true);
        }
        //else set delete to disabled.


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

    private void setFileDetails() {
        String originalName = dataStorage.getFileToAccess().getFile().getFileName();
        String upToNCharacters = originalName.substring(0, Math.min(originalName.length(), 15));
        if(!originalName.equalsIgnoreCase(upToNCharacters)) {
            upToNCharacters += "...";
        }
        detail_filename.setText(upToNCharacters);
        detail_uploaded.setText(dataStorage.getFileToAccess().getFile().getDateUploaded());
        detail_modified.setText(dataStorage.getFileToAccess().getFile().getDateModified());
    }

    private void setPermissionValues() {

        readColumn.setCellValueFactory(new PropertyValueFactory<>("read"));
        writeColumn.setCellValueFactory(new PropertyValueFactory<>("write"));
        adminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("delete"));

        ArrayList<Integer> permissionValues = dataStorage.getFileToAccess().getPermissions();
        String read = "False";
        String write = "False";
        String delete = "False";
        String admin = "False";

        for (int i : permissionValues) {
            switch (i) {
                case 1:
                    read = "True";
                    break;
                case 2:
                    write = "True";
                    break;
                case 8:
                    delete = "True";
                    break;
                case 16:
                    admin = "True";
                    break;
            }
        }

        ObservableList<Permission> data = FXCollections.observableArrayList(
                new Permission(read, write, admin, delete)
        );
        detail_permissions.setItems(data);
    }

    public static class Permission {
        private final SimpleStringProperty read;
        private final SimpleStringProperty write;
        private final SimpleStringProperty admin;
        private final SimpleStringProperty delete;

        public Permission(String read, String write, String admin, String delete) {
            this.read = new SimpleStringProperty(read);
            this.write = new SimpleStringProperty(write);
            this.admin = new SimpleStringProperty(admin);
            this.delete = new SimpleStringProperty(delete);
        }

        public String getRead() {
            return read.get();
        }

        public SimpleStringProperty readProperty() {
            return read;
        }

        public String getWrite() {
            return write.get();
        }

        public SimpleStringProperty writeProperty() {
            return write;
        }

        public String getAdmin() {
            return admin.get();
        }

        public SimpleStringProperty adminProperty() {
            return admin;
        }

        public String getDelete() {
            return delete.get();
        }

        public SimpleStringProperty deleteProperty() {
            return delete;
        }

        public void setRead(String read) {
            this.read.set(read);
        }

        public void setWrite(String write) {
            this.write.set(write);
        }

        public void setAdmin(String admin) {
            this.admin.set(admin);
        }

        public void setDelete(String delete) {
            this.delete.set(delete);
        }
    }

    public void setPermissionsTab() {
        if(!dataStorage.getFileToAccess().getPermissions().contains(16)) {
            permissionsTab.setDisable(true);
        }
    }

    public void setPermissionsTabValues() {
        UserAnchorPane.setDisable(true);

        searchButton.setOnAction(e -> {
            UserAnchorPane.setDisable(false);
            try {

                userPermissionResponse = webClientEFS.getUserPermissions(dataStorage.getUserDetails(),
                        new UserPermissionRequest(SearchBar.getText(), dataStorage.getFileToAccess().getFile().getId()));

                for (int i : userPermissionResponse.block().getPermissionValues()) {
                    System.out.println(i);
                    switch (i) {
                        case 1:
                            readToggle.setSelected(true);
                            break;
                        case 2:
                            writeToggle.setSelected(true);
                            break;
                        case 8:
                            deleteToggle.setSelected(true);
                            break;
                        case 16:
                            adminToggle.setSelected(true);
                            break;
                    }
                }
                alterFileText.setText("Alter permissions for: " + userPermissionResponse.block().getUsername());
                filenameText.setText("File name: " + userPermissionResponse.block().getFilename());
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
            stageInitializer.switchScene(stageInitializer.getDetailsResource());
        });
        updateAndChooseAnotherButton.setOnAction(e -> {
            update();
            UserAnchorPane.setDisable(true);
            alterFileText.setText("Alter permissions for: ");
            filenameText.setText("File name: ");
            readToggle.setSelected(false);
            writeToggle.setSelected(false);
            adminToggle.setSelected(false);
            deleteToggle.setSelected(false);
            SearchBar.setText("");
        });

        cancelButton.setOnAction(e -> {
            UserAnchorPane.setDisable(true);
            alterFileText.setText("Alter permissions for: ");
            filenameText.setText("File name: ");
            readToggle.setSelected(false);
            writeToggle.setSelected(false);
            adminToggle.setSelected(false);
            deleteToggle.setSelected(false);
            SearchBar.setText("");
        });

    }

    private HashMap<String, String> generateHashMap() {
        HashMap<String, String> permissions = new HashMap<>();
        if (readToggle.isSelected() == true) {
            permissions.put("read", "true");
        } else {
            permissions.put("read", "false");
        }
        if (writeToggle.isSelected() == true) {
            permissions.put("write", "true");
        } else {
            permissions.put("write", "false");
        }
        if (adminToggle.isSelected() == true) {
            permissions.put("admin", "true");
        } else {
            permissions.put("admin", "false");
        }
        if (deleteToggle.isSelected() == true) {
            permissions.put("delete", "true");
        } else {
            permissions.put("delete", "false");
        }
        return permissions;
    }

    private void update() {
        HashMap<String, String> permissions = generateHashMap();
        PermissionRequest permissionRequest = new PermissionRequest(userPermissionResponse.block().getFileID(), permissions, userPermissionResponse.block().getUserID());
        try {
            webClientEFS.updatePermissions(dataStorage.getUserDetails(), permissionRequest);
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
