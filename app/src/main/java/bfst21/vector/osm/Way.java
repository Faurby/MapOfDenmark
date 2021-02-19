package bfst21.vector.osm;

import java.util.ArrayList;
import java.util.List;

import bfst21.vector.Drawable;
import javafx.scene.canvas.GraphicsContext;

public class Way implements Drawable {
    private List<Node> nodes = new ArrayList<>();

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());
        for (var node : nodes) {
            gc.lineTo(node.getX(), node.getY());
        }
        gc.stroke();
    }
}
