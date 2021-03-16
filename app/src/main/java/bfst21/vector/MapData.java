package bfst21.vector;

import bfst21.vector.osm.Way;
import java.util.List;


public class MapData {

    private List<Drawable> shapes;

    private List<Way> buildings;
    private List<Way> islands;
    private List<Way> extendedWays;

    private float minx, miny, maxx, maxy;

    public MapData(List<Drawable> shapes, List<Way> buildings, List<Way> islands, List<Way> extendedWays, float minx, float maxx, float miny, float maxy) {
        this.shapes = shapes;
        this.buildings = buildings;
        this.islands = islands;
        this.extendedWays = extendedWays;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public List<Drawable> getShapes() {
        return shapes;
    }

    public List<Way> getBuildings() {
        return buildings;
    }

    public List<Way> getIslands() {
        return islands;
    }

    public List<Way> getExtendedWays() {
        return extendedWays;
    }

    public float getMinx() {
        return minx;
    }

    public float getMiny() {
        return miny;
    }

    public float getMaxx() {
        return maxx;
    }

    public float getMaxy() {
        return maxy;
    }
}
