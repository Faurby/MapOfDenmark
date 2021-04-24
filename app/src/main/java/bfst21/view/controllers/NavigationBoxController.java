package bfst21.view.controllers;

import bfst21.address.Address;
import bfst21.address.TriesMap;
import bfst21.exceptions.IllegalInputException;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.TransportationOption;
import bfst21.models.TransportationOptions;
import bfst21.osm.Node;
import bfst21.view.MapCanvas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


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
    private VBox navigationBox;

    private TriesMap triesMap;

    @FXML
    public void searchNavigationAddresses() {
        if (startingPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", startingPoint.getId());

        } else if (destinationPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", destinationPoint.getId());

        } else {
            if (triesMap == null) {
                MapCanvas mapCanvas = mainController.getCanvas();
                Model model = mapCanvas.getModel();
                MapData mapData = model.getMapData();
                triesMap = mapData.getTriesMap();
            }

            String originAddress = startingPoint.getText();
            Node originNode = triesMap.getNode(originAddress);

            String destinationAddress = destinationPoint.getText();
            Node destinationNode = triesMap.getNode(destinationAddress);

            if (originNode != null && destinationNode != null) {

                Node nearOrigin = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(originNode);
                Node nearDestination = mainController.getCanvas().getModel().getMapData().kdTreeNearestNeighborSearch(destinationNode);

                mainController.getCanvas().getModel().getMapData().originNode = nearOrigin;
                mainController.getCanvas().getModel().getMapData().destinationNode = nearDestination;
                mainController.getCanvas().getModel().getMapData().runDijkstra();

            } else {
                System.out.println("Invalid address");
            }
        }
    }

    public void switchText() {
        String s = startingPoint.getText();
        startingPoint.setText(destinationPoint.getText());
        destinationPoint.setText(s);
    }

    public void transportationButtonPushed(ActionEvent actionEvent) {
        TransportationOptions transOptions = new TransportationOptions();

        if (actionEvent.getSource().toString().contains("WALK")) {
            transOptions.chooseType(TransportationOption.WALK);
            WALK.setSelected(true);
        } else if (actionEvent.getSource().toString().contains("BIKE")) {
            transOptions.chooseType(TransportationOption.BIKE);
            BIKE.setSelected(true);
        } else {
            transOptions.chooseType(TransportationOption.CAR);
            CAR.setSelected(true);
        }
        System.out.println(transOptions.returnType().toString());
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
            searchNavigationAddresses();
        }
    }

    @FXML
    public void expandSearchView() {

        mainController.setNavigationBoxVisible(false);
        mainController.setSearchBoxVisible(true);

        if (!destinationPoint.getText().isEmpty() && startingPoint.getText().isEmpty()) {
            mainController.setSearchBoxAddressText(destinationPoint.getText());

        } else if (!startingPoint.getText().isEmpty()) {
            mainController.setSearchBoxAddressText(startingPoint.getText());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
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
        navigationBox.setMaxWidth(stage.getWidth() * 0.30D);
    }
}
