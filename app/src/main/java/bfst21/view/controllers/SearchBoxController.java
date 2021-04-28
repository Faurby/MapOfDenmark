package bfst21.view.controllers;

import bfst21.address.Address;
import bfst21.address.TST;
import bfst21.exceptions.IllegalInputException;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.osm.OsmAddress;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
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
    private List<String> allSuggestions = new ArrayList<>();
    private List<String> shownSuggestions = new ArrayList<>();

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

        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE && addressArea.getText().trim().length() <= 2) {
            suggestions.getChildren().clear();

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
            if (count <= 50) {
                Label b = new Label(s);
                b.setOnMouseClicked((event) -> {
                    addressArea.setText(b.getText());
                    suggestions.getChildren().clear();
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
                    MapCanvas mapCanvas = mainController.getCanvas();
                    Model model = mapCanvas.getModel();
                    MapData mapData = model.getMapData();
                    addressTries = mapData.getAddressTries();
                }
                shownSuggestions = new ArrayList<>();

                String input = addressArea.getText();
                String addressInput = input.replace(" ", "").toLowerCase();

                Iterator<String> it = addressTries.keysWithPrefix(addressInput).iterator();
                if (it.hasNext()) {

                    allSuggestions = new ArrayList<>();
                    List<OsmAddress> osmAddressList = addressTries.get(it.next());

                    for (OsmAddress osmAddress : osmAddressList) {
                        allSuggestions.add(osmAddress.toString());
                    }
                }
                if (allSuggestions.size() > 0) {

                    int count = 0;
                    for (String address : allSuggestions) {
                        if (count < 10) {

                            if (address.toLowerCase().contains(input.toLowerCase())) {
                                shownSuggestions.add(address);
                                count++;
                            }
                        }
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

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    public void transferAddressText(String address) {
        addressArea.setText(address);
    }
}
