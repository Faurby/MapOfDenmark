package bfst21.view.controllers;

import bfst21.address.Address;
import bfst21.address.TST;
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

    private TST<Node> streetTries;

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
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && addressArea.getText().trim().length() <= 3){
            suggestions.getChildren().clear();
        } else {
            if (addressArea.getText().trim().length() >= 2) {
                try{ if (streetTries == null) {
                    MapCanvas mapCanvas = mainController.getCanvas();
                    Model model = mapCanvas.getModel();
                    MapData mapData = model.getMapData();
                    streetTries = mapData.getStreetTries();
                }

                Iterable<String> list = streetTries.keysWithPrefix(addressArea.getText());
                suggestions.getChildren().clear();
                for (String s : list) {
                    Label b = new Label(s);
                    b.setOnMouseClicked((event) -> {
                        addressArea.setText(b.getText());
                        suggestions.getChildren().clear();
                    });
                    suggestions.getChildren().add(b);
                }} catch (NullPointerException e) {
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
