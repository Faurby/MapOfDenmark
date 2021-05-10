package bfst21.view.controllers;

import bfst21.address.TST;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.address.OsmAddress;
import bfst21.osm.Pin;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class NavigationBoxController extends SubController {

    @FXML
    private TextArea originTextArea;
    @FXML
    private TextArea destinationTextArea;
    @FXML
    private ToggleButton selectCarButton;
    @FXML
    private ToggleButton selectBikeButton;
    @FXML
    private ToggleButton selectWalkButton;
    @FXML
    private Button switchAddressButton;
    @FXML
    private Button findRouteButton;
    @FXML
    private VBox routeBox;
    @FXML
    private VBox originSuggestionsBox;
    @FXML
    private VBox destinationSuggestionsBox;
    @FXML
    private TextArea addressTextArea;
    @FXML
    private Button expandButton;
    @FXML
    private Button searchButton;
    @FXML
    private VBox searchBox;
    @FXML
    private VBox suggestionsBox;

    private final TransportOptions transOptions = TransportOptions.getInstance();

    private List<OsmAddress> allSuggestionsOrigin = new ArrayList<>();
    private List<OsmAddress> allSuggestionsDestination = new ArrayList<>();

    private List<String> shownSuggestionsOrigin = new ArrayList<>();
    private List<String> shownSuggestionsDestination = new ArrayList<>();

    private Task<Void> addressSuggestionTask;
    private TST<List<OsmAddress>> addressTries;

    private boolean isNavigationBoxExpanded = false;

    public void initialize() {
        selectWalkButton.setOnAction(new ToggleTransportListener(TransportOption.WALK, selectWalkButton));
        selectBikeButton.setOnAction(new ToggleTransportListener(TransportOption.BIKE, selectBikeButton));
        selectCarButton.setOnAction(new ToggleTransportListener(TransportOption.CAR, selectCarButton));
    }

    @FXML
    public void searchSingleAddress() {
        String address = addressTextArea.getText().trim().toLowerCase();
        suggestionsBox.getChildren().clear();

        if (!address.isEmpty()) {

            for (OsmAddress osmAddress : allSuggestionsOrigin) {
                if (osmAddress.toString().toLowerCase().contains(address)
                 || osmAddress.omitHouseNumberToString().toLowerCase().contains(address)) {

                    Pin.DESTINATION.setCoords(osmAddress.getNodeCoords());
                    Pin.DESTINATION.setVisible(true);

                    mainController.getCanvas().changeView(osmAddress.getNode().getX(), osmAddress.getNode().getY());
                    return;
                }
            }
            displayAlert(Alert.AlertType.ERROR, "Error", "Unable to find address: "+address);
        } else {
            displayAlert(Alert.AlertType.ERROR, "Error", "Search field is empty");
        }
    }

    public void typingCheck(KeyEvent keyEvent) {
        if (!isNavigationBoxExpanded) {

            if (keyEvent.getCode() == KeyCode.TAB) {
                if (keyEvent.getSource().toString().contains("addressTextArea")) {

                    //TODO: What is the point in trimming the text in addressArea?
                    addressTextArea.setText(addressTextArea.getText().trim());
                    expandButton.requestFocus();
                }
            } else if (keyEvent.getCode() == KeyCode.ENTER) {

                //TODO: What is the point in trimming the text in addressArea?
                addressTextArea.setText(addressTextArea.getText().trim());
                suggestionsBox.getChildren().clear();
                searchButton.requestFocus();
                searchSingleAddress();

            } else if (keyEvent.getCode() == KeyCode.BACK_SPACE
                    && addressTextArea.getText().trim().length() <= 2) {

                suggestionsBox.getChildren().clear();

            } else if (keyEvent.getCode() == KeyCode.DOWN && shownSuggestionsOrigin.size() > 0) {
                suggestionsBox.requestFocus();

            } else {
                int textLength = addressTextArea.getText().trim().length();
                if (textLength >= 2) {
                    runAddressSuggestionTask(suggestionsBox, addressTextArea, false);
                }
            }
        } else {
            if (keyEvent.getCode() == KeyCode.TAB) {

                if (keyEvent.getSource().toString().contains("originTextArea")) {
                    originTextArea.setText(originTextArea.getText().trim());
                    destinationTextArea.requestFocus();

                } else if (keyEvent.getSource().toString().contains("destinationTextArea")) {
                    destinationTextArea.setText(destinationTextArea.getText().trim());
                    originTextArea.setText(originTextArea.getText().trim());
                    switchAddressButton.requestFocus();
                }
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                findRouteButton.requestFocus();
                originTextArea.setText(originTextArea.getText().trim());
                destinationTextArea.setText(destinationTextArea.getText().trim());
                originSuggestionsBox.getChildren().clear();
                destinationSuggestionsBox.getChildren().clear();
                findRoute();
            } else {
                if (keyEvent.getSource().toString().contains("originTextArea")) {
                    int textLength = originTextArea.getText().trim().length();
                    if (textLength >= 2) {
                        runAddressSuggestionTask(originSuggestionsBox, originTextArea, false);
                    }
                } else if (keyEvent.getSource().toString().contains("destinationTextArea")) {
                    int textLength = destinationTextArea.getText().trim().length();
                    if (textLength >= 2) {
                        runAddressSuggestionTask(destinationSuggestionsBox, destinationTextArea, true);
                    }
                }
            }
        }
    }

    @FXML
    public void findRoute() {
        if (originTextArea.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Starting point search field is empty");

        } else if (destinationTextArea.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Destination point search field is empty");

        } else {
            String startingAddress = originTextArea.getText().trim().toLowerCase();
            String destinationAddress = destinationTextArea.getText().trim().toLowerCase();

            originSuggestionsBox.getChildren().clear();
            destinationSuggestionsBox.getChildren().clear();

            float[] originCoords = null;
            float[] destinationCoords = null;

            for (OsmAddress osmAddressS : allSuggestionsOrigin) {
                if (osmAddressS.toString().toLowerCase().contains(startingAddress)
                        || osmAddressS.omitHouseNumberToString().toLowerCase().contains(startingAddress)) {
                    originCoords = new float[]{osmAddressS.getNode().getX(), osmAddressS.getNode().getY()};
                    break;
                }
            }

            for (OsmAddress osmAddressD : allSuggestionsDestination) {
                if (osmAddressD.toString().toLowerCase().contains(destinationAddress)
                        || osmAddressD.omitHouseNumberToString().toLowerCase().contains(destinationAddress)) {
                    destinationCoords = new float[]{osmAddressD.getNode().getX(), osmAddressD.getNode().getY()};
                    break;
                }
            }

            if (originCoords != null && destinationCoords != null) {

                Pin.ORIGIN.setCoords(originCoords[0], originCoords[1]);
                Pin.ORIGIN.setVisible(true);

                Pin.DESTINATION.setCoords(destinationCoords[0], destinationCoords[1]);
                Pin.DESTINATION.setVisible(true);

                float avgX = (originCoords[0] + destinationCoords[0]) / 2;
                float avgY = (originCoords[1] + destinationCoords[1]) / 2;

                mainController.getCanvas().changeView(avgX, avgY);
                mainController.changeZoomToShowPoints(originCoords, destinationCoords);

                TransportOptions transportOptions = TransportOptions.getInstance();
                TransportOption currentTransportOption = transportOptions.getCurrentlyEnabled();

                float[] nearOriginCoords = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(originCoords, currentTransportOption);
                float[] nearDestinationCoords = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(destinationCoords, currentTransportOption);

                mainController.getCanvas().originCoords = nearOriginCoords;
                mainController.getCanvas().destinationCoords = nearDestinationCoords;
                mainController.getCanvas().runDijkstraTask();

                mainController.getCanvas().repaint();
            }
        }
    }

    private void displayAddressSuggestions(VBox suggestions, TextArea textArea, boolean extended) {
        int count = 0;
        suggestions.getChildren().clear();

        List<String> localShownSuggestions = shownSuggestionsOrigin;
        if (extended) {
            localShownSuggestions = shownSuggestionsDestination;
        }

        for (String s : localShownSuggestions) {
            if (count <= 500) {
                Label b = new Label(s);
                b.setPrefWidth(800.0D);
                b.setOnMouseClicked((event) -> {
                    textArea.setText(b.getText());
                    suggestions.getChildren().clear();
                    textArea.requestFocus();
                });
                b.setOnMouseEntered((event) -> b.setStyle("-fx-background-color:#dae7f3;"));
                b.setOnMouseExited((event) -> b.setStyle("-fx-background-color: transparent;"));

                suggestions.getChildren().add(b);
                count++;
            }
        }
    }

    private void runAddressSuggestionTask(VBox suggestions, TextArea textArea, boolean extended) {if (addressSuggestionTask != null) {
            if (addressSuggestionTask.isRunning()) {
                addressSuggestionTask.cancel();
            }
        }
        addressSuggestionTask = new Task<>() {

            @Override
            protected Void call() {
                List<OsmAddress> localAllSuggestions;
                if (extended) {
                    shownSuggestionsDestination = new ArrayList<>();
                    localAllSuggestions = allSuggestionsDestination;
                } else {
                    shownSuggestionsOrigin = new ArrayList<>();
                    localAllSuggestions = allSuggestionsOrigin;
                }
                updateAddressTries();

                String input = textArea.getText();
                String addressInput = input.replace(" ", "").toLowerCase();

                Iterator<String> it = addressTries.keysWithPrefix(addressInput).iterator();

                //If the string doesn't return matches, find a substring that does
                if (!it.hasNext()) {
                    addressInput = findLongestSubstringWithMatches(addressInput);
                    it = addressTries.keysWithPrefix(addressInput).iterator();
                }

                //Has a valid street name been typed? Either street names or house numbers are then suggested
                String streetName = null;
                if (it.hasNext()) {
                    String streetInfo = addressTries.keysWithPrefix(addressInput).iterator().next();
                    streetName = streetInfo.substring(0, streetInfo.length() - 4);
                }

                if (addressInput.equals(streetName)) {

                    if (it.hasNext()) {
                        localAllSuggestions = addressTries.get(it.next());
                    }
                    if (localAllSuggestions.size() > 0) {
                        for (OsmAddress osmAddress : localAllSuggestions) {
                            String address = osmAddress.toString();

                            if (extended) {
                                shownSuggestionsDestination.add(address);
                            } else {
                                shownSuggestionsOrigin.add(address);
                            }
                        }
                    }
                } else {

                    localAllSuggestions = new ArrayList<>();

                    while (it.hasNext()) {
                        localAllSuggestions.add(addressTries.get(it.next()).get(0));
                    }
                    for (OsmAddress osmAddress : localAllSuggestions) {
                        String address = osmAddress.omitHouseNumberToString();

                        if (extended) {
                            shownSuggestionsDestination.add(address);
                        } else {
                            shownSuggestionsOrigin.add(address);
                        }
                    }
                }
                if (extended) {
                    allSuggestionsDestination = localAllSuggestions;
                } else {
                    allSuggestionsOrigin = localAllSuggestions;
                }
                return null;
            }
        };

        addressSuggestionTask.setOnSucceeded(e -> displayAddressSuggestions(suggestions, textArea, extended));
        addressSuggestionTask.setOnFailed(e -> addressSuggestionTask.getException().printStackTrace());
        Thread thread = new Thread(addressSuggestionTask);
        thread.start();
    }

    private String findLongestSubstringWithMatches(String addressInput) {

        int endIndex = addressInput.length() - 1;
        while (endIndex > 0) {
            String subStringInput = addressInput.substring(0, endIndex);
            Iterator<String> it = addressTries.keysWithPrefix(subStringInput).iterator();

            if (it.hasNext()) {
                return subStringInput;
            } else {
                endIndex--;
            }
        }
        return addressInput;
    }

    @FXML
    public void switchAddressText() {
        List<OsmAddress> temp = allSuggestionsOrigin;
        allSuggestionsOrigin = allSuggestionsDestination;
        allSuggestionsDestination = temp;

        String s = originTextArea.getText();
        originTextArea.setText(destinationTextArea.getText());
        destinationTextArea.setText(s);
    }

    private class ToggleTransportListener implements EventHandler<ActionEvent> {

        private final TransportOption transportOption;
        private final ToggleButton toggleButton;

        public ToggleTransportListener(TransportOption transportOption,
                                       ToggleButton toggleButton) {

            this.transportOption = transportOption;
            this.toggleButton = toggleButton;
        }

        @Override
        public void handle(ActionEvent event) {
            transOptions.setCurrentlyEnabled(transportOption);
            toggleButton.setSelected(true);

            System.out.println("Selected TransportOption."+transportOption.toString());
        }
    }

    @FXML
    public void expandNavigationBox() {
        setSearchBoxVisible(false);
        setRouteBoxVisible(true);

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);

        if (addressTextArea.getText() != null) {
            destinationTextArea.setText(addressTextArea.getText());
            originTextArea.setText("");
            allSuggestionsDestination = allSuggestionsOrigin;
        }
    }

    @FXML
    public void minimizeNavigationBox() {
        setRouteBoxVisible(false);
        setSearchBoxVisible(true);

        if (!destinationTextArea.getText().isEmpty() && originTextArea.getText().isEmpty()) {
            addressTextArea.setText(destinationTextArea.getText());

        } else if (!originTextArea.getText().isEmpty()) {
            addressTextArea.setText(originTextArea.getText());
        }

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);
    }

    public void setSearchBoxVisible(boolean visible) {
        searchBox.setVisible(visible);
        searchBox.setManaged(visible);

        if (visible) {
            searchBox.requestFocus();
            isNavigationBoxExpanded = false;
        }
    }

    public void setRouteBoxVisible(boolean visible) {
        routeBox.setVisible(visible);
        routeBox.setManaged(visible);

        if (visible) {
            routeBox.requestFocus();
            isNavigationBoxExpanded = true;
        }
    }

    private void updateAddressTries() {
        MapCanvas mapCanvas = mainController.getCanvas();
        Model model = mapCanvas.getModel();
        MapData mapData = model.getMapData();
        addressTries = mapData.getAddressTries();
    }

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
        routeBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    public void deleteUserActions(ActionEvent actionEvent) {
        originTextArea.setText("");
        destinationTextArea.setText("");
        addressTextArea.setText("");

        suggestionsBox.getChildren().clear();
        originSuggestionsBox.getChildren().clear();
        destinationSuggestionsBox.getChildren().clear();

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);
    }
}
