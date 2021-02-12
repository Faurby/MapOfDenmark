package bfst21.vector;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

public class View {
    Model model;
    Stage stage;
    Canvas canvas = new Canvas(640, 480);
    BorderPane pane = new BorderPane(canvas);
    Scene scene = new Scene(pane);
    Affine trans = new Affine();
    boolean fancy;

    public View(Model model, Stage stage) {
        this(model, stage, false);
    }

    public View(Model model, Stage stage, boolean fancy) {
        this.fancy = fancy;
        this.model = model;
        model.addObserver(this::repaint);
        this.stage = stage;
        stage.setScene(scene);
        stage.setTitle("Awesome drawing program");
        stage.show();
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        repaint();
    }

    void repaint() {
        var gc = canvas.getGraphicsContext2D();
        gc.save();
        if (fancy) {
            gc.setTransform(new Affine());
            gc.setFill(Color.GREEN);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setTransform(trans);
            gc.setLineWidth(12);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setStroke(Color.BROWN);
            for (Line line : model)
                line.draw(gc);
            gc.setLineWidth(10);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.setStroke(Color.BLACK);
            for (Line line : model)
                line.draw(gc);
            gc.setLineWidth(1);
            gc.setLineDashes(2, 5);
            gc.setLineCap(StrokeLineCap.SQUARE);
            gc.setStroke(Color.WHITE);
            for (Line line : model)
                line.draw(gc);
        } else {
            gc.setTransform(new Affine());
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setStroke(Color.BLACK);
            for (Line line : model)
                line.draw(gc);
        }
        gc.restore();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    public void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        repaint();
    }

    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
	}
}
