package bfst21.view;

import java.io.IOException;

import bfst21.models.Model;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class View {

    public View(Model model, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(View.class.getResource("View.fxml"));
        Scene scene = loader.load();
        stage.setScene(scene);
        stage.setTitle("Map of Denmark");
        Controller controller = loader.getController();
        stage.show();
        controller.init(model);
        controller.onWindowResize(stage);

        stage.widthProperty().addListener(e -> {
            controller.onWindowResize(stage);
        });
        stage.heightProperty().addListener(e -> {
            controller.onWindowResize(stage);
        });
        stage.maximizedProperty().addListener(e -> {
            System.out.println("Fullscreen");
            controller.onWindowResize(stage);
        });
    }
}
