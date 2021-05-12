package bfst21.models;

import bfst21.address.OsmAddress;
import bfst21.address.TST;
import bfst21.osm.*;
import bfst21.pathfinding.DijkstraPath;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;

import java.util.*;


/**
 * MapData contains all the relevant data structures and information used to draw the map.
 */
public class MapData {

    private DirectedGraph directedGraph;
    private DijkstraPath dijkstraPath;

    private HashMap<ElementGroup, KdTree<Way>> kdTreeMap;
    private final HashMap<ElementGroup, List<Way>> kdTreeSearchMap = new HashMap<>();

    private KdTree<Relation> kdTreeRelations;
    private List<Relation> kdTreeRelationSearchList = new ArrayList<>();

    private KdTree<MapText> kdTreeMapTexts;
    private List<MapText> kdTreeMapTextSearchList = new ArrayList<>();

    private List<UserNode> userNodes;
    private final HashMap<String, UserNode> userNodesMap;

    private final List<Way> islands;

    private final float minX, minY, maxX, maxY;

    private final TST<List<OsmAddress>> addressTries;

    /**
     * MapData constructor.
     * Creates directed graph for path finding.
     * Builds kd-trees for Ways and Relations.
     */
    public MapData(
            List<Way> islands,
            List<Way> wayList,
            List<Relation> relationList,
            List<MapText> mapTexts,
            TST<List<OsmAddress>> addressTries,
            HashMap<ElementGroup, KdTree<Way>> kdTreeMap,
            KdTree<Relation> kdTreeRelations,
            KdTree<MapText> kdTreeMapTexts,
            DirectedGraph directedGraph,
            List<UserNode> userNodes,
            float minX,
            float maxX,
            float minY,
            float maxY) {

        this.directedGraph = directedGraph;
        this.kdTreeMap = kdTreeMap;
        this.kdTreeRelations = kdTreeRelations;
        this.kdTreeMapTexts = kdTreeMapTexts;
        this.islands = islands;
        this.addressTries = addressTries;
        this.userNodes = userNodes;
        this.userNodesMap = new HashMap<>();
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        updateUserNodesMap();

        //We need to initially fill the search HashMap with empty Lists.
        //This is to avoid issues when accessing the search map before a range search.
        for (ElementGroup elementGroup : ElementGroup.values()) {
            kdTreeSearchMap.put(elementGroup, new ArrayList<>());
        }

        if (wayList != null && relationList != null) {
            List<Relation> finalRelationList = new ArrayList<>();

            //Use relation to set Way type if no type is present.
            //Otherwise add it to relationList.
            for (Relation relation : relationList) {
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
                    finalRelationList.add(relation);
                }
            }

            //Build the data structures if none is present.
            if (directedGraph == null) {
                buildDirectedGraph(wayList);
            }
            if (kdTreeMap == null) {
                buildKdTreeForWays(wayList);
            }
            if (kdTreeRelations == null) {
                buildKdTreeForRelations(finalRelationList);
            }
            if (kdTreeMapTexts == null) {
                buildKdTreeForMapText(mapTexts);
            }
            if (userNodes == null) {
                this.userNodes = new ArrayList<>();
            }
        }
    }

    /**
     * Builds a directed graph used for path finding.
     */
    private void buildDirectedGraph(List<Way> wayList) {

        long time = -System.nanoTime();
        directedGraph = new DirectedGraph();

        for (Way way : wayList) {
            if (way.getType() != null) {
                if (way.getType().canNavigate(TransportOption.ALL)) {

                    boolean junction = way.isJunction();

                    ElementType type = way.getType();
                    boolean canDrive = type.canNavigate(TransportOption.CAR);
                    boolean canBike = type.canNavigate(TransportOption.BIKE);
                    boolean canWalk = type.canNavigate(TransportOption.WALK);
                    boolean oneWay = way.isOneWay();
                    boolean oneWayBike = way.isOneWayBike();
                    int maxSpeed = way.getMaxSpeed();
                    String name = way.getName();

                    float[] coords = way.getCoords();

                    for (int i = 0; i < (coords.length - 2); i += 2) {
                        float vX = coords[i];
                        float vY = coords[i + 1];
                        float wX = coords[i + 2];
                        float wY = coords[i + 3];

                        float[] fromCoords = new float[]{vX, vY};
                        float[] toCoords = new float[]{wX, wY};

                        directedGraph.createVertex(fromCoords);
                        directedGraph.createVertex(toCoords);

                        directedGraph.addEdge(name,
                                fromCoords,
                                toCoords,
                                maxSpeed,
                                junction,
                                oneWay,
                                oneWayBike,
                                canDrive,
                                canBike,
                                canWalk);
                    }
                }
            }
        }
        directedGraph.cleanUp();

        time += System.nanoTime();
        System.out.println("Built directed graph for path finding in " + time / 1_000_000L + "ms");
    }

    /**
     * Run dijkstra path finding if coords for origin and destination are present.
     */
    public void runDijkstra(float[] originCoords, float[] destinationCoords) {
        dijkstraPath = new DijkstraPath(directedGraph, originCoords, destinationCoords);
    }

    /**
     * Builds a kd-tree for MapText
     * <p>
     * The tree is only built if no tree is given in the constructor of this class.
     * There is no need to build any tree if we loaded an .obj file.
     */
    private void buildKdTreeForMapText(List<MapText> mapTexts) {
        if (kdTreeMapTexts == null) {
            long time = -System.nanoTime();

            kdTreeMapTexts = new KdTree<>();
            kdTreeMapTexts.build(mapTexts);

            time += System.nanoTime();
            System.out.println("Built kd-tree for map texts in " + time / 1_000_000L + "ms with depth: " + kdTreeMapTexts.getMaxDepth());
        }
    }

    /**
     * Builds a kd-tree for Relations
     * <p>
     * The tree is only built if no tree is given in the constructor of this class.
     * There is no need to build any tree if we loaded an .obj file.
     */
    private void buildKdTreeForRelations(List<Relation> relationList) {
        if (kdTreeRelations == null) {
            long time = -System.nanoTime();

            kdTreeRelations = new KdTree<>();
            kdTreeRelations.build(relationList);

            time += System.nanoTime();
            System.out.println("Built kd-tree for relations in " + time / 1_000_000L + "ms with depth: " + kdTreeRelations.getMaxDepth());
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
    private void buildKdTreeForWays(List<Way> wayList) {

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
     * The lists are built using the elements from the wayList
     */
    private HashMap<ElementGroup, List<Way>> getElementMap(List<Way> wayList) {
        HashMap<ElementGroup, List<Way>> elementMap = new HashMap<>();

        for (ElementGroup elementGroup : ElementGroup.values()) {
            elementMap.put(elementGroup, new ArrayList<>());
        }
        for (Way way : wayList) {

            ElementType type = way.getType();
            if (type != null) {

                ElementSize size = way.getElementSize();

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    if (elementGroup.getType() == type) {
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
     * @return list of MapTexts found by the kd-tree range search.
     */
    public List<MapText> getMapTexts() {
        return kdTreeMapTextSearchList;
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
        //Search Way kd-trees
        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.doShowElement(zoomLevel)) {
                if (kdTreeMap.containsKey(elementGroup)) {
                    List<Way> wayList = kdTreeMap.get(elementGroup).rangeSearch(boundingBox);
                    kdTreeSearchMap.put(elementGroup, wayList);
                }
            }
        }
        //Search Relation kd-tree and MapText kd-tree
        kdTreeRelationSearchList = kdTreeRelations.rangeSearch(boundingBox);
        kdTreeMapTextSearchList = kdTreeMapTexts.rangeSearch(boundingBox);
    }

    /**
     * Starts a nearest neighbor search for the kd-tree with the given query coords.
     * Will only search in kd-trees with an ElementGroup where navigation is possible.
     * <p>
     * When a list of nearby coords are found, we will then find the coords closest to the query.
     */
    public float[] kdTreeNearestNeighborSearch(float[] queryCoords, TransportOption transportOption) {
        float[] coordsList = new float[ElementGroup.values().size() * 2];

        int count = 0;
        for (ElementGroup elementGroup : ElementGroup.values()) {
            if (elementGroup.getType().canNavigate(transportOption)) {
                if (kdTreeMap.containsKey(elementGroup)) {
                    float[] coords = kdTreeMap.get(elementGroup).nearestNeighborSearch(queryCoords);
                    coordsList[count] = coords[0];
                    coordsList[count + 1] = coords[1];
                    count += 2;
                }
            }
        }
        float[] nearest = null;
        double minimumDistance = Double.MAX_VALUE;

        for (int i = 0; i < coordsList.length; i += 2) {
            float x = coordsList[i];
            float y = coordsList[i + 1];

            double distance = DistanceUtil.distTo(queryCoords[0], queryCoords[1], x, y);

            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearest = new float[]{x, y};
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

    public KdTree<MapText> getKdTreeMapTexts() {
        return kdTreeMapTexts;
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

    public HashMap<String, UserNode> getUserNodesMap() {
        return userNodesMap;
    }

    public void updateUserNodesMap() {
        userNodesMap.clear();
        for (UserNode userNode : userNodes) {
            userNodesMap.put(userNode.getName(), userNode);
        }
    }

    public DijkstraPath getDijkstra() {
        return dijkstraPath;
    }

    public TST<List<OsmAddress>> getAddressTries() {
        return addressTries;
    }
}
