package uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DataStorage;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.StageInitializer;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.GenericServerSideException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.InvalidCredentialsException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.UnauthorizedException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.FileResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.FilesResponse;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.WebClientEFS;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.payload.responses.UploadResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;


@Component
public class listviewController implements Initializable {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private WebClientEFS webClientEFS;

    @Autowired
    private DataStorage dataStorage;

    @Autowired
    private StageInitializer stageInitializer;

    @FXML
    public StackPane rootPane;

    @FXML
    public AnchorPane rootAnchor;

    @FXML
    private JFXTreeTableView<File> TreeTable;

    @FXML
    public JFXButton ExitButton, HomeButton, logoutButton, uploadButton, rolesButton;

    @FXML
    private JFXTextField SearchBar;


    public listviewController(WebClientEFS webClientEFS) {
        this.webClientEFS = webClientEFS;
    }


    class File extends RecursiveTreeObject<File> {
        Long id;
        StringProperty fileName;
        StringProperty dateModified;

        public File(Long id, String fileName, String dateModified) {
            this.id = id;
            this.fileName = new SimpleStringProperty(fileName);
            this.dateModified = new SimpleStringProperty(dateModified);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setFiles();
        generateSearchFunction();
        setExitButton();
        setHomeButton();
        setLogoutButton();
        setUploadButton();
        setRolesButton();
    }

    private void setFiles() {
        JFXTreeTableColumn<File, String> fileName = new JFXTreeTableColumn<>("Filename");
        fileName.setCellValueFactory(fileStringCellDataFeatures -> fileStringCellDataFeatures.getValue().getValue().fileName);
        fileName.setPrefWidth(350);

        JFXTreeTableColumn<File, String> readWrite = new JFXTreeTableColumn<>("Date Modified");
        readWrite.setCellValueFactory(fileStringCellDataFeatures -> fileStringCellDataFeatures.getValue().getValue().dateModified);
        readWrite.setPrefWidth(190);

        JFXTreeTableColumn<File, String> settingsColumn = new JFXTreeTableColumn<>("Details");
        settingsColumn.setPrefWidth(140);
        Callback<TreeTableColumn<File, String>, TreeTableCell<File, String>> cellFactory
                = //
                new Callback<TreeTableColumn<File, String>, TreeTableCell<File, String>>() {
                    @Override
                    public TreeTableCell call(final TreeTableColumn<File, String> param) {
                        final TreeTableCell<File, String> cell = new TreeTableCell<File, String>() {

                            final JFXButton btn = new JFXButton("Click me");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setButtonType(JFXButton.ButtonType.RAISED);
                                    btn.setOnAction(event -> {
                                        //Button Action here
                                        File f = getTreeTableRow().getItem();
                                        FileResponse file = null;
                                        for(FileResponse file1: dataStorage.getFilesResponse().getFilesResponse()) {
                                            if(file1.getFile().getId() == f.id) {
                                                file = file1;
                                                break;
                                            }
                                        }
                                        dataStorage.setFileToAccess(file);
                                        stageInitializer.switchScene(stageInitializer.getDetailsResource());

                                    });
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        settingsColumn.setCellFactory(cellFactory);


        ObservableList<File> files = FXCollections.observableArrayList();
        try {
            Mono<FilesResponse> filesResponse = webClientEFS.files(dataStorage.getUserDetails());
            dataStorage.setFilesResponse(filesResponse.block());
            for (FileResponse f : filesResponse.block().getFilesResponse()) {
                File newFile;
                if (f.getFile().getDateModified() == null) {
                    newFile = new File(f.getFile().getId(), f.getFile().getFileName(), f.getFile().getDateUploaded());
                } else {
                    newFile = new File(f.getFile().getId(), f.getFile().getFileName(), f.getFile().getDateModified());
                }
                files.add(newFile);
            }
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

        TreeTable.setRowFactory(tv -> {
            TreeTableRow<File> row = new TreeTableRow<>();
            row.setOnMouseClicked(e -> {
                if(e.getClickCount() == 2 && !row.isEmpty()) {
                    File f = row.getItem();
                    FileResponse file = null;
                    for(FileResponse file1: dataStorage.getFilesResponse().getFilesResponse()) {
                        if(file1.getFile().getId() == f.id) {
                            file = file1;
                            break;
                        }
                    }
                    dataStorage.setFileToAccess(file);
                    stageInitializer.switchScene(stageInitializer.getDetailsResource());
                }
            });
            return row;
        });


        final TreeItem<File> root = new RecursiveTreeItem<File>(files, RecursiveTreeObject::getChildren);
        TreeTable.getColumns().setAll(fileName, readWrite, settingsColumn);
        TreeTable.setRoot(root);
        TreeTable.setShowRoot(false);


    }

    private void generateSearchFunction() {
        SearchBar.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                TreeTable.setPredicate(new Predicate<TreeItem<File>>() {
                    @Override
                    public boolean test(TreeItem<File> fileTreeItem) {
                        boolean flag = fileTreeItem.getValue().fileName.getValue().contains(t1);
                        return flag;
                    }
                });
            }
        });
    }

    private void setExitButton() {
        ExitButton.setOnAction(e-> {
            try {
                FileUtils.cleanDirectory(new java.io.File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
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
                FileUtils.cleanDirectory(new java.io.File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
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

}
