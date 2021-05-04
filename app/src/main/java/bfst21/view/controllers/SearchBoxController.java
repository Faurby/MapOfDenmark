package bfst21.view.controllers;

import bfst21.address.TST;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.osm.OsmAddress;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

    private TST<List<OsmAddress>> addressTries;
    private Task<Void> addressSuggestionTask;
    private List<OsmAddress> allSuggestions = new ArrayList<>();
    private List<String> shownSuggestions = new ArrayList<>();

    @FXML
    private void searchSingleAddress() {
        String address = addressArea.getText().trim().toLowerCase();
        suggestions.getChildren().clear();

        if (!address.isEmpty()) {

            if (addressTries == null) {
                activateTries();
            }
            for (OsmAddress osmAddress : allSuggestions) {
                if (osmAddress.toString().toLowerCase().contains(address) || osmAddress.omitHouseNumberToString().toLowerCase().contains(address)) {
                    mainController.getCanvas().changeView(osmAddress.getNode().getX(), osmAddress.getNode().getY());
                    break;
                }
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("");
            alert.setContentText("Search field is empty.");
            alert.showAndWait();
        }
    }

    private void activateTries() {
        MapCanvas mapCanvas = mainController.getCanvas();
        Model model = mapCanvas.getModel();
        MapData mapData = model.getMapData();
        addressTries = mapData.getAddressTries();
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
            suggestions.getChildren().clear();
            searchSingleAddress();
            searchButton.requestFocus();

        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && addressArea.getText().trim().length() <= 2) {
            suggestions.getChildren().clear();

        } else if (keyEvent.getCode() == KeyCode.DOWN && shownSuggestions.size() > 0){
            suggestions.requestFocus();
        } else {
            int textLength = addressArea.getText().trim().length();
            if (textLength >= 2) {
                runAddressSuggestionTask();
            }
        }
    }

    private void displayAddressSuggestions() {
        int count = 0;
        suggestions.getChildren().clear();

        for (String s : shownSuggestions) {
            if (count <= 500) {
                Label b = new Label(s);
                b.setPrefWidth(800);
                b.setOnMouseClicked((event) -> {
                    addressArea.setText(b.getText());
                    suggestions.getChildren().clear();
                    addressArea.requestFocus();
                });
                b.setOnMouseEntered((event) -> {
                    b.setStyle("-fx-background-color:#dae7f3;");
                });
                b.setOnMouseExited((event) -> {
                    b.setStyle("-fx-background-color: transparent;");
                });
                suggestions.getChildren().add(b);
                count++;
            }
        }
    }

    private void runAddressSuggestionTask() {
        if (addressSuggestionTask != null) {
            if (addressSuggestionTask.isRunning()) {
                addressSuggestionTask.cancel();
            }
        }
        addressSuggestionTask = new Task<>() {
            @Override
            protected Void call() {
                if (addressTries == null) {
                    activateTries();
                }
                shownSuggestions = new ArrayList<>();

                String input = addressArea.getText();
                String addressInput = input.replace(" ", "").toLowerCase();

                Iterator<String> it = addressTries.keysWithPrefix(addressInput).iterator();

                //If the string doesn't return matches, find a substring that does
                if (!it.hasNext()) {
                    addressInput = longestSubstringWithMatches(addressInput);
                    it = addressTries.keysWithPrefix(addressInput).iterator();
                }

                //A check to see whether a valid street name has been typed
                //This determines whether the suggested addresses are of streets or house numbers
                String streetName = null;
                if (it.hasNext()){
                    String streetInfo = addressTries.keysWithPrefix(addressInput).iterator().next();
                    streetName = streetInfo.substring(0, streetInfo.length()-4);
                }

                if (addressInput.equals(streetName)) {

                    if (it.hasNext()) {
                        allSuggestions = new ArrayList<>();
                        allSuggestions = addressTries.get(it.next());
                    }

                    if (allSuggestions.size() > 0) {
                        for (OsmAddress osmAddress : allSuggestions) {
                            String address = osmAddress.toString();
                            shownSuggestions.add(address);
                        }
                    }
                } else {

                    allSuggestions = new ArrayList<>();

                    while (it.hasNext()) {
                        allSuggestions.add(addressTries.get(it.next()).get(0));
                    }
                    for (OsmAddress osmAddress : allSuggestions) {
                        String address = osmAddress.omitHouseNumberToString();
                        shownSuggestions.add(address);
                    }
                }
                return null;
            }
        };

        addressSuggestionTask.setOnSucceeded(e -> displayAddressSuggestions());
        addressSuggestionTask.setOnFailed(e -> addressSuggestionTask.getException().printStackTrace());
        Thread thread = new Thread(addressSuggestionTask);
        thread.start();
    }

    public String longestSubstringWithMatches(String addressInput){
        Iterator<String> modifiedIt;
        boolean hasResults = true;
        int counter = 1;

        while (hasResults && counter < addressInput.length()) {
            modifiedIt = addressTries.keysWithPrefix(addressInput.substring(0, counter)).iterator();
            if(!modifiedIt.hasNext()){
                hasResults = false;
                counter--;
            } else {
                counter++;
            }
        }

        return addressInput.substring(0, counter);
    }

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    public void transferAddressText(String address) {
        addressArea.setText(address);
    }

}
