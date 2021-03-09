package bfst21.vector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var filename = "amager.zip.obj";
        var model = new Model(filename);
        new View(model, primaryStage);
    }
}