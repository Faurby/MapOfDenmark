package bfst21.vector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller {

    private Model model;
    private Point2D lastMouse;

    @FXML
    private MapCanvas canvas;
    @FXML
    private VBox vbox;

    public void init(Model model) {
        this.model = model;
        canvas.init(model);
    }

    public void onWindowResize(){
        vbox.setMaxWidth(canvas.getWidth()*0.25);
        System.out.println(canvas.getWidth());
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        double factor = Math.pow(1.01, e.getDeltaY());
        canvas.preZoom(factor, new Point2D(e.getX(), e.getY()));
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
            canvas.preZoom(2.0,new Point2D(canvas.getWidth()/2,canvas.getHeight()/2));
        } else {
            canvas.preZoom(0.50,new Point2D(canvas.getWidth()/2,canvas.getHeight()/2));
        }
        //TODO: tjek at zoom centreres præcis i midten af vinduet, så menubaren ikke tæller med
    }

    @FXML
    public void changeColorMode(ActionEvent actionEvent) {
        String buttonClicked = actionEvent.toString().toLowerCase();
        if(buttonClicked.contains("standard")) {
            canvas.setColorMode(ColorMode.STANDARD);
        }
        else if(buttonClicked.contains("inverted")) {
            canvas.setColorMode(ColorMode.INVERTED);
        }
        else if(buttonClicked.contains("blackwhite")) {
            canvas.setColorMode(ColorMode.BLACKWHITE);
        }
        canvas.repaint();
    }
}
