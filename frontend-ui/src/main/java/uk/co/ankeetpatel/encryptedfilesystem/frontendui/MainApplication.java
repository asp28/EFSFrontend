package uk.co.ankeetpatel.encryptedfilesystem.frontendui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class MainApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
            genericApplicationContext.registerBean(Application.class, () -> MainApplication.this);
            genericApplicationContext.registerBean(Parameters.class, () -> getParameters());
            genericApplicationContext.registerBean(HostServices.class, () -> getHostServices());
        };

        this.applicationContext = new SpringApplicationBuilder().sources(FrontendUiApplication.class)
                .initializers(initializer)
                .build().run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) {
         this.applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return ((Stage) getSource());
        }
    }
}
