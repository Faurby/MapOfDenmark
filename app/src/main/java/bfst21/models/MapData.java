package bfst21.models;

import bfst21.osm.*;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MapData {

    private final List<Drawable> shapes;
    private final List<Way> islands;
    private final List<Way> ways;
    private final ElementLongIndex idToRelation;
    private KdTree kdTree;
    private RTree<Integer, Way> rTree;

    //TODO: Maybe this should be reconsidered. This list could end up containing all elements
    private List<Way> searchList;
    private List<Way> rTreeSearchList;

    private final float minx, miny, maxx, maxy;

    private final Options options = Options.getInstance();

    public MapData(
            List<Drawable> shapes,
            List<Way> islands,
            List<Way> ways,
            ElementLongIndex idToRelation,
            KdTree kdTree,
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

        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTree != null) {
                this.kdTree = kdTree;
            } else {
                this.kdTree = new KdTree();
                this.kdTree.build(ways);
            }
        } else if (options.getBool(Option.USE_R_TREE)) {
            this.rTree = RTree.star().maxChildren(6).create();

            for (Way way : ways) {
                rTree = rTree.add(0, way);
            }
        }
    }

    public KdTree getKdTree() {
        return kdTree;
    }

    private List<Way> getList() {
        if (options.getBool(Option.USE_KD_TREE)) {
            return searchList;

        } else if (options.getBool(Option.USE_R_TREE)) {
            return rTreeSearchList;
        }
        return ways;
    }

    public void search(double x1, double y1, double x2, double y2) {
        Iterable<Entry<Integer, Way>> results =
            rTree.search(Geometries.rectangle(x1, y1, x2, y2));

        Iterator<Entry<Integer, Way>> rTreeIterator = results.iterator();
        rTreeSearchList = new ArrayList<>();

        while (rTreeIterator.hasNext()) {
            Way way = rTreeIterator.next().geometry();
            rTreeSearchList.add(way);
        }
    }

    public void rangeSearch(BoundingBox boundingBox) {
        searchList = kdTree.preRangeSearch(boundingBox);
    }

    public List<Way> getWays(WayType wayType) {
        List<Way> list = new ArrayList<>();

        switch (wayType) {
            case ISLAND:
                return islands;
            case WATER:
                return getWater();
            case WATERWAY:
                return getWaterWays();
            case LANDUSE:
                return getLandUse();
            case BUILDING:
                return getBuildings();
            case MOTORWAY:
            case TERTIARY:
            case PRIMARY:
            case RESIDENTIAL:
                return getExtendedWays(wayType);
        }
        return list;
    }

    private List<Way> getExtendedWays(WayType wayType) {
        List<Way> list = new ArrayList<>();

        for (Way way : getList()) {
            if (way.getValue("highway") != null) {
                if (way.getValue("highway").contains(wayType.toString())) {
                    list.add(way);
                }
            }
        }
        return list;
    }

    private List<Way> getWater() {
        List<Way> list = new ArrayList<>();

        for (Way way : getList()) {
            if (way.getValue("natural") != null) {
                if (way.getValue("natural").contains("WATER")) {
                    list.add(way);
                }
            }
        }
        return list;
    }

    private List<Way> getBuildings() {
        List<Way> list = new ArrayList<>();

        for (Way way : getList()) {
            if (way.getValue("building") != null) {
                list.add(way);
            }
        }
        return list;
    }

    private List<Way> getWaterWays() {
        List<Way> list = new ArrayList<>();

        for (Way way : getList()) {
            if (way.getValue("waterway") != null) {
                list.add(way);
            }
        }
        return list;
    }

    private List<Way> getLandUse() {
        List<Way> list = new ArrayList<>();

        for (Way way : getList()) {
            if (way.getValue("landuse") != null) {
                if (way.getValue("landuse").equals("GRASS") ||
                    way.getValue("landuse").equals("MEADOW") ||
                    way.getValue("landuse").equals("ORCHARD") ||
                    way.getValue("landuse").equals("ALLOTMENTS")) {

                    list.add(way);
                }
            } else if (way.getValue("leisure") != null) {
                if (way.getValue("leisure").equals("PARK")) {
                    list.add(way);
                }
            }
        }
        return list;
    }

    public List<Drawable> getShapes() {
        return shapes;
    }

    public List<Way> getWays() {
        return ways;
    }

    public ElementLongIndex getIdToRelation() {
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
