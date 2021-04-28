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
        if (zoomLevel <= 50000) {
            if (100 <= zoomLevel && zoomLevel <= 2000 && place.equals("island")) {
                return true;
            } else if (1000 <= zoomLevel && zoomLevel <= 25000 && place.equals("city")) {
                return true;
            } else if (2000 <= zoomLevel && place.equals("islet")) {
                return true;
            } else if (6000 <= zoomLevel && zoomLevel <= 8000 && place.equals("county")) {
                return true;
            }  else if (6000 <= zoomLevel && zoomLevel <= 10000 && place.equals("municipality")) {
                return true;
            }  else if (6000 <= zoomLevel && place.equals("region")) {
                return true;
            } else if (6000 <= zoomLevel && place.equals("town")) {
                return true;
            } else if (6000 <= zoomLevel && place.equals("village")) {
                return true;
            } else if (12000 <= zoomLevel && place.equals("suburb")) {
                return true;
            } else if (12000 <= zoomLevel && place.equals("hamlet")) {
                return true;
            }
        }
        return false;
    }
}
