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

    private HashMap<ElementGroup, KdTree<Way>> kdTreeMap;
    private HashMap<ElementGroup, RTree<Integer, Way>> rTreeMap;

    private final HashMap<ElementGroup, List<Way>> searchMap = new HashMap<>();
    private final HashMap<ElementGroup, List<Way>> rTreeSearchMap = new HashMap<>();

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Way> islands;
    private final List<Relation> relations;
    private final ElementLongIndex<Way> wayLongIndex;

    private final float minX, minY, maxX, maxY;

    private final Options options = Options.getInstance();

    /**
     * MapData constructor.
     * Creates directed graph for path finding.
     * Builds kd-trees or r-trees if option is enabled.
     */
    public MapData(
            List<Way> islands,
            ElementLongIndex<Way> wayLongIndex,
            List<Relation> relations,
            HashMap<ElementGroup, KdTree<Way>> kdTreeMap,
            HashMap<ElementGroup, RTree<Integer, Way>> rTreeMap,
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

        if (kdTreeMap != null) {
            System.out.println("kd-tree map size: "+kdTreeMap.size());
            for (ElementGroup elementGroup : kdTreeMap.keySet()) {
                KdTree<Way> kdTree = kdTreeMap.get(elementGroup);
                System.out.println("Found kd-tree for "+elementGroup.toString()+" with depth: "+kdTree.getMaxDepth());
            }
        }

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
     * A tree is built for every ElementGroup as we only need certain trees at a given zoom level.
     * <p>
     * The trees are only built if no trees are given in the constructor of this class.
     * There is no need to build any trees if we loaded an .obj file.
     */
    public void buildTrees() {

        for (ElementGroup elementGroup : ElementGroup.values()) {
            searchMap.put(elementGroup, new ArrayList<>());
            rTreeSearchMap.put(elementGroup, new ArrayList<>());
        }

        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTreeMap == null) {
                kdTreeMap = new HashMap<>();

                System.out.println("Building kd-trees...");
                HashMap<ElementGroup, List<Way>> wayMap = getElementMap();

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    List<Way> wayList = wayMap.get(elementGroup);

                    if (wayList.size() > 0) {
                        KdTree<Way> kdTree = new KdTree<>();
                        this.kdTreeMap.put(elementGroup, kdTree);
                        kdTree.build(wayList);
                        System.out.println("Building kd-tree for "+elementGroup.toString()+" with depth: "+kdTree.getMaxDepth());
                    }
                }
            }
        } else if (options.getBool(Option.USE_R_TREE)) {
            if (rTreeMap == null) {
                rTreeMap = new HashMap<>();

                System.out.println("Building r-trees...");
                HashMap<ElementGroup, List<Way>> wayMap = getElementMap();

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    System.out.println("Building r-tree for "+elementGroup.toString());

                    RTree<Integer, Way> rTree = RTree.star().maxChildren(6).create();

                    for (Way way : wayMap.get(elementGroup)) {
                        rTree = rTree.add(0, way);
                    }
                    this.rTreeMap.put(elementGroup, rTree);
                }
            }
        }
    }

    /**
     * @return HashMap containing every ElementGroup and their list of Ways
     * The lists are built using the elements from the wayLongIndex
     */
    public HashMap<ElementGroup, List<Way>> getElementMap() {
        HashMap<ElementGroup, List<Way>> elementMap = new HashMap<>();

        for (ElementGroup elementGroup : ElementGroup.values()) {
            elementMap.put(elementGroup, new ArrayList<>());
        }
        for (Way way : wayLongIndex.getElements()) {

            ElementType type = way.getType();
            if (type != null) {

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    if (elementGroup.getType() == type) {

                        ElementSize size = way.getElementSize();
                        if (elementGroup.getSize() == size) {

                            List<Way> elementList = elementMap.get(elementGroup);
                            elementList.add(way);
                            elementMap.put(elementGroup, elementList);
                        }
                    }
                }
            }
        }
        return elementMap;
    }

    /**
     * Returns a list of Ways with the specific ElementGroup.
     * List is retrieved from search map filled by the kd-tree or r-tree range search.
     * If no tree is enabled, it builds a list from WayLongIndex.
     *
     * @param elementGroup specific ElementGroup.
     * @return list of Ways with the specific ElementGroup.
     */
    public List<Way> getWays(ElementGroup elementGroup) {
        if (elementGroup.getType() == ElementType.ISLAND) {
            return islands;

        } else if (options.getBool(Option.USE_KD_TREE)) {
            return searchMap.get(elementGroup);

        } else if (options.getBool(Option.USE_R_TREE)) {
            return rTreeSearchMap.get(elementGroup);
        }
        List<Way> wayList = new ArrayList<>();
        for (Way way : wayLongIndex.getElements()) {
            ElementType type = way.getType();
            if (type != null) {
                for (ElementType emType : ElementType.values()) {
                    if (type == emType) {
                        wayList.add(way);
                    }
                }
            }
        }
        return wayList;
    }

    /**
     * Starts a range search within the screens BoundingBox for all r-trees
     * if the specific ElementGroup is enabled at the given zoomLevel.
     */
    public void rTreeRangeSearch(double x1, double y1, double x2, double y2, double zoomLevel) {
        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.doShowElement(zoomLevel)) {

                Iterable<Entry<Integer, Way>> results =
                        rTreeMap.get(elementGroup).search(Geometries.rectangle(x1, y1, x2, y2));

                Iterator<Entry<Integer, Way>> rTreeIterator = results.iterator();
                List<Way> wayList = new ArrayList<>();

                while (rTreeIterator.hasNext()) {
                    Way way = rTreeIterator.next().geometry();
                    wayList.add(way);
                }
                rTreeSearchMap.put(elementGroup, wayList);
            }
        }
    }

    /**
     * Starts a range search within the screens BoundingBox for all kd-trees
     * if the specific ElementGroup is enabled at the given zoomLevel.
     */
    public void kdTreeRangeSearch(BoundingBox boundingBox, double zoomLevel) {
        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.doShowElement(zoomLevel)) {
                if (kdTreeMap.containsKey(elementGroup)) {
                    List<Way> wayList = kdTreeMap.get(elementGroup).preRangeSearch(boundingBox);
                    searchMap.put(elementGroup, wayList);
                }
            }
        }
    }

    public ElementLongIndex<Way> getWayLongIndex() {
        return wayLongIndex;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public HashMap<ElementGroup, KdTree<Way>> getKdTreeMap() {
        return kdTreeMap;
    }

    public HashMap<ElementGroup, RTree<Integer, Way>> getRTreeMap() {
        return rTreeMap;
    }

    public DirectedGraph getDirectedGraph() {
        return directedGraph;
    }

    public KdTree<Way> getKdTree(ElementGroup elementGroup) {
        return kdTreeMap.get(elementGroup);
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
