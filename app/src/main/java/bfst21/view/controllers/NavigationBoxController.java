package bfst21.view.controllers;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.osm.OsmAddress;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;


public class NavigationBoxController extends NavigationSubController {

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
    private VBox navigationBox;
    @FXML
    private VBox startingSuggestions;
    @FXML
    private VBox destinationSuggestions;

    private final TransportOptions transOptions = TransportOptions.getInstance();

    @FXML
    public void searchNavigationAddresses() {
        if (startingPoint.getText().trim().equals("")) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Starting point search field is empty");

        } else if (destinationPoint.getText().trim().equals("")) {
            displayAlert(Alert.AlertType.ERROR, "Error", "Destination point search field is empty");

        } else {
            String startingAddress = startingPoint.getText().trim().toLowerCase();
            String destinationAddress = destinationPoint.getText().trim().toLowerCase();

            startingSuggestions.getChildren().clear();
            destinationSuggestions.getChildren().clear();

            checkTries();

            float[] sCoords = null;
            float[] dCoords = null;

            for (OsmAddress osmAddressS : getAllSuggestions()) {
                if (osmAddressS.toString().toLowerCase().contains(startingAddress)
                        || osmAddressS.omitHouseNumberToString().toLowerCase().contains(startingAddress)) {
                    sCoords = new float[]{osmAddressS.getNode().getX(), osmAddressS.getNode().getY()};
                    break;
                }
            }

            for (OsmAddress osmAddressD : getAllSuggestionsDestSpecific()) {
                System.out.println("Destination, address check: " + osmAddressD.toString());
                if (osmAddressD.toString().toLowerCase().contains(destinationAddress)
                        || osmAddressD.omitHouseNumberToString().toLowerCase().contains(destinationAddress)) {
                    dCoords = new float[]{osmAddressD.getNode().getX(), osmAddressD.getNode().getY()};
                    break;
                }
            }

            if (sCoords != null && dCoords != null) {

                mainController.getCanvas().setGreyPinCoords(sCoords[0], sCoords[1]);
                mainController.getCanvas().setGreyPinVisible(true);

                mainController.getCanvas().setRedPinCoords(dCoords[0], dCoords[1]);
                mainController.getCanvas().setRedPinVisible(true);

                float avgX = (sCoords[0] + dCoords[0]) / 2;
                float avgY = (sCoords[1] + dCoords[1]) / 2;

                mainController.getCanvas().changeView(avgX, avgY);
                //TODO while(rangeSearch(getBoundingBox))
            }


//            String originAddress = startingPoint.getText();
//            float[] originCoords = addressTries.get(originAddress);
//
//            String destinationAddress = destinationPoint.getText();
//            float[] destinationCoords = addressTries.get(destinationAddress);
//
//            if (originCoords != null && destinationCoords != null) {
//
//                float[] nearOriginCoords = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(originCoords);
//                float[] nearDestinationCoords = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(destinationCoords);
//
//                mainController.getCanvas().getModel().getMapData().originCoords = nearOriginCoords;
//                mainController.getCanvas().getModel().getMapData().destinationCoords = nearDestinationCoords;
//                mainController.getCanvas().getModel().getMapData().runDijkstra();
//
//            } else {
//                System.out.println("Invalid address");
//            }
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


    public void typingCheck(KeyEvent keyEvent) {
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

    @FXML
    public void minimizeSearchView() {
        mainController.setNavigationBoxVisible(false);
        mainController.setSearchBoxVisible(true);

        if (!destinationPoint.getText().isEmpty() && startingPoint.getText().isEmpty()) {
            mainController.setSearchBoxAddressText(destinationPoint.getText());

        } else if (!startingPoint.getText().isEmpty()) {
            mainController.setSearchBoxAddressText(startingPoint.getText());
        }

        mainController.getCanvas().setRedPinVisible(false);
        mainController.getCanvas().setGreyPinVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        navigationBox.setVisible(visible);
        navigationBox.setManaged(visible);

        if (visible) {
            navigationBox.requestFocus();
        }
    }

    public void transferAddressText(String address) {
        destinationPoint.setText(address);
        startingPoint.setText("");
    }

    public void onWindowResize(Stage stage) {
        navigationBox.setMaxWidth(stage.getWidth() * 0.25D);
    }
}
