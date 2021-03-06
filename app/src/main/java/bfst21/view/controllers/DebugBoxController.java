package bfst21.view.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;


public class DebugBoxController extends SubController {

    @FXML
    private Text zoomText;
    @FXML
    private Text repaintTime;
    @FXML
    private Text nodeSkipAmount;
    @FXML
    private Text mouseCoordsText;
    @FXML
    private Text realMouseCoordsText;

    @FXML
    public void onCheckDebug(ActionEvent actionEvent) {
        mainController.checkDebug(actionEvent);
    }

    public void setZoomText(String zoomText) {
        this.zoomText.setText(zoomText);
    }

    public void setRepaintTime(String repaintTime) {
        this.repaintTime.setText(repaintTime);
    }

    public void setNodeSkipAmount(String nodeSkipAmount) {
        this.nodeSkipAmount.setText(nodeSkipAmount);
    }

    public void setMouseCoords(String mouseCoords) {
        this.mouseCoordsText.setText(mouseCoords);
    }

    public void setMouseCoordsRealWorld(String mouseCoords) {
        this.realMouseCoordsText.setText(mouseCoords);
    }
}
