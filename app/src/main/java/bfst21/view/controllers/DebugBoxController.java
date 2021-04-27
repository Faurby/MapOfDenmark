package bfst21.view.controllers;

import bfst21.models.DisplayOption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class DebugBoxController extends SubController{

    @FXML
    private Text zoomText;
    @FXML
    private Text zoomPercent;
    @FXML
    private Text repaintTime;
    @FXML
    private Text nodeSkipAmount;

    @FXML
    public void onCheckDebug(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (DisplayOption displayOption : DisplayOption.values()) {
            String optionText = displayOption.toString().toLowerCase().replaceAll("_", " ");
            if (text.contains(optionText)) {
                mainController.toggleDisplayOption(displayOption);
                break;
            }
        }
    }

    public void setZoomText(String zoomText) {
        this.zoomText.setText(zoomText);
    }

    public void setZoomPercent(String zoomPercent) {
        this.zoomPercent.setText(zoomPercent);
    }

    public void setRepaintTime(String repaintTime) {
        this.repaintTime.setText(repaintTime);
    }

    public void setNodeSkipAmount(String nodeSkipAmount) {
        this.nodeSkipAmount.setText(nodeSkipAmount);
    }
}
