package bfst21.view;

import java.io.IOException;
import java.util.Objects;

import bfst21.models.Model;
import bfst21.view.controllers.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class View {

    public View(Model model, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(View.class.getResource("View.fxml"));
        Scene scene = loader.load();

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Mapster");
        MainController mainController = loader.getController();
        Image icon = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("globe_icon2.png")));
        stage.getIcons().add(icon);
        stage.show();

        mainController.init(model);
        mainController.onWindowResize(stage);

        stage.widthProperty().addListener(e -> mainController.onWindowResize(stage));
        stage.heightProperty().addListener(e -> mainController.onWindowResize(stage));
        stage.setMinWidth(450.0D);
        stage.setMinHeight(500.0D);

        stage.maximizedProperty().addListener(e -> {
            System.out.println("Fullscreen");
            mainController.onWindowResize(stage);
        });
    }
}
