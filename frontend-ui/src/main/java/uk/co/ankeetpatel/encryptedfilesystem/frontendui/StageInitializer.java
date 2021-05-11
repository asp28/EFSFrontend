package uk.co.ankeetpatel.encryptedfilesystem.frontendui;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.MainApplication.StageReadyEvent;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.controllers.*;
import uk.co.ankeetpatel.encryptedfilesystem.frontendui.utils.DataStorage;


import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Value("classpath:/fxml/main.fxml")
    private Resource loginResource;

    @Value("classpath:/fxml/listview.fxml")
    private Resource allFilesResource;

    @Value("classpath:/fxml/detailview.fxml")
    private Resource detailsResource;

    @Value("classpath:/fxml/Info.fxml")
    private Resource infoResource;

    @Value("classpath:/fxml/roles.fxml")
    private Resource rolesResource;

    @Autowired
    private RoleviewController roleviewController;

    @Autowired
    private MainController mainController;

    @Autowired
    private listviewController listviewController;

    @Autowired
    private detailviewController detailviewController;

    @Autowired
    private InfoviewController infoviewController;

    private Stage stage;

    @Value("${spring.application.ui.title}")
    private String applicationTitle;

    @Value("classpath:images/icon.png")
    private Resource icon;

    private ApplicationContext applicationContext;

    @Autowired
    private DataStorage dataStorage;

    private double xOffset = 0;
    private double yOffset = 0;

    private Boolean accessibility = false;


    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(loginResource.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            stage = stageReadyEvent.getStage();

            parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });


            Scene scene = new Scene(parent);
            //scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle(applicationTitle);
            stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toExternalForm()));
            stage.show();
        } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public MainController getMainController() {
        return mainController;
    }

    public listviewController getListviewController() {
        return listviewController;
    }

    public detailviewController getDetailViewController() {
        return detailviewController;
    }

    public InfoviewController getInfoController() {
        return infoviewController;
    }

    public RoleviewController getRoleviewController() {
        return roleviewController;
    }

    public void switchScene(Resource resource) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(resource.getURL());
            switch (resource.getFilename()) {
                case "listview.fxml":
                    fxmlLoader.setControllerFactory(param -> getListviewController());
                    break;
                case "detailview.fxml":
                    fxmlLoader.setControllerFactory(param -> getDetailViewController());
                    break;
                case "main.fxml":
                    fxmlLoader.setControllerFactory(param -> getMainController());
                    break;
                case "Info.fxml":
                    fxmlLoader.setControllerFactory(param -> getInfoController());
                    break;
                case "roles.fxml":
                    fxmlLoader.setControllerFactory(param -> getRoleviewController());
                    break;
                default:
                    System.err.println("Resource not found.");
                    break;
            }
            Parent parent = fxmlLoader.load();

            parent.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
            Scene scene = new Scene(parent);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource(getCSS()).toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Resource getLoginResource() {
        return loginResource;
    }

    public Resource getAllFilesResource() {
        return allFilesResource;
    }

    public Resource getDetailsResource() {
        return detailsResource;
    }

    public Resource getInfoResource() {
        return infoResource;
    }

    public Resource getRolesResource() {
        return rolesResource;
    }

    public Boolean getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    private String getCSS() {
        if (accessibility) {
            return "/css/accessibility.css";
        }
        return "/css/main.css";
    }
}
