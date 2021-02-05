package bfst21.addressparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var input = new TextField();
        var output = new TextArea();
        var pane = new BorderPane();
        input.setFont(new Font(30));
        output.setFont(new Font(30));
        input.setOnAction(e -> {
            var raw = input.getText();
            var parsed = Address.parse(raw);
            output.setText(parsed.toString());
        });
        pane.setTop(input);
        pane.setCenter(output);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle("Hello World!");
        primaryStage.show();
    }
}
