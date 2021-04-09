package bfst21.core;

import bfst21.models.Model;
import bfst21.view.View;
import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String defaultFileName = "amager.obj";
        boolean jarFile = false;

        String path = getClass().getResource("App.class").toString();
        if (path.contains(".jar")) {
            jarFile = true;
        }
        if (getParameters().getRaw().size() > 0) {
            defaultFileName = getParameters().getRaw().get(0);
        }
        Model model = new Model(defaultFileName, jarFile);
        new View(model, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}