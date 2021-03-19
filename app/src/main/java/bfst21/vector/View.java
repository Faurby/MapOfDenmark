package bfst21.vector;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class View {

    public View(Model model, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(View.class.getResource("View.fxml"));
        Scene scene = loader.load();
        stage.setScene(scene);
        stage.setTitle("Awesome drawing program");
        Controller controller = loader.getController();
        stage.show();
        controller.init(model);

        stage.widthProperty().addListener(e -> {
            controller.onWindowResize();
        });
        stage.heightProperty().addListener(e -> {
            controller.onWindowResize();
        });
    }
}
