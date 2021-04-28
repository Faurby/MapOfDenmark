package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class MapText {

    private String place;
    private String name;
    private float[] coords;

    public MapText(String place, String name, float[] coords) {
        this.place = place;
        this.name = name;
        this.coords = coords;
    }

    public String getPlace() {
        return place;
    }

    public String getName() {
        return name;
    }

    public float[] getCoords() {
        return coords;
    }

    public boolean canDraw(double zoomLevel) {
        if (zoomLevel >= 1000 && place.equals("city")) {
            return true;
        } else if (zoomLevel >= 10000 && place.equals("town")) {
            return true;
        } else if (zoomLevel >= 1200 && place.equals("village")) {
            return true;
        } else if (zoomLevel >= 8000 && place.equals("hamlet")) {
            return true;
        } else if (zoomLevel >= 1200 && place.equals("suburb")) {
            return true;
        } else if (zoomLevel >= 100 && place.equals("island")) {
            return true;
        } else if (zoomLevel >= 8000 && place.equals("neighbourhood")) {
            return true;
        } else if (zoomLevel >= 8000 && place.equals("city_block")) {
            return true;
        } else if (zoomLevel >= 1500 && place.equals("county")) {
            return true;
        } else if (zoomLevel >= 1500 && place.equals("plot")) {
            return true;
        }  else if (zoomLevel >= 1800 && place.equals("locality")) {
            return true;
        }
        return false;
    }
}
