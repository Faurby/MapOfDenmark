package bfst21.vector;

import javafx.geometry.Point2D;

public class Controller {
	private Model model;
    private View view;
    private Point2D lastMouse;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        view.canvas.setOnMousePressed(e -> {
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        view.canvas.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouse.getX();
            double dy = e.getY() - lastMouse.getY();
            if (e.isPrimaryButtonDown()) {
                view.pan(dx, dy);
            } else {
                var from = view.mouseToModelCoords(lastMouse);
                var to = view.mouseToModelCoords(new Point2D(e.getX(), e.getY()));
                model.add(new Line(from, to));
                view.repaint();
            }
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        view.canvas.setOnScroll(e -> {
            double factor = Math.pow(1.01, e.getDeltaY());
            view.zoom(factor, new Point2D(e.getX(), e.getY()));
        });
	}
}
