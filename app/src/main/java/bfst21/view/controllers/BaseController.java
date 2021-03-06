package bfst21.view.controllers;

import javafx.scene.control.Alert;


public class BaseController {

    protected Alert displayAlert(Alert.AlertType alertType, String title, String contentText) {
        return displayAlert(alertType, title, "", contentText);
    }

    protected Alert displayAlert(Alert.AlertType alertType,
                                 String title,
                                 String headerText,
                                 String contentText) {

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();

        return alert;
    }
}
