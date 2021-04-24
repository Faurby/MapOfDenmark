package bfst21.models;

import bfst21.address.TriesMap;
import bfst21.osm.*;
import bfst21.pathfinding.Dijkstra;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;

import java.util.*;


public class MapData {

    private DirectedGraph directedGraph;
    private Dijkstra dijkstra;

    private HashMap<ElementGroup, KdTree<Way>> kdTreeMap;
    private final HashMap<ElementGroup, List<Way>> kdTreeSearchMap = new HashMap<>();

    private KdTree<Relation> kdTreeRelations;
    private List<Relation> kdTreeRelationSearchList = new ArrayList<>();

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Way> islands;

    private final float minX, minY, maxX, maxY;

    private final TriesMap triesMap;

    private final DisplayOptions displayOptions = DisplayOptions.getInstance();

    public Node originNode;
    public Node destinationNode;

    /**
     * MapData constructor.
     * Creates directed graph for path finding.
     * Builds kd-trees for Ways and Relations.
     */
    public MapData(
            List<Way> islands,
            ElementLongIndex<Way> wayLongIndex,
            ElementLongIndex<Relation> relationLongIndex,
            HashMap<ElementGroup, KdTree<Way>> kdTreeMap,
            KdTree<Relation> kdTreeRelations,
            DirectedGraph directedGraph,
            TriesMap triesMap,
            float minX,
            float maxX,
            float minY,
            float maxY) {

        this.directedGraph = directedGraph;
        this.kdTreeMap = kdTreeMap;
        this.kdTreeRelations = kdTreeRelations;
        this.islands = islands;
        this.triesMap = triesMap;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        //We need to initially fill the search HashMap with empty Lists.
        //This is to avoid issues when accessing the search map before a range search.
        for (ElementGroup elementGroup : ElementGroup.values()) {
            kdTreeSearchMap.put(elementGroup, new ArrayList<>());
        }

        if (wayLongIndex != null && relationLongIndex != null) {
            List<Relation> relationList = new ArrayList<>();

            //Use relation to set Way type if no type is present.
            //Otherwise add it to relationList.
            for (Relation relation : relationLongIndex.getElements()) {
                if (!relation.isMultipolygon()) {
                    if (relation.getType() != null) {
                        for (Way way : relation.getWays()) {
                            if (way.getType() == null) {
                                way.setType(relation.getType());
                            }
                        }
                    }
                } else {
                    relation.mergeOuterWays();
                    relationList.add(relation);
                }
            }

            //Build the data structures if none is present.
            if (directedGraph == null) {
                buildDirectedGraph(wayLongIndex.getElements());
            }
            if (kdTreeMap == null) {
                buildSearchTreesForWays(wayLongIndex.getElements());
            }
            if (kdTreeRelations == null) {
                buildSearchTreesForRelations(relationList);
            }
        }
    }

    /**
     * Builds a directed graph used for path finding if option is enabled.
     */
    public void buildDirectedGraph(List<Way> wayList) {
        if (displayOptions.getBool(DisplayOption.USE_GRAPH)) {

            long time = -System.nanoTime();
            directedGraph = new DirectedGraph();

            int idCount = 0;
            for (Way way : wayList) {
                if (way.getType() != null) {
                    if (way.getType().canNavigate()) {

                        int size = way.getNodes().size();
                        for (int i = 0; i < (size - 1); i++) {
                            Node v = way.getNodes().get(i);
                            Node w = way.getNodes().get(i + 1);

                            int vID = idCount;
                            int wID = idCount + 1;

                            directedGraph.createVertex(v.getX(), v.getY(), vID);
                            directedGraph.createVertex(w.getX(), w.getY(), wID);
                            idCount += 2;
                        }
                    }
                }
            }
            idCount = 0;
            for (Way way : wayList) {
                if (way.getType() != null) {
                    if (way.getType().canNavigate()) {

                        int maxSpeed = way.getMaxSpeed();

                        int size = way.getNodes().size();
                        for (int i = 0; i < (size - 1); i++) {
                            Node v = way.getNodes().get(i);
                            Node w = way.getNodes().get(i + 1);

                            directedGraph.addEdge(v, w, maxSpeed, way.isOneWay());
                            idCount += 2;
                        }
                    }
                }
            }
            time += System.nanoTime();
            System.out.println("Built directed graph for path finding in " + time / 1_000_000 + "ms");
        }
    }

    /**
     * Run dijkstra path finding if nodes for origin and destination are present.
     */
    public void runDijkstra() {
        if (originNode != null && destinationNode != null) {
            dijkstra = new Dijkstra(directedGraph, originNode, destinationNode);
        }
    }

    /**
     * Builds a kd-tree for Relations
     * <p>
     * The tree is only built if no tree is given in the constructor of this class.
     * There is no need to build any tree if we loaded an .obj file.
     */
    public void buildSearchTreesForRelations(List<Relation> relationList) {
        if (kdTreeRelations == null) {
            long time = -System.nanoTime();

            kdTreeRelations = new KdTree<>();
            kdTreeRelations.build(relationList);

            time += System.nanoTime();
            System.out.println("Built kd-tree for relations in " + time / 1_000_000 + "ms with depth: " + kdTreeRelations.getMaxDepth());
        }
    }

    /**
     * Builds a HashMap of kd-trees for Ways.
     * <p>
     * A tree is built for every ElementGroup as we only need certain trees at a given zoom level.
     * <p>
     * The trees are only built if no trees are given in the constructor of this class.
     * There is no need to build any trees if we loaded an .obj file.
     */
    public void buildSearchTreesForWays(List<Way> wayList) {

        if (kdTreeMap == null) {
            kdTreeMap = new HashMap<>();

            HashMap<ElementGroup, List<Way>> wayMap = getElementMap(wayList);

            for (ElementGroup elementGroup : ElementGroup.values()) {
                List<Way> innerWayList = wayMap.get(elementGroup);

                if (innerWayList.size() > 0) {
                    long time = -System.nanoTime();

                    KdTree<Way> kdTree = new KdTree<>();
                    this.kdTreeMap.put(elementGroup, kdTree);
                    kdTree.build(innerWayList);

                    time += System.nanoTime();
                    System.out.println("Built kd-tree for " + elementGroup.toString() + " in " + time / 1_000_000 + "ms with depth: " + kdTree.getMaxDepth());
                }
            }
        }
    }

    /**
     * @return HashMap containing every ElementGroup and their list of Ways
     * The lists are built using the elements from the wayLongIndex
     */
    private HashMap<ElementGroup, List<Way>> getElementMap(List<Way> wayList) {
        HashMap<ElementGroup, List<Way>> elementMap = new HashMap<>();

        for (ElementGroup elementGroup : ElementGroup.values()) {
            elementMap.put(elementGroup, new ArrayList<>());
        }
        for (Way way : wayList) {

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
     * @return list of Relations found by the range search with specific ElementType.
     */
    public List<Relation> getRelations(ElementType elementType) {
        List<Relation> relationList = new ArrayList<>();

        for (Relation relation : getRelations()) {
            if (relation.getType() == elementType) {
                relationList.add(relation);
            }
        }
        return relationList;
    }

    /**
     * @return list of Relations found by the kd-tree range search.
     */
    public List<Relation> getRelations() {
        return kdTreeRelationSearchList;
    }

    /**
     * Returns a list of Ways with the specific ElementGroup.
     * List is retrieved from search map filled by the kd-tree range search.
     *
     * @param elementGroup specific ElementGroup.
     * @return list of Ways with the specific ElementGroup.
     */
    public List<Way> getWays(ElementGroup elementGroup) {
        if (elementGroup.getType() == ElementType.ISLAND) {
            return islands;
        }
        return kdTreeSearchMap.get(elementGroup);
    }

    /**
     * Starts a range search within the screens BoundingBox for all kd-trees
     * if the specific ElementGroup is enabled at the given zoomLevel.
     */
    public void kdTreeRangeSearch(BoundingBox boundingBox, double zoomLevel) {
        //Search way kd-trees
        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.doShowElement(zoomLevel)) {
                if (kdTreeMap.containsKey(elementGroup)) {
                    List<Way> wayList = kdTreeMap.get(elementGroup).preRangeSearch(boundingBox);
                    kdTreeSearchMap.put(elementGroup, wayList);
                }
            }
        }
        //Search relation kd-tree
        kdTreeRelationSearchList = kdTreeRelations.preRangeSearch(boundingBox);
    }

    /**
     * Starts a nearest neighbor search for the kd-tree with the given query node.
     * Will only search in kd-trees with an ElementGroup where navigation is possible.
     * <p>
     * When a list of nearby nodes are found, we will then find the closest node.
     */
    public Node kdTreeNearestNeighborSearch(Node queryNode) {
        List<Node> list = new ArrayList<>();

        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.getType().canNavigate()) {
                if (kdTreeMap.containsKey(elementGroup)) {
                    Node node = kdTreeMap.get(elementGroup).nearestNeighborSearch(queryNode);
                    list.add(node);
                }
            }
        }
        Node nearest = null;
        double minimumDistance = Double.MAX_VALUE;

        for (Node node : list) {
            double distance = queryNode.distTo(node);

            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearest = node;
            }
        }
        return nearest;
    }

    public HashMap<ElementGroup, KdTree<Way>> getKdTreeMap() {
        return kdTreeMap;
    }

    public KdTree<Relation> getKdTreeRelations() {
        return kdTreeRelations;
    }

    public KdTree<Way> getKdTree(ElementGroup elementGroup) {
        return kdTreeMap.get(elementGroup);
    }

    public DirectedGraph getDirectedGraph() {
        return directedGraph;
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

    public Dijkstra getDijkstra() {
        return dijkstra;
    }

    public TriesMap getTriesMap() {
        return triesMap;
    }
}
