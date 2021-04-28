package bfst21.view.controllers;

import javafx.event.ActionEvent;


public class StartBoxController extends SubController {
    public void loadDefault() {
        mainController.loadDefault();
    }

    public void loadFile() {
        mainController.loadNewFile();
    }

    public void onCheckDebug(ActionEvent actionEvent) {
        mainController.checkDebug(actionEvent);
    }
}
