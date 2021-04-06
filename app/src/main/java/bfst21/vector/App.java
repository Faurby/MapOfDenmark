package bfst21.vector;

import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String fileName = "amager.obj";
        boolean jarFile = false;

        String path = getClass().getResource("App.class").toString();
        if (path.contains(".jar")) {
            jarFile = true;
        }
        if (getParameters().getRaw().size() > 0) {
            fileName = getParameters().getRaw().get(0);
        }
        Model model = new Model(fileName, jarFile);
        new View(model, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}