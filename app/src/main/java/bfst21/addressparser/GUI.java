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
    public void start(Stage primaryStage) throws Exception{
        Main.main();
        TextField input = new TextField();
        TextArea output = new TextArea();
        BorderPane pane = new BorderPane();
        input.setFont(new Font(30));
        output.setFont(new Font(30));
        input.setOnAction(e -> {
            String raw = input.getText();
            Address parsed = Address.parse(raw);
            output.setText(parsed.toString());
        });
        pane.setTop(input);
        pane.setCenter(output);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle("Test test yo");
        primaryStage.show();
    }
}
