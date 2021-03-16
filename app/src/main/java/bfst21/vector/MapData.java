package bfst21.vector;

import java.util.List;


public class MapData {

    private List<Drawable> shapes;
    private List<Drawable> buildings;
    private List<Drawable> islands;
    private List<Drawable> extendedWays;

    private float minx, miny, maxx, maxy;

    public MapData(List<Drawable> shapes, List<Drawable> buildings, List<Drawable> islands, List<Drawable> extendedWays, float minx, float maxx, float miny, float maxy) {
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

    public List<Drawable> getBuildings() {
        return buildings;
    }

    public List<Drawable> getIslands() {
        return islands;
    }

    public List<Drawable> getExtendedWays() {
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
