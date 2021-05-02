package bfst21.pathfinding;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.osm.Way;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;


public class Edge implements Serializable {

    private static final long serialVersionUID = -8885206149678561745L;

    private final float weight;
    private final int from, to;
    private final Way way;

    private final boolean canDrive, canBike, canWalk;

    public Edge(Way way,
                int from,
                int to,
                float weight,
                boolean canDrive,
                boolean canBike,
                boolean canWalk) {

        this.way = way;
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

//    public void draw(DirectedGraph directedGraph, GraphicsContext gc) {
//        float[] fromCoords = directedGraph.getVertexCoords(from);
//        float[] toCoords = directedGraph.getVertexCoords(to);
//
//        float[] wayCoords = way.getCoords();
//
//        if (fromCoords != null && toCoords != null && wayCoords != null) {
//
//            gc.moveTo(fromCoords[0], fromCoords[1]);
//            boolean foundStartingPoint = false;
//
//            for (int i = 2; i < (wayCoords.length - 2); i += 2) {
//                float x = wayCoords[i];
//                float y = wayCoords[i + 1];
//
//                if (toCoords[0] == x && toCoords[1] == y) {
//                    break;
//
//                } else if (fromCoords[0] == x && fromCoords[1] == y) {
//                    foundStartingPoint = true;
//
//                } else if (foundStartingPoint) {
//                    gc.lineTo(x, y);
//                }
//            }
//            gc.lineTo(toCoords[0], toCoords[1]);
//        }
//    }

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

    public Way getWay() {
        return way;
    }
}
