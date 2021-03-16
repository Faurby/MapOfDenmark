package bfst21.vector;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;


public class Controller {

    private Model model;
    private Point2D lastMouse;

    @FXML
    private MapCanvas canvas;
    private double zoomLevel = 1.0;
    private double zoomLevelMin = 0.018682;
    private double zoomLevelMax = 80.0;


    public void init(Model model) {
        this.model = model;
        canvas.init(model);
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        double factor = Math.pow(1.01, e.getDeltaY());
        double zoomLevelPre = zoomLevel * factor;
        if (zoomLevelPre < zoomLevelMax && zoomLevelPre > zoomLevelMin) {
            zoomLevel = zoomLevelPre;
            canvas.zoom(factor, new Point2D(e.getX(), e.getY()));
            System.out.println(zoomLevel);
        }
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
}
