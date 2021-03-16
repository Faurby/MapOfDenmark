package bfst21.vector;

import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String filename = "amager.obj";
        boolean jarFile = true;

        if (getParameters().getRaw().size() > 0) {
            filename = getParameters().getRaw().get(0);
            jarFile = false;
        }
        Model model = new Model(filename, jarFile);
        new View(model, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}