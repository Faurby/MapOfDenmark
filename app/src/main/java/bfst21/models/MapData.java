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

    private DirectedGraph directedGraph;

    private HashMap<ElementType, KdTree> kdTreeMap;
    private HashMap<ElementType, RTree<Integer, Way>> rTreeMap;

    private final HashMap<ElementType, List<Way>> searchMap = new HashMap<>();
    private final HashMap<ElementType, List<Way>> rTreeSearchMap = new HashMap<>();

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Way> islands;
    private final List<Relation> relations;
    private final WayLongIndex wayLongIndex;

    private final float minX, minY, maxX, maxY;

    private final Options options = Options.getInstance();

    /**
     * MapData constructor.
     * Creates directed graph for path finding.
     * Builds kd-trees or r-trees if option is enabled.
     */
    public MapData(
            List<Way> islands,
            WayLongIndex wayLongIndex,
            List<Relation> relations,
            HashMap<ElementType, KdTree> kdTreeMap,
            HashMap<ElementType, RTree<Integer, Way>> rTreeMap,
            float minX,
            float maxX,
            float minY,
            float maxY) {

        this.wayLongIndex = wayLongIndex;
        this.kdTreeMap = kdTreeMap;
        this.rTreeMap = rTreeMap;
        this.islands = islands;
        this.relations = relations;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        buildDirectedGraph();
        buildTrees();
    }

    /**
     * Builds a directed graph used for path finding.
     */
    public void buildDirectedGraph() {
        directedGraph = new DirectedGraph(wayLongIndex.getElements().size());
        System.out.println("Building directed graph for path finding...");

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
    }

    /**
     * Builds a HashMap of kd-trees or r-trees if the option is enabled.
     * <p>
     * A tree is built for every ElementType as we only need certain trees at a given zoom level.
     * <p>
     * The trees are only built if no trees are given in the constructor of this class.
     * There is no need to build any trees if we loaded an .obj file.
     */
    public void buildTrees() {

        for (ElementType elementType : ElementType.values()) {
            searchMap.put(elementType, new ArrayList<>());
            rTreeSearchMap.put(elementType, new ArrayList<>());
        }

        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTreeMap == null) {
                kdTreeMap = new HashMap<>();

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
            if (rTreeMap == null) {
                rTreeMap = new HashMap<>();

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
     * Returns a list of Ways with the specific ElementType.
     * List is retrieved from search map filled by the kd-tree or r-tree range search.
     * If no tree is enabled, it builds a list from WayLongIndex.
     *
     * @param elementType specific ElementType.
     * @return list of Ways with the specific ElementType.
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
     * if the specific ElementType is enabled at the given zoomLevel.
     */
    public void rTreeRangeSearch(double x1, double y1, double x2, double y2, double zoomLevel) {
        for (ElementType elementType : ElementType.values()) {
            if (zoomLevel >= elementType.getZoomLevelRequired()) {

                Iterable<Entry<Integer, Way>> results =
                        rTreeMap.get(elementType).search(Geometries.rectangle(x1, y1, x2, y2));

                Iterator<Entry<Integer, Way>> rTreeIterator = results.iterator();
                List<Way> wayList = new ArrayList<>();

                while (rTreeIterator.hasNext()) {
                    Way way = rTreeIterator.next().geometry();
                    wayList.add(way);
                }
                rTreeSearchMap.put(elementType, wayList);
            }
        }
    }

    /**
     * Starts a range search within the screens BoundingBox for all kd-trees
     * if the specific ElementType is enabled at the given zoomLevel.
     */
    public void kdTreeRangeSearch(BoundingBox boundingBox, double zoomLevel) {
        for (ElementType elementType : ElementType.values()) {
            if (zoomLevel >= elementType.getZoomLevelRequired()) {
                if (kdTreeMap.containsKey(elementType)) {
                    List<Way> wayList = kdTreeMap.get(elementType).preRangeSearch(boundingBox);
                    searchMap.put(elementType, wayList);
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

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public List<UserNode> getUserNodes() {
        return userNodes;
    }

    public void addUserNode(UserNode userNode) {
        userNodes.add(userNode);
    }
}
