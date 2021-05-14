package bfst21.view.controllers;

import bfst21.address.TST;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.address.OsmAddress;
import bfst21.osm.Pin;
import bfst21.view.MapCanvas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    private ScrollPane originScrollPane;
    @FXML
    private ScrollPane destinationScrollPane;
    @FXML
    private ScrollPane addressScrollPane;
    @FXML
    private Button searchButton;
    @FXML
    private Button deleteTextButton;
    @FXML
    private VBox searchBox;
    @FXML
    private VBox suggestionsBox;
    @FXML
    private VBox navigationDescriptionBox;
    @FXML
    private ListView<String> navigationListView;
    @FXML
    private Text routeDetails;

    private final TransportOptions transOptions = TransportOptions.getInstance();

    private final List<OsmAddress> allSuggestionsOrigin = new ArrayList<>();
    private final List<OsmAddress> allSuggestionsDestination = new ArrayList<>();

    private final List<String> shownSuggestionsOrigin = new ArrayList<>();
    private final List<String> shownSuggestionsDestination = new ArrayList<>();

    private Task<Void> addressSuggestionTask;
    private Task<Void> dijkstraTask;

    private TST<List<OsmAddress>> addressTries;

    private boolean isNavigationBoxExpanded = false;

    public void initialize() {
        selectWalkButton.setOnAction(new ToggleTransportListener(TransportOption.WALK, selectWalkButton));
        selectBikeButton.setOnAction(new ToggleTransportListener(TransportOption.BIKE, selectBikeButton));
        selectCarButton.setOnAction(new ToggleTransportListener(TransportOption.CAR, selectCarButton));

        originTextArea.setOnMouseClicked(event -> {
            destinationScrollPane.setVisible(false);
            destinationScrollPane.setManaged(false);
            originScrollPane.setVisible(true);
            originScrollPane.setManaged(true);
        });

        destinationTextArea.setOnMouseClicked(event -> {
            originScrollPane.setVisible(false);
            originScrollPane.setManaged(false);
            destinationScrollPane.setVisible(true);
            destinationScrollPane.setManaged(true);
        });

        //Necessary if destinationTextArea is reached from originTextArea without clicking
        destinationTextArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                destinationScrollPane.setVisible(true);
                destinationScrollPane.setManaged(true);
            }
        });

        addressTextArea.setOnMouseClicked(event -> {
            addressScrollPane.setVisible(true);
            addressScrollPane.setManaged(true);
        });
    }

    @FXML
    public void searchSingleAddress() {
        String address = addressTextArea.getText().trim().toLowerCase();
        suggestionsBox.getChildren().clear();

        if (!address.isEmpty()) {

            for (OsmAddress osmAddress : allSuggestionsOrigin) {
                if (osmAddress.matches(address)) {

                    addressTextArea.setText(osmAddress.toString());
                    Pin.DESTINATION.setCoords(osmAddress.getNodeCoords());
                    Pin.DESTINATION.setVisible(true);

                    mainController.getCanvas().changeView(osmAddress.getNode().getX(), osmAddress.getNode().getY());
                    return;
                }
            }
            displayAlert(Alert.AlertType.ERROR, "Error", "Unable to find address: " + address);
        } else {
            displayAlert(Alert.AlertType.ERROR, "Error", "Please enter an address into the search field");
        }
    }

    public void typingCheck(KeyEvent keyEvent) {
        if (!isNavigationBoxExpanded) {

            if (keyEvent.getCode() == KeyCode.TAB) {
                deleteTextButton.requestFocus();

            } else if (keyEvent.getCode() == KeyCode.ENTER) {

                suggestionsBox.getChildren().clear();
                searchButton.requestFocus();
                searchSingleAddress();

            } else if (keyEvent.getCode() == KeyCode.DOWN && shownSuggestionsOrigin.size() > 0) {
                suggestionsBox.getChildren().get(0).requestFocus();

            } else {
                int textLength = addressTextArea.getText().trim().length();

                if (addressTextArea.getText().trim().length() <= 2) {
                    suggestionsBox.getChildren().clear();

                } else if (!(keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE) && textLength >= 2) {
                    runAddressSuggestionTask(suggestionsBox, addressTextArea, false, addressScrollPane);
                }
            }
        } else {
            if (keyEvent.getCode() == KeyCode.TAB) {

                if (keyEvent.getSource().toString().contains("originTextArea")) {
                    originTextArea.setText(originTextArea.getText().trim());
                    originScrollPane.setVisible(false);
                    originScrollPane.setManaged(false);
                    destinationScrollPane.setVisible(true);
                    destinationScrollPane.setManaged(true);
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

            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                if (keyEvent.getSource().toString().contains("originTextArea") && shownSuggestionsOrigin.size() > 0) {
                    originSuggestionsBox.getChildren().get(0).requestFocus();
                } else if (keyEvent.getSource().toString().contains("destinationTextArea") && shownSuggestionsDestination.size() > 0) {
                    destinationSuggestionsBox.getChildren().get(0).requestFocus();
                }
            } else {
                if (keyEvent.getSource().toString().contains("originTextArea")) {
                    int textLength = originTextArea.getText().trim().length();

                    if (textLength <= 2) {
                        originSuggestionsBox.getChildren().clear();

                    } else if (!(keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE)) {
                        runAddressSuggestionTask(originSuggestionsBox, originTextArea, false, originScrollPane);
                    }

                } else if (keyEvent.getSource().toString().contains("destinationTextArea")) {
                    int textLength = destinationTextArea.getText().trim().length();

                    if (textLength <= 2) {
                        destinationSuggestionsBox.getChildren().clear();

                    } else if (!(keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE)) {
                        runAddressSuggestionTask(destinationSuggestionsBox, destinationTextArea, true, destinationScrollPane);
                    }
                }
            }
        }
    }

    @FXML
    public void findRoute() {
        if (originTextArea.getText().trim().isEmpty() && destinationTextArea.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Please enter an address for the starting and destination point");

        } else if (originTextArea.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Please enter an address for the starting point");

        } else if (destinationTextArea.getText().trim().isEmpty()) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Please enter an address for the destination point");

        } else {
            String startingAddress = originTextArea.getText().trim().toLowerCase();
            String destinationAddress = destinationTextArea.getText().trim().toLowerCase();

            originSuggestionsBox.getChildren().clear();
            destinationSuggestionsBox.getChildren().clear();

            float[] originCoords = null;
            float[] destinationCoords = null;

            for (OsmAddress osmAddress : allSuggestionsOrigin) {
                if (osmAddress.matches(startingAddress)) {
                    originTextArea.setText(osmAddress.toString());
                    originCoords = new float[]{osmAddress.getNode().getX(), osmAddress.getNode().getY()};
                    break;
                }
            }

            for (OsmAddress osmAddress : allSuggestionsDestination) {
                if (osmAddress.matches(destinationAddress)) {
                    destinationTextArea.setText(osmAddress.toString());
                    destinationCoords = new float[]{osmAddress.getNode().getX(), osmAddress.getNode().getY()};
                    break;
                }
            }

            if (originCoords != null && destinationCoords != null) {
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
                runDijkstraTask();
            }
        }
    }

    /**
     * Run dijkstra path finding if coords for origin and destination are present.
     */
    public void runDijkstraTask() {
        if (dijkstraTask != null) {
            if (dijkstraTask.isRunning()) {
                dijkstraTask.cancel();
            }
        }
        dijkstraTask = new Task<>() {
            @Override
            protected Void call() {
                mainController.getCanvas().runDijkstra();
                return null;
            }
        };
        dijkstraTask.setOnSucceeded(e -> {
            mainController.getCanvas().repaint();

            List<String> directionsList = mainController.getCanvas().getCurrentDirections();
            if (directionsList != null) {
                ObservableList<String> tempList = FXCollections.observableArrayList();
                tempList.addAll(directionsList);
                navigationListView.setItems(tempList);
            }

            navigationDescriptionBox.setVisible(true);
            navigationDescriptionBox.setManaged(true);

            routeDetails.setText(mainController.getCanvas().getCurrentRouteDuration() + "\n" + mainController.getCanvas().getRouteDistanceToString());

            int navListSize = navigationListView.getItems().size();
            if (navListSize == 1) {
                navigationListView.setMaxHeight(27.0D);
                navigationListView.setMinHeight(27.0D);

            } else if (navListSize < 15) {
                navigationListView.setMaxHeight(navListSize * 24.0D);
                navigationListView.setMinHeight(navListSize * 24.0D);
            } else {
                navigationListView.setMaxHeight(350.0D);
                navigationListView.setMinHeight(350.0D);
            }


        });
        dijkstraTask.setOnFailed(e -> dijkstraTask.getException().printStackTrace());

        Thread thread = new Thread(dijkstraTask);
        thread.start();
    }

    private void displayAddressSuggestions(VBox suggestions, TextArea textArea, boolean extended, ScrollPane scrollPane) {
        int count = 0;
        suggestions.getChildren().clear();

        List<String> localShownSuggestions = shownSuggestionsOrigin;
        if (extended) {
            localShownSuggestions = shownSuggestionsDestination;
        }

        for (String s : localShownSuggestions) {
            if (count <= 1000) {
                Label label = new Label(s);
                label.setPrefWidth(800.0D);
                label.setOnMouseClicked((event) -> {
                    textArea.setText(label.getText());
                    suggestions.getChildren().clear();
                    textArea.requestFocus();
                    textArea.end();
                });
                label.setOnMouseEntered((event) -> label.setStyle("-fx-background-color:#dae7f3;"));
                label.setOnMouseExited((event) -> label.setStyle("-fx-background-color: transparent;"));
                label.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        label.setStyle("-fx-background-color:#dae7f3;");
                    } else {
                        label.setStyle("-fx-background-color: transparent;");
                    }
                });

                //This line is necessary to remove other functionality
                suggestions.setOnKeyPressed((event) -> {
                });

                label.setOnKeyPressed((event) -> {
                    if (event.getCode() == KeyCode.UP && suggestions.getChildren().size() > 0 && suggestions.getChildren().indexOf(label) > 0) {
                        Node previous = suggestions.getChildren().get(suggestions.getChildren().indexOf(label) - 1);
                        previous.requestFocus();
                        centerLabelInScrollPane(scrollPane, previous);

                    } else if ((event.getCode() == KeyCode.DOWN && suggestions.getChildren().size() > 0) && suggestions.getChildren().indexOf(label) < suggestions.getChildren().size() - 1) {
                        Node next = suggestions.getChildren().get(suggestions.getChildren().indexOf(label) + 1);
                        next.requestFocus();
                        centerLabelInScrollPane(scrollPane, next);

                    } else if (event.getCode() == KeyCode.ENTER) {
                        textArea.setText(label.getText());
                        scrollPane.setVisible(false);
                        scrollPane.setManaged(false);
                    }
                });

                suggestions.getChildren().add(label);
                count++;
            }
        }
    }

    //TODO this is directly from stackoverflow, should I rewrite it more?
    public void centerLabelInScrollPane(ScrollPane scrollPane, Node node) {
        double h = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
        double v = scrollPane.getViewportBounds().getHeight();

        scrollPane.setVvalue(scrollPane.getVmax() * ((y - 0.5 * v) / (h - v)));
    }

    public void updateSuggestions(String input, List<String> suggestions, List<OsmAddress> osmSuggestions) {
        suggestions.clear();
        osmSuggestions.clear();
        updateAddressTries();

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
                for (OsmAddress osmAddress : addressTries.get(it.next())) {
                    osmSuggestions.add(osmAddress);

                    String address = osmAddress.toString();
                    suggestions.add(address);
                }
            }
        } else {
            while (it.hasNext()) {
                osmSuggestions.add(addressTries.get(it.next()).get(0));
            }
            for (OsmAddress osmAddress : osmSuggestions) {
                String address = osmAddress.omitHouseNumberToString();

                suggestions.add(address);
            }
        }
    }

    private void runAddressSuggestionTask(VBox suggestions, TextArea textArea, boolean extended, ScrollPane scrollPane) {
        if (addressSuggestionTask != null) {
            if (addressSuggestionTask.isRunning()) {
                addressSuggestionTask.cancel();
            }
        }
        addressSuggestionTask = new Task<>() {

            @Override
            protected Void call() {
                String input = textArea.getText();

                if (extended) {
                    updateSuggestions(input, shownSuggestionsDestination, allSuggestionsDestination);
                } else {
                    updateSuggestions(input, shownSuggestionsOrigin, allSuggestionsOrigin);
                }
                return null;
            }
        };

        addressSuggestionTask.setOnSucceeded(e -> displayAddressSuggestions(suggestions, textArea, extended, scrollPane));
        addressSuggestionTask.setOnFailed(e -> addressSuggestionTask.getException().printStackTrace());
        Thread thread = new Thread(addressSuggestionTask);
        thread.start();
    }

    private String findLongestSubstringWithMatches(String addressInput) {

        int endIndex = addressInput.length() - 1;
        while (endIndex >= 1) {
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
        allSuggestionsOrigin.clear();
        allSuggestionsOrigin.addAll(allSuggestionsDestination);
        allSuggestionsDestination.clear();
        allSuggestionsDestination.addAll(temp);

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

            System.out.println("Selected TransportOption." + transportOption.toString());
        }
    }

    @FXML
    public void expandNavigationBox() {
        setSearchBoxVisible(false);
        setRouteBoxVisible(true);
        navigationDescriptionBox.setVisible(false);
        navigationDescriptionBox.setManaged(false);

        if (mainController.getCanvas().getCurrentDirections() != null) {
            if (mainController.getCanvas().getCurrentDirections().size() > 0) {

                navigationDescriptionBox.setVisible(true);
                navigationDescriptionBox.setManaged(true);
            }
        }

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);

        if (addressTextArea.getText() != null) {
            destinationTextArea.setText(addressTextArea.getText());
            originTextArea.setText("");
            allSuggestionsDestination.clear();
            allSuggestionsDestination.addAll(allSuggestionsOrigin);
        }
    }

    @FXML
    public void minimizeNavigationBox() {
        navigationListView.getItems().removeAll();
        setRouteBoxVisible(false);
        setSearchBoxVisible(true);

        if (!destinationTextArea.getText().isEmpty() && originTextArea.getText().isEmpty()) {
            addressTextArea.setText(destinationTextArea.getText());
            allSuggestionsOrigin.clear();
            allSuggestionsOrigin.addAll(allSuggestionsDestination);

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

    @FXML
    public void clearRoute() {
        originTextArea.setText("");
        destinationTextArea.setText("");
        addressTextArea.setText("");

        suggestionsBox.getChildren().clear();
        originSuggestionsBox.getChildren().clear();
        destinationSuggestionsBox.getChildren().clear();
        routeDetails.setText("");
        navigationDescriptionBox.setVisible(false);
        navigationDescriptionBox.setManaged(false);

        Pin.ORIGIN.setVisible(false);
        Pin.DESTINATION.setVisible(false);

        mainController.getCanvas().originCoords = null;
        mainController.getCanvas().destinationCoords = null;
        mainController.getCanvas().repaint();
    }
}
