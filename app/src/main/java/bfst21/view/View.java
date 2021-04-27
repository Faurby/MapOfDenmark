package bfst21.view;

import java.io.IOException;

import bfst21.models.Model;
import bfst21.view.controllers.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class View {

    public View(Model model, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(View.class.getResource("View.fxml"));
        Scene scene = loader.load();
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Map of Denmark");
        MainController mainController = loader.getController();
        stage.show();

        mainController.init(model);
        mainController.onWindowResize(stage);

        stage.widthProperty().addListener(e -> mainController.onWindowResize(stage));
        stage.heightProperty().addListener(e -> mainController.onWindowResize(stage));

        stage.maximizedProperty().addListener(e -> {
            System.out.println("Fullscreen");
            mainController.onWindowResize(stage);
        });
    }
}
