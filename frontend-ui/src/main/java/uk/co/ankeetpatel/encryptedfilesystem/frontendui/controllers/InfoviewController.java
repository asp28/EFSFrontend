package uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.StageInitializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class InfoviewController implements Initializable {

    @FXML
    public JFXButton HomeButton, ExitButton, change;

    @FXML
    public AnchorPane anchorPane;

    @Autowired
    StageInitializer stageInitializer;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setExitButton();
        setHomeButton();
        setChange();
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


    private void setHomeButton() {
        HomeButton.setOnAction(e-> {
            stageInitializer.switchScene(stageInitializer.getLoginResource());
        });
    }

    private void setChange() {
        change.setOnAction(e-> {
            if (stageInitializer.getAccessibility()) {
                stageInitializer.setAccessibility(false);
                stageInitializer.switchScene(stageInitializer.getInfoResource());
            } else {
                stageInitializer.setAccessibility(true);
                stageInitializer.switchScene(stageInitializer.getInfoResource());
            }
        });
    }

}
