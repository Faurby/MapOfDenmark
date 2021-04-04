package bfst21.vector;

import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import bfst21.vector.osm.Way;
import java.util.ArrayList;
import java.util.List;


public class MapData {

    private final List<Drawable> shapes;

    private final List<Way> islands;
    private final LongIndex idToRelation;
    private final KdTree kdTree;
    private final List<Way> ways;

    private List<Way> searchList;

    private final float minx, miny, maxx, maxy;

    public MapData(
            List<Drawable> shapes,
            List<Way> islands,
            List<Way> ways,
            LongIndex idToRelation,
            float minx,
            float maxx,
            float miny,
            float maxy) {

        this.shapes = shapes;
        this.islands = islands;
        this.ways = ways;
        this.idToRelation = idToRelation;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;

        kdTree = new KdTree();
        kdTree.build(ways);
    }

    public KdTree getKdTree() {
        return kdTree;
    }

    public void rangeSearch(BoundingBox boundingBox) {
        searchList = kdTree.preRangeSearch(boundingBox);
    }

    public List<Way> getExtendedWays(String type) {
        List<Way> list = new ArrayList<>();

        for (Way way : searchList) {
            if (way.getValue("highway") != null) {
                if (way.getValue("highway").contains(type)) {
                    list.add(way);
                }
            }
        }
        return list;
    }

    public List<Way> getWater() {
        List<Way> list = new ArrayList<>();

        for (Way way : searchList) {
            if (way.getValue("natural") != null) {
                if (way.getValue("natural").contains("water")) {
                    list.add(way);
                }
            }
        }
        return list;
    }

    public List<Way> getBuildings() {
        List<Way> list = new ArrayList<>();

        for (Way way : searchList) {
            if (way.getValue("building") != null) {
                list.add(way);
            }
        }
        return list;
    }

    public List<Way> getWaterWays() {
        List<Way> list = new ArrayList<>();

        for (Way way : searchList) {
            if (way.getValue("waterway") != null) {
                list.add(way);
            }
        }
        return list;
    }

    public List<Way> getLandUse() {
        List<Way> list = new ArrayList<>();

        for (Way way : searchList) {
            if (way.getValue("landuse") != null) {
                if (way.getValue("landuse").equalsIgnoreCase("grass") ||
                        way.getValue("landuse").equalsIgnoreCase("meadow") ||
                        way.getValue("landuse").equalsIgnoreCase("orchard") ||
                        way.getValue("landuse").equalsIgnoreCase("allotments")) {

                    list.add(way);
                }
            } else if (way.getValue("leisure") != null) {
                if (way.getValue("leisure").equalsIgnoreCase("park")) {

                    list.add(way);
                }
            }
        }
        return list;
    }

    public List<Drawable> getShapes() {
        return shapes;
    }

    public List<Way> getIslands() {
        return islands;
    }

    public List<Way> getWays() {
        return ways;
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
