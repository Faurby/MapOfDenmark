package bfst21.pathfinding;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;


public class Edge implements Serializable {

    private static final long serialVersionUID = -8885206149678561745L;

    private final float weight;
    private final int from, to;
    private String name;

    private final boolean canDrive, canBike, canWalk;

    public Edge(String name,
                int from,
                int to,
                float weight,
                boolean canDrive,
                boolean canBike,
                boolean canWalk) {

        if (name != null) {
            this.name = name.intern();
        }
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.canDrive = canDrive;
        this.canBike = canBike;
        this.canWalk = canWalk;
    }

    public void draw(DirectedGraph directedGraph, GraphicsContext gc) {
        float[] fromCoords = directedGraph.getVertexCoords(from);
        float[] toCoords = directedGraph.getVertexCoords(to);

        if (fromCoords != null && toCoords != null) {
            gc.moveTo(fromCoords[0], fromCoords[1]);
            gc.lineTo(toCoords[0], toCoords[1]);
        }
    }

    public boolean canNavigate() {
        TransportOption transportOption = TransportOptions.getInstance().getCurrentlyEnabled();

        if (canDrive && transportOption == TransportOption.CAR) {
            return true;

        } else if (canBike && transportOption == TransportOption.BIKE) {
            return true;

        } else return canWalk && transportOption == TransportOption.WALK;
    }

    public boolean canWalk() {
        return canWalk;
    }

    public boolean canBike() {
        return canBike;
    }

    public boolean canDrive() {
        return canDrive;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public float getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }
}
