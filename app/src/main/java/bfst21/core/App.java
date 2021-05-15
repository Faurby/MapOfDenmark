package bfst21.core;

import bfst21.models.Model;
import bfst21.view.View;
import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String defaultFileName = "denmark.obj";

        boolean jarFile = getClass().getResource("App.class").toString().contains(".jar");

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