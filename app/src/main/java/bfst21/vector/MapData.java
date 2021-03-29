package bfst21.vector;

import bfst21.vector.osm.Way;
import java.util.ArrayList;
import java.util.List;


public class MapData {

    private final List<Drawable> shapes;

    private final List<Way> buildings;
    private final List<Way> islands;
    private final List<Way> extendedWays;
    private final LongIndex idToRelation;

    private final float minx, miny, maxx, maxy;

    public MapData(
            List<Drawable> shapes,
            List<Way> buildings,
            List<Way> islands,
            List<Way> extendedWays,
            LongIndex idToRelation,
            float minx,
            float maxx,
            float miny,
            float maxy) {

        this.shapes = shapes;
        this.buildings = buildings;
        this.islands = islands;
        this.extendedWays = extendedWays;
        this.idToRelation = idToRelation;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public List<Way> getExtendedWays(String type) {
        List<Way> ways = new ArrayList<>();

        for (Way way : extendedWays) {
            if (way.getValue("highway") != null) {
                if (way.getValue("highway").contains(type)) {
                    ways.add(way);
                }
            }
        }
        return ways;
    }

    public List<Way> getWater() {
        List<Way> water = new ArrayList<>();

        for (Way way : extendedWays) {
            if (way.getValue("natural") != null) {
                if (way.getValue("natural").contains("water")) {
                    water.add(way);
                }
            }
        }
        return water;
    }

    public List<Way> getWaterWays() {
        List<Way> waterWays = new ArrayList<>();

        for (Way way : extendedWays) {
            if (way.getValue("waterway") != null) {
                waterWays.add(way);
            }
        }
        return waterWays;
    }

    public List<Way> getLandUse() {
        List<Way> landUse = new ArrayList<>();

        for (Way way : extendedWays) {
            if (way.getValue("landuse") != null) {
                if (way.getValue("landuse").equalsIgnoreCase("grass") ||
                        way.getValue("landuse").equalsIgnoreCase("meadow") ||
                        way.getValue("landuse").equalsIgnoreCase("orchard") ||
                        way.getValue("landuse").equalsIgnoreCase("allotments")) {

                    landUse.add(way);
                }
            } else if (way.getValue("leisure") != null) {
                if (way.getValue("leisure").equalsIgnoreCase("park")) {

                    landUse.add(way);
                }
            }
        }
        return landUse;
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

    public LongIndex getIdToRelation() {
        return idToRelation;
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
