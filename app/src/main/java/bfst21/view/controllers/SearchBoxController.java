package bfst21.view.controllers;

import bfst21.address.Address;
import bfst21.address.AddressType;
import bfst21.address.TST;
import bfst21.address.TriesMap;
import bfst21.exceptions.IllegalInputException;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.osm.Node;
import bfst21.view.MapCanvas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;


public class SearchBoxController extends SubController {

    @FXML
    private TextArea addressArea;
    @FXML
    private Button navigateButton;
    @FXML
    private Button searchButton;
    @FXML
    private StackPane searchBox;
    @FXML
    private VBox suggestions;

    private TriesMap triesMap;

    @FXML
    private void searchSingleAddress() {
        if (!addressArea.getText().trim().isEmpty()) {
            String address = addressArea.getText();
            Address parsed = Address.parse(address);
            System.out.println(parsed);
        } else {
            throw new IllegalInputException("Search field is empty");
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        searchBox.setVisible(visible);
        searchBox.setManaged(visible);
        if (visible) {
            searchBox.requestFocus();
        }
    }

    @FXML
    public void expandSearchView() {
        mainController.setSearchBoxVisible(false);
        mainController.setNavigationBoxVisible(true);

        if (addressArea.getText() != null) {
            mainController.setNavigationBoxAddressText(addressArea.getText());
        }
    }

    public void typingCheck(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.TAB) {
            if (keyEvent.getSource().toString().contains("addressArea")) {
                addressArea.setText(addressArea.getText().trim());
                navigateButton.requestFocus();
            }
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            addressArea.setText(addressArea.getText().trim());
            searchSingleAddress();
            searchButton.requestFocus();
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && addressArea.getText().trim().length() <= 3) {
            suggestions.getChildren().clear();
        } else {
            int textLength = addressArea.getText().trim().length();
            if (textLength >= 2) {
                try {
                    if (triesMap == null) {
                        MapCanvas mapCanvas = mainController.getCanvas();
                        Model model = mapCanvas.getModel();
                        MapData mapData = model.getMapData();
                        triesMap = mapData.getTriesMap();
                    }
                    suggestions.getChildren().clear();
                    for (String s : triesMap.keysWithPrefix(addressArea.getText(), textLength)) {
                        Label b = new Label(s);
                        b.setOnMouseClicked((event) -> {
                            addressArea.setText(b.getText());
                            suggestions.getChildren().clear();
                        });
                        suggestions.getChildren().add(b);
                    }
                } catch (NullPointerException e) {
                    e.getMessage();
                }
            }
        }
    }

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    public void transferAddressText(String address) {
        addressArea.setText(address);
    }
}
