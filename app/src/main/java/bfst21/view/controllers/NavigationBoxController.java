package bfst21.view.controllers;

import bfst21.address.TST;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.osm.OsmAddress;
import bfst21.osm.Pin;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
    private TextArea startingPoint;
    @FXML
    private TextArea destinationPoint;
    @FXML
    private ToggleButton CAR;
    @FXML
    private ToggleButton BIKE;
    @FXML
    private ToggleButton WALK;
    @FXML
    private Button switchButton;
    @FXML
    private Button searchButtonExpanded;
    @FXML
    private VBox routeBox;
    @FXML
    private VBox startingSuggestions;
    @FXML
    private VBox destinationSuggestions;
    @FXML
    private TextArea addressArea;
    @FXML
    private Button directionsButton;
    @FXML
    private Button searchButton;
    @FXML
    private VBox searchBox;
    @FXML
    private VBox suggestions;

    private final TransportOptions transOptions = TransportOptions.getInstance();

    private List<OsmAddress> allSuggestions = new ArrayList<>();
    private List<OsmAddress> allSuggestionsDestSpecific = new ArrayList<>();

    private List<String> shownSuggestions = new ArrayList<>();
    private List<String> shownSuggestionsDestSpecific = new ArrayList<>();

    private TST<List<OsmAddress>> addressTries;
    private Task<Void> addressSuggestionTask;

    private boolean searchBoxVisible = true;

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

    public void setSearchBoxVisible(boolean visible) {
        searchBox.setVisible(visible);
        searchBox.setManaged(visible);

        if (visible) {
            searchBox.requestFocus();
            searchBoxVisible = true;
        }
    }

    public void setNavigationBoxVisible(boolean visible) {
        routeBox.setVisible(visible);
        routeBox.setManaged(visible);

        if (visible) {
            routeBox.requestFocus();
            searchBoxVisible = false;
        }
    }

    @FXML
    public void expandSearchView() {
        setSearchBoxVisible(false);
        setNavigationBoxVisible(true);
        Pin.DESTINATION.setVisible(false);

        if (addressArea.getText() != null) {
            destinationPoint.setText(addressArea.getText());
            startingPoint.setText("");
            setAllSuggestionsDestSpecific(getAllSuggestions());
        }
    }

    public void typingCheck(KeyEvent keyEvent) {
        if (searchBoxVisible) {

            if (keyEvent.getCode() == KeyCode.TAB) {
                if (keyEvent.getSource().toString().contains("addressArea")) {
                    addressArea.setText(addressArea.getText().trim());
                    directionsButton.requestFocus();
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
        } else {
            if (keyEvent.getCode() == KeyCode.TAB) {

                if (keyEvent.getSource().toString().contains("startingPoint")) {
                    startingPoint.setText(startingPoint.getText().trim());
                    destinationPoint.requestFocus();

                } else if (keyEvent.getSource().toString().contains("destinationPoint")) {
                    destinationPoint.setText(destinationPoint.getText().trim());
                    startingPoint.setText(startingPoint.getText().trim());
                    switchButton.requestFocus();
                }
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                searchButtonExpanded.requestFocus();
                startingPoint.setText(startingPoint.getText().trim());
                destinationPoint.setText(destinationPoint.getText().trim());
                startingSuggestions.getChildren().clear();
                destinationSuggestions.getChildren().clear();
                searchNavigationAddresses();
            } else {
                if (keyEvent.getSource().toString().contains("startingPoint")) {
                    int textLength = startingPoint.getText().trim().length();
                    if (textLength >= 2) {
                        runAddressSuggestionTask(startingSuggestions, startingPoint, false);
                    }
                } else if (keyEvent.getSource().toString().contains("destinationPoint")) {
                    int textLength = destinationPoint.getText().trim().length();
                    if (textLength >= 2) {
                        runAddressSuggestionTask(destinationSuggestions, destinationPoint, true);
                    }
                }
            }
        }
    }

    @FXML
    public void searchNavigationAddresses() {
        if (startingPoint.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Starting point search field is empty");

        } else if (destinationPoint.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Destination point search field is empty");

        } else {
            String startingAddress = startingPoint.getText().trim().toLowerCase();
            String destinationAddress = destinationPoint.getText().trim().toLowerCase();

            startingSuggestions.getChildren().clear();
            destinationSuggestions.getChildren().clear();

            checkTries();

            float[] originCoords = null;
            float[] destinationCoords = null;

            for (OsmAddress osmAddressS : getAllSuggestions()) {
                if (osmAddressS.toString().toLowerCase().contains(startingAddress)
                        || osmAddressS.omitHouseNumberToString().toLowerCase().contains(startingAddress)) {
                    originCoords = new float[]{osmAddressS.getNode().getX(), osmAddressS.getNode().getY()};
                    break;
                }
            }

            for (OsmAddress osmAddressD : getAllSuggestionsDestSpecific()) {
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

                mainController.getCanvas().getModel().getMapData().originCoords = nearOriginCoords;
                mainController.getCanvas().getModel().getMapData().destinationCoords = nearDestinationCoords;
                mainController.getCanvas().getModel().getMapData().runDijkstraTask();
            }
        }
    }

    public void switchText() {
        List<OsmAddress> temp = getAllSuggestions();
        setAllSuggestions(getAllSuggestionsDestSpecific());
        setAllSuggestionsDestSpecific(temp);

        String s = startingPoint.getText();
        startingPoint.setText(destinationPoint.getText());
        destinationPoint.setText(s);
    }

    public void transportationButtonPushed(ActionEvent actionEvent) {
        if (actionEvent.getSource().toString().contains("WALK")) {
            transOptions.setCurrentlyEnabled(TransportOption.WALK);
            WALK.setSelected(true);

        } else if (actionEvent.getSource().toString().contains("BIKE")) {
            transOptions.setCurrentlyEnabled(TransportOption.BIKE);
            BIKE.setSelected(true);

        } else {
            transOptions.setCurrentlyEnabled(TransportOption.CAR);
            CAR.setSelected(true);
        }
        System.out.println(transOptions.getCurrentlyEnabled().toString());
    }

    @FXML
    public void minimizeSearchView() {
        setNavigationBoxVisible(false);
        setSearchBoxVisible(true);

        if (!destinationPoint.getText().isEmpty() && startingPoint.getText().isEmpty()) {
            addressArea.setText(destinationPoint.getText());

        } else if (!startingPoint.getText().isEmpty()) {
            addressArea.setText(startingPoint.getText());
        }

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);
    }

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
        routeBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    protected void displayAddressSuggestions(VBox suggestions, TextArea textArea, boolean extended) {
        int count = 0;
        suggestions.getChildren().clear();

        List<String> localShownSuggestions = shownSuggestions;
        if (extended) {
            localShownSuggestions = shownSuggestionsDestSpecific;
        }

        for (String s : localShownSuggestions) {
            if (count <= 500) {
                Label b = new Label(s);
                b.setPrefWidth(800);
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

    protected void checkTries() {
        if (addressTries == null) {
            activateTries();
        }
    }

    private void activateTries() {
        MapCanvas mapCanvas = mainController.getCanvas();
        Model model = mapCanvas.getModel();
        MapData mapData = model.getMapData();
        addressTries = mapData.getAddressTries();
    }

    protected void runAddressSuggestionTask(VBox suggestions, TextArea textArea, boolean extended) {
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
                List<OsmAddress> localAllSuggestions;
                if (extended) {
                    shownSuggestionsDestSpecific = new ArrayList<>();
                    localAllSuggestions = allSuggestionsDestSpecific;
                } else {
                    shownSuggestions = new ArrayList<>();
                    localAllSuggestions = allSuggestions;
                }

                String input = textArea.getText();
                String addressInput = input.replace(" ", "").toLowerCase();

                Iterator<String> it = addressTries.keysWithPrefix(addressInput).iterator();

                //If the string doesn't return matches, find a substring that does
                if (!it.hasNext()) {
                    addressInput = longestSubstringWithMatches(addressInput);
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
                                shownSuggestionsDestSpecific.add(address);
                            } else {
                                shownSuggestions.add(address);
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
                            shownSuggestionsDestSpecific.add(address);
                        } else {
                            shownSuggestions.add(address);
                        }
                    }
                }
                if (extended) {
                    allSuggestionsDestSpecific = localAllSuggestions;
                } else {
                    allSuggestions = localAllSuggestions;
                }
                return null;
            }
        };

        addressSuggestionTask.setOnSucceeded(e -> displayAddressSuggestions(suggestions, textArea, extended));
        addressSuggestionTask.setOnFailed(e -> addressSuggestionTask.getException().printStackTrace());
        Thread thread = new Thread(addressSuggestionTask);
        thread.start();
    }

    private String longestSubstringWithMatches(String addressInput) {
        Iterator<String> modifiedIt;
        boolean hasResults = true;
        int counter = 1;

        while (hasResults && counter < addressInput.length()) {
            modifiedIt = addressTries.keysWithPrefix(addressInput.substring(0, counter)).iterator();
            if (!modifiedIt.hasNext()) {
                hasResults = false;
                counter--;
            } else {
                counter++;
            }
        }
        return addressInput.substring(0, counter);
    }

    public List<String> getShownSuggestions() {
        return shownSuggestions;
    }

    public List<OsmAddress> getAllSuggestions() {
        return allSuggestions;
    }

    public List<OsmAddress> getAllSuggestionsDestSpecific() {
        return allSuggestionsDestSpecific;
    }

    public void setAllSuggestions(List<OsmAddress> allSuggestions) {
        this.allSuggestions = allSuggestions;
    }

    public void setAllSuggestionsDestSpecific(List<OsmAddress> allSuggestionsDestSpecific) {
        this.allSuggestionsDestSpecific = allSuggestionsDestSpecific;
    }
}
