package bfst21.vector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("data/lines8.txt");
        var view = new View(model, primaryStage);
        new Controller(model, view);
        new Controller(model, new View(model, new Stage(), true));
    }
}