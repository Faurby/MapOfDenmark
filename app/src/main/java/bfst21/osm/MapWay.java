package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.Arrays;


/**
 * MapWay is an ordered list of coordinates that
 * represents a line such as a road, river, wall, etc...
 * <p>
 * Extends BoundingBoxElement so it can be placed in a KD-tree.
 */
public class MapWay extends BoundingBoxElement implements Drawable, Serializable {

    private static final long serialVersionUID = -1748520848381396239L;

    /**
     * Draw a Way by iterating through all the coordinates.
     * At certain zoom levels, nodes may be skipped to increase drawing performance.
     * <p>
     * To avoid incorrect drawings, the first and last coordinate
     * will always be drawn, no matter the amount of nodes to skip.
     */
    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(coords[0], coords[1]);

        int nodeSkipAmount = getNodeSkipAmount(zoomLevel);
        int size = coords.length;

        for (int i = 2; i < (size - 2); i += 2 * nodeSkipAmount) {
            gc.lineTo(coords[i], coords[i + 1]);
        }
        gc.lineTo(coords[size - 2], coords[size - 1]);
    }

    public static int getNodeSkipAmount(double zoomLevel) {
        if (zoomLevel <= 100.0D) {
            return 10;
        } else if (zoomLevel <= 140.0D) {
            return 9;
        } else if (zoomLevel <= 190.0D) {
            return 8;
        } else if (zoomLevel <= 270.0D) {
            return 7;
        } else if (zoomLevel <= 350.0D) {
            return 6;
        } else if (zoomLevel <= 500.0D) {
            return 5;
        } else if (zoomLevel <= 700.0D) {
            return 4;
        } else if (zoomLevel <= 950.0D) {
            return 3;
        } else if (zoomLevel <= 1_350.0D) {
            return 2;
        }
        return 1;
    }

    public float[] getCoords() {
        return coords;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coords == null) ? 0 : Arrays.hashCode(coords));
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
        MapWay other = (MapWay) obj;
        if (coords == null) {
            return other.coords == null;
        } else {
            return Arrays.equals(coords, other.coords);
        }
    }
}
