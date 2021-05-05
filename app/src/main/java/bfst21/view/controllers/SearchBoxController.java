package bfst21.view.controllers;

import bfst21.osm.OsmAddress;
import bfst21.osm.Pin;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SearchBoxController extends NavigationSubController {

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

    @FXML
    private void searchSingleAddress() {
        String address = addressArea.getText().trim().toLowerCase();
        suggestions.getChildren().clear();

        if (!address.isEmpty()) {

            checkTries();

            for (OsmAddress osmAddress : getAllSuggestions()) {
                if (osmAddress.toString().toLowerCase().contains(address) || osmAddress.omitHouseNumberToString().toLowerCase().contains(address)) {

                    Pin.DESTINATION.setCoords(osmAddress.getNode().getX(), osmAddress.getNode().getY());
                    Pin.DESTINATION.setVisible(true);

                    mainController.getCanvas().changeView(osmAddress.getNode().getX(), osmAddress.getNode().getY());
                    break;
                }
            }
        } else {
            displayAlert(Alert.AlertType.ERROR, "Error", "Search field is empty");
        }
    }

    @Override
    public void setVisible(boolean visible) {
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
        Pin.DESTINATION.setVisible(false);

        if (addressArea.getText() != null) {
            mainController.setNavigationBoxAddressText(addressArea.getText());
            setAllSuggestionsDestSpecific(getAllSuggestions());
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
            suggestions.getChildren().clear();
            searchSingleAddress();
            searchButton.requestFocus();

        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && addressArea.getText().trim().length() <= 2) {
            suggestions.getChildren().clear();

        } else if (keyEvent.getCode() == KeyCode.DOWN && getShownSuggestions().size() > 0) {
            suggestions.requestFocus();
        } else {
            int textLength = addressArea.getText().trim().length();
            if (textLength >= 2) {
                runAddressSuggestionTask(suggestions, addressArea, false);
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
