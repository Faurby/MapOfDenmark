package bfst21.models;

import bfst21.osm.*;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;

import java.util.*;


public class MapData {

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Drawable> shapes;
    private final List<Way> islands;
    private final List<Way> ways;
    private final List<Relation> relations;
    private KdTree kdTree;
    private RTree<Integer, Way> rTree;

    private List<Way> searchList;
    private List<Way> rTreeSearchList;

    private final float minx, miny, maxx, maxy;

    private final Options options = Options.getInstance();

    public MapData(
            List<Drawable> shapes,
            List<Way> islands,
            List<Way> ways,
            List<Relation> relations,
            KdTree kdTree,
            float minx,
            float maxx,
            float miny,
            float maxy) {

        this.shapes = shapes;
        this.islands = islands;
        this.ways = ways;
        this.relations = relations;
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

        if (wayType == WayType.ISLAND) {
            return islands;
        }
        for (Way way : getList()) {
            if (way.getType() == wayType) {
                list.add(way);
            }
        }
        return list;
    }

    public List<Way> getFillWays(WayType wayType, double zoomLevel) {
        if (wayType != WayType.ISLAND) {
            List<Way> list = new ArrayList<>();

            for (Way way : getList()) {
                if (way.getType() == wayType) {
                    if (way.getArea() >= 500_000 || zoomLevel >= 9000) {
                        list.add(way);
                    } else if (way.getArea() >= 100_000 && zoomLevel >= 2000) {
                        list.add(way);
                    }  else if (way.getArea() >= 70_000 && zoomLevel >= 5000) {
                        list.add(way);
                    }
                }
            }
            return list;
        } else {
            return islands;
        }
    }

    public List<Drawable> getShapes() {
        return shapes;
    }

    public List<Way> getWays() {
        return ways;
    }

    public List<Relation> getRelations() {
        return relations;
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

    public List<UserNode> getUserNodes() {
        return userNodes;
    }

    public void addUserNode(UserNode userNode) {
        userNodes.add(userNode);
    }
}
