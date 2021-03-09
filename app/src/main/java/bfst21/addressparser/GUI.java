package bfst21.addressparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        CityController cityController = CityController.getInstance();
        cityController.readCities();
        TextField input = new TextField();
        input.setPromptText("Please write an address");

        TextArea output = new TextArea();
        output.setDisable(true);
        output.setOpacity(1);

        TextArea previousAddressTitel = new TextArea("Previous entered address:");
        previousAddressTitel.setPrefHeight(30);
        previousAddressTitel.setDisable(true);
        previousAddressTitel.setOpacity(1);

        VBox vbox = new VBox();
        vbox.setPrefWidth(300);
        vbox.getChildren().add(previousAddressTitel);

        BorderPane pane = new BorderPane();
        ScrollPane scrollpane = new ScrollPane();

        input.setFont(new Font(30));
        output.setFont(new Font(30));
        input.setOnAction(e -> {
            String raw = input.getText();
            Address parsed = Address.parse(raw);
            output.setText(parsed.toString());

            Button previousAddress = new Button(parsed.toString());
            previousAddress.setPrefHeight(60);
            previousAddress.setPrefWidth(vbox.getPrefWidth());

            previousAddress.setOnAction(i -> input.setText(previousAddress.getText()));

            vbox.getChildren().add(1, previousAddress);
        });

        scrollpane.setContent(vbox);

        pane.setTop(input);
        pane.setCenter(output);
        pane.setRight(scrollpane);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle("Test test yo");
        primaryStage.show();
    }
}
