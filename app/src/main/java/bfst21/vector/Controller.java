package bfst21.vector;

import bfst21.addressparser.Address;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;


public class Controller {

    private Model model;
    private Point2D lastMouse;

    @FXML
    private MapCanvas canvas;
    @FXML
    private VBox vbox;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox zoomBox;
    @FXML
    private Scene scene;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Text zoomText;
    @FXML
    private TextArea startingPoint;
    @FXML
    private TextArea destinationPoint;

    public void init(Model model) throws IOException {
        this.model = model;
        canvas.init(model);
        stackPane.setAlignment(zoomBox, Pos.TOP_RIGHT);
        zoomText.setText(canvas.getZoomPercent());
    }

    public void onWindowResize(Stage stage) {
        stackPane.setAlignment(zoomBox, Pos.TOP_RIGHT);
        vbox.setMaxWidth(stage.getWidth() * 0.25);
        System.out.println("StackPane width: " + stackPane.getWidth());
        System.out.println("StackPane height: " + stackPane.getHeight());
        canvas.repaint();
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        double factor = Math.pow(1.01, e.getDeltaY());
        canvas.preZoom(factor, new Point2D(e.getX(), e.getY()));
        zoomText.setText(canvas.getZoomPercent());
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        if (e.isPrimaryButtonDown()) {
            canvas.pan(dx, dy);

        } else {
            Point2D from = canvas.mouseToModelCoords(lastMouse);
            Point2D to = canvas.mouseToModelCoords(new Point2D(e.getX(), e.getY()));
            model.add(new Line(from, to));
            canvas.repaint();
        }
        onMousePressed(e);
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        lastMouse = new Point2D(e.getX(), e.getY());
    }

    public void zoomButtonClicked(ActionEvent actionEvent) {

        if (actionEvent.toString().contains("zoomIn")) {
            canvas.preZoom(2.0, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
        } else {
            canvas.preZoom(0.50, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
        }

        //TODO her er begyndelsen på et fix af zoomIndikatoren oppe til højre når man bruger zoom knappen.
        // PT er den ret scuffed. Jeg er ikke sikker på functionen af 'factor'.
        double factor = 1;
        canvas.preZoom(factor, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
        zoomText.setText(canvas.getZoomPercent());
    }

    @FXML
    public void changeColorMode(ActionEvent actionEvent) {
        String buttonClicked = actionEvent.toString().toLowerCase();

        if (buttonClicked.contains("standard")) {
            canvas.setColorMode(ColorMode.STANDARD);

        } else if (buttonClicked.contains("blackwhite")) {
            canvas.setColorMode(ColorMode.BLACK_WHITE);

        } else if (buttonClicked.contains("colorblind")) {
            canvas.setColorMode(ColorMode.COLOR_BLIND);
        }
        canvas.repaint();
    }

    @FXML
    public void searchAddressString(ActionEvent actionEvent) {
        String sAddress = startingPoint.getText();
        Address parsedSA = Address.parse(sAddress);

        String dAddress = destinationPoint.getText();
        Address parsedDA = Address.parse(dAddress);

        System.out.println(parsedSA.toString());
        System.out.println(parsedDA.toString());
    }
}
