package bfst21.models;

import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Vertex;
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
    private final DirectedGraph directedGraph;
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

        directedGraph = new DirectedGraph(ways.size());

        int idCount = 0;
        for (Way way : ways) {
            if (way.getType() != null) {
                if (way.canNavigate()) {
                    double maxSpeed = way.getMaxSpeed();

                    int size = way.getNodes().size();
                    for (int i = 0; i < (size -1); i++) {
                        Node first = way.getNodes().get(i);
                        Node last = way.getNodes().get(i + 1);

                        Vertex from = directedGraph.getVertex(first.getX(), first.getY(), idCount);
                        Vertex to = directedGraph.getVertex(last.getX(), last.getY(), idCount);

                        directedGraph.addEdge(from, to, maxSpeed);
                        idCount++;
                    }
                }
            }
        }

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

    public DirectedGraph getDirectedGraph() {
        return directedGraph;
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

    public List<Way> getWays(ElementType elementType) {
        List<Way> list = new ArrayList<>();

        if (elementType == ElementType.ISLAND) {
            return islands;
        }
        for (Way way : getList()) {
            if (way.getType() == elementType) {
                list.add(way);
            }
        }
        return list;
    }

    public List<Way> getFillWays(ElementType elementType, double zoomLevel) {
        if (elementType != ElementType.ISLAND) {
            List<Way> list = new ArrayList<>();

            for (Way way : getList()) {
                if (way.getType() == elementType) {
                    float area = way.getArea();
                    if (area >= 500_000 || zoomLevel >= 9000) {
                        list.add(way);
                    } else if (area >= 100_000 && zoomLevel >= 2000) {
                        list.add(way);
                    }  else if (area >= 70_000 && zoomLevel >= 5000) {
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
