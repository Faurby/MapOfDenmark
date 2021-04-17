package bfst21.models;

import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Vertex;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;

import java.util.*;


public class MapData {

    private HashMap<ElementType, KdTree> kdTreeMap = new HashMap<>();
    private HashMap<ElementType, RTree<Integer, Way>> rTreeMap = new HashMap<>();

    private final HashMap<ElementType, List<Way>> searchMap = new HashMap<>();
    private final HashMap<ElementType, List<Way>> rTreeSearchMap = new HashMap<>();

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Way> islands;
    private final List<Relation> relations;
    private final DirectedGraph directedGraph;
    private final WayLongIndex wayLongIndex;

    private final float minx, miny, maxx, maxy;

    private final Options options = Options.getInstance();

    /**
     * MapData constructor
     * Creates directed graph for path finding
     * Builds kd-trees or r-trees if option is enabled
     */
    public MapData(
            List<Way> islands,
            WayLongIndex wayLongIndex,
            List<Relation> relations,
            HashMap<ElementType, KdTree> kdTreeMap,
            HashMap<ElementType, RTree<Integer, Way>> rTreeMap,
            float minx,
            float maxx,
            float miny,
            float maxy) {

        this.islands = islands;
        this.relations = relations;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.wayLongIndex = wayLongIndex;

        directedGraph = new DirectedGraph(wayLongIndex.getElements().size());

        //Initialize ArrayLists for the search maps
        for (ElementType elementType : ElementType.values()) {
            searchMap.put(elementType, new ArrayList<>());
            rTreeSearchMap.put(elementType, new ArrayList<>());
        }

        //Create directed graph for path finding
        int idCount = 0;
        for (Way way : wayLongIndex.getElements()) {
            if (way.getType() != null) {
                if (way.canNavigate()) {
                    double maxSpeed = way.getMaxSpeed();

                    int size = way.getNodes().size();
                    for (int i = 0; i < (size - 1); i++) {
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

        //Build kd-trees or r-trees
        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTreeMap != null) {
                this.kdTreeMap = kdTreeMap;
            } else {
                System.out.println("Building kd-trees...");
                HashMap<ElementType, List<Way>> wayMap = wayLongIndex.getElementMap();
                for (ElementType elementType : ElementType.values()) {
                    if (elementType != ElementType.ISLAND) {

                        List<Way> wayList = wayMap.get(elementType);
                        if (wayList.size() > 0) {
                            KdTree kdTree = new KdTree();
                            this.kdTreeMap.put(elementType, kdTree);
                            kdTree.build(wayList);
                        }
                    }
                }
            }
        } else if (options.getBool(Option.USE_R_TREE)) {
            if (rTreeMap != null) {
                this.rTreeMap = rTreeMap;
            } else {
                System.out.println("Building r-trees...");
                HashMap<ElementType, List<Way>> wayList = wayLongIndex.getElementMap();
                for (ElementType elementType : ElementType.values()) {
                    RTree<Integer, Way> rTree = RTree.star().maxChildren(6).create();

                    for (Way way : wayList.get(elementType)) {
                        rTree = rTree.add(0, way);
                    }
                    this.rTreeMap.put(elementType, rTree);
                }
            }
        }
    }

    /**
     * Returns a list of Ways with the specific ElementType
     * List is retrieved from search map filled by the kd-tree or r-tree range search
     * If no tree is enabled, it builds a list from WayLongIndex
     *
     * @param elementType specific ElementType
     * @return list of Ways with the specific ElementType
     */
    public List<Way> getWays(ElementType elementType) {
        if (elementType == ElementType.ISLAND) {
            return islands;

        } else if (options.getBool(Option.USE_KD_TREE)) {
            return searchMap.get(elementType);

        } else if (options.getBool(Option.USE_R_TREE)) {
            return rTreeSearchMap.get(elementType);
        }
        List<Way> list = new ArrayList<>();
        for (Way way : wayLongIndex.getElements()) {
            ElementType type = way.getType();
            if (type != null) {
                for (ElementType emType : ElementType.values()) {
                    if (type == emType) {
                        list.add(way);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Starts a range search within the screens BoundingBox for all r-trees
     * if the specific ElementType is enabled at the given zoomLevel
     */
    public void rTreeRangeSearch(double x1, double y1, double x2, double y2, double zoomLevel) {
        for (ElementType elementType : ElementType.values()) {
            if (zoomLevel >= elementType.getZoomLevelRequired()) {

                Iterable<Entry<Integer, Way>> results =
                        rTreeMap.get(elementType).search(Geometries.rectangle(x1, y1, x2, y2));

                Iterator<Entry<Integer, Way>> rTreeIterator = results.iterator();
                List<Way> rTreeSearchList = new ArrayList<>();

                while (rTreeIterator.hasNext()) {
                    Way way = rTreeIterator.next().geometry();
                    rTreeSearchList.add(way);
                }
                rTreeSearchMap.put(elementType, rTreeSearchList);
            }
        }
    }

    /**
     * Starts a range search within the screens BoundingBox for all kd-trees
     * if the specific ElementType is enabled at the given zoomLevel
     */
    public void kdTreeRangeSearch(BoundingBox boundingBox, double zoomLevel) {
        for (ElementType elementType : ElementType.values()) {
            if (zoomLevel >= elementType.getZoomLevelRequired()) {
                if (kdTreeMap.containsKey(elementType)) {
                    searchMap.put(elementType, kdTreeMap.get(elementType).preRangeSearch(boundingBox));
                }
            }
        }
    }

//    public List<Way> getFillWays(ElementType elementType, double zoomLevel) {
//        if (elementType != ElementType.ISLAND) {
//            List<Way> list = new ArrayList<>();
//
//            for (Way way : getList()) {
//                if (way.getType() == elementType) {
//                    float area = way.getArea();
//                    if (area >= 500_000 || zoomLevel >= 9000) {
//                        list.add(way);
//                    } else if (area >= 100_000 && zoomLevel >= 2000) {
//                        list.add(way);
//                    }  else if (area >= 70_000 && zoomLevel >= 5000) {
//                        list.add(way);
//                    }
//                }
//            }
//            return list;
//        } else {
//            return islands;
//        }
//    }


    public WayLongIndex getWayLongIndex() {
        return wayLongIndex;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public HashMap<ElementType, KdTree> getKdTreeMap() {
        return kdTreeMap;
    }

    public HashMap<ElementType, List<Way>> getrTreeSearchMap() {
        return rTreeSearchMap;
    }

    public DirectedGraph getDirectedGraph() {
        return directedGraph;
    }

    public KdTree getKdTree(ElementType elementType) {
        return kdTreeMap.get(elementType);
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
