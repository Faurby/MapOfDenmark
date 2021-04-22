package bfst21.view;

import bfst21.address.Address;
import bfst21.exceptions.IllegalInputException;
import bfst21.models.TransportationOption;
import bfst21.models.TransportationOptions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

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
    private Button minimizeButton;
    @FXML
    private Button searchButtonExpanded;
    @FXML
    private VBox navigationVBox;


    @FXML
    public void searchNavigationAddresses() {
        if(startingPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", startingPoint.toString());
        }
        else if(destinationPoint.getText().trim().equals("")) {
            throw new IllegalInputException("Search field is empty", destinationPoint.toString());
        }
        else {
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
        if(!addressArea.getText().trim().equals("")) {
            String address = addressArea.getText();
            Address parsed = Address.parse(address);
            System.out.println(parsed);
        }
        else {
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

    public void tabEnterCheck(KeyEvent keyEvent) {
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
        }
    }
}
