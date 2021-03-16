package bfst21.vector.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import bfst21.vector.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class Way implements Drawable, Serializable {

    //Used for creating binary files
    private static final long serialVersionUID = 3139576893143362100L;
    protected List<Node> nodes = new ArrayList<>();

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public void trace(GraphicsContext gc) {
        gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());
        for (Node node : nodes) {
            gc.lineTo(node.getX(), node.getY());
        }
    }

    public static Way merge(Way first, Way second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        Way merged = new Way();
        merged.nodes.addAll(first.nodes);
        merged.nodes.addAll(second.nodes.subList(1, second.nodes.size()));
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Way other = (Way) obj;
        if (nodes == null) {
            return other.nodes == null;
        } else {
            return nodes.equals(other.nodes);
        }
    }
}
