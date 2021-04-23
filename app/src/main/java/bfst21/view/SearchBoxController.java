package bfst21.view;

import bfst21.address.Address;
import bfst21.address.TST;
import bfst21.exceptions.IllegalInputException;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.TransportationOption;
import bfst21.models.TransportationOptions;
import bfst21.osm.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class SearchBoxController {

    @FXML
    private TextArea addressArea;
    @FXML
    private Button navigateButton;
    @FXML
    private Button searchButton;
    @FXML
    private VBox searchAddressVBox;
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
    private VBox navigationVBox;

    private TST<Node> streetTries;

    private Controller controller;

    @FXML
    private VBox searchBox;

    @FXML
    public void searchNavigationAddresses() {
        if (startingPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", startingPoint.getId());

        } else if (destinationPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", destinationPoint.getId());

        } else {
            String sAddress = startingPoint.getText();
            Address parsedSA = Address.parse(sAddress);

            String dAddress = destinationPoint.getText();
            Address parsedDA = Address.parse(dAddress);

            System.out.println(parsedSA.toString());
            System.out.println(parsedDA.toString());
        }
    }

    @FXML
    private void searchSingleAddress() {
        if (!addressArea.getText().trim().equals("")) {
            String address = addressArea.getText();
            Address parsed = Address.parse(address);
            System.out.println(parsed);
        } else {
            throw new IllegalInputException("Search field is empty");
        }
    }

    @FXML
    public void expandSearchView(ActionEvent actionEvent) {
        if (actionEvent.toString().contains("navigateButton")) {
            if (addressArea.getText() != null) {
                destinationPoint.setText(addressArea.getText());
                startingPoint.setText("");
            }
            searchAddressVBox.setVisible(false);
            searchAddressVBox.setManaged(false);
            navigationVBox.setVisible(true);
            navigationVBox.setManaged(true);
            //To avoid any TextArea being activated
            navigationVBox.requestFocus();
        } else {
            if (!destinationPoint.getText().equals("") && startingPoint.getText().equals("")) {
                addressArea.setText(destinationPoint.getText());
            } else if (!startingPoint.getText().equals("")) {
                addressArea.setText(startingPoint.getText());
            }
            searchAddressVBox.setVisible(true);
            searchAddressVBox.setManaged(true);
            navigationVBox.setVisible(false);
            navigationVBox.setManaged(false);
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
            if (keyEvent.getSource().toString().contains("addressArea")) {
                addressArea.setText(addressArea.getText().trim());
                navigateButton.requestFocus();
            } else if (keyEvent.getSource().toString().contains("startingPoint")) {
                startingPoint.setText(startingPoint.getText().trim());
                destinationPoint.requestFocus();
            } else if (keyEvent.getSource().toString().contains("destinationPoint")) {
                destinationPoint.setText(destinationPoint.getText().trim());
                startingPoint.setText(startingPoint.getText().trim());
                switchButton.requestFocus();
            }
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            if (navigationVBox.isVisible()) {
                searchButtonExpanded.requestFocus();
                startingPoint.setText(startingPoint.getText().trim());
                destinationPoint.setText(destinationPoint.getText().trim());
                searchNavigationAddresses();
            } else {
                addressArea.setText(addressArea.getText().trim());
                searchSingleAddress();
                searchButton.requestFocus();
            }
        } else {
            if (addressArea.getText().trim().length() >= 4) {
                if (streetTries == null) {
                    MapCanvas mapCanvas = controller.getCanvas();
                    Model model = mapCanvas.getModel();
                    MapData mapData = model.getMapData();
                    streetTries = mapData.getStreetTries();
                }

                //for (String s : streetTries.keys()) {
                //    System.out.println("a: "+s);
                //}

                Iterable<String> list = streetTries.keysWithPrefix(addressArea.getText());
                for (String s : list) {
                    System.out.println("b: "+s);
                }
            }
        }
    }

    public void onWindowResize(Stage stage) {
        searchBox.setMaxWidth(stage.getWidth() * 0.25D);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
