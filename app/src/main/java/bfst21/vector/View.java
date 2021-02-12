package bfst21.vector;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class View {
    Model model;
    Stage stage;
    Canvas canvas = new Canvas(640,480);
    BorderPane pane = new BorderPane(canvas);
    Scene scene = new Scene(pane);
    
    public View(Model model, Stage stage) {
        this.model = model;
        this.stage = stage;
        stage.setScene(scene);
        stage.setTitle("Awesome drawing program");
        stage.show();
        var gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        for (var line : model) line.draw(gc);
	}
}
