package uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DataStorage;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.StageInitializer;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.GenericServerSideException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.Exceptions.InvalidCredentialsException;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.models.UserDetails;
import uk.co.ankeetpatel.encryptedfilesystem.guiconnector.WebClientEFS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private StageInitializer stageInitializer;

    @Autowired
    private DataStorage dataStorage;

    @FXML
    public StackPane rootPane;

    @FXML
    public AnchorPane rootAnchor;

    @FXML
    public JFXTextField UsernameField;

    @FXML
    public JFXPasswordField PasswordField;

    @FXML
    public JFXButton LoginButton, ExitButton, InfoButton;

    
    private WebClientEFS webClientEFS;



    public MainController(WebClientEFS webClientEFS) {
        this.webClientEFS = webClientEFS;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        setInfoButton();
        setExitButton();
        setOnEnter();
        setLoginButton();

    }

    private void setOnEnter() {
        PasswordField.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER)) {
                loginUser();
            }
        });
    }

    private void setLoginButton() {
        LoginButton.setOnAction(e -> {
            loginUser();
        });
    }

    private void loginUser() {
        if (!UsernameField.getText().isEmpty() && !PasswordField.getText().isEmpty()) {
            try {
                Mono<UserDetails> a = webClientEFS.login(UsernameField.getText(), PasswordField.getText());
                UserDetails details = a.block();
                dataStorage.setKeys(dataStorage.getCipherUtility().getKeyPair());
                dataStorage.setUserDetails(details);
                stageInitializer.switchScene(stageInitializer.getAllFilesResource());
            } catch (InvalidCredentialsException ex) {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton cancel = new JFXButton("Confirm");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                cancel.setOnAction(ev -> {
                    dialog.close();
                });
                dialogLayout.setHeading(new Label("Login failed. Incorrect username or password."));
                List<JFXButton> buttonList = new ArrayList<>();
                buttonList.add(cancel);
                dialogLayout.setActions(buttonList);
                dialog.show();
                dialog.setOnDialogClosed((JFXDialogEvent ev) -> {
                    rootAnchor.setEffect(null);
                });
                rootAnchor.setEffect(boxBlur);
            } catch (GenericServerSideException ex) {
                BoxBlur boxBlur = new BoxBlur(3,3,3);
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                JFXButton exit = new JFXButton("Exit");
                JFXDialog dialog = new JFXDialog(rootPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
                exit.setOnAction(ev -> {
                    try {
                        FileUtils.cleanDirectory(new File("frontend-ui/src/main/java/uk/co/ankeetpatel/encryptedfilesystem/frontendui/files"));
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
            }
        }
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

    private void setInfoButton() {
        InfoButton.setOnAction(e-> {
            stageInitializer.switchScene(stageInitializer.getInfoResource());
        });
    }


}
