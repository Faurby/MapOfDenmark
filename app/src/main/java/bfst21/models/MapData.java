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

    private final HashMap<ElementGroup, List<Way>> kdTreeSearchMap = new HashMap<>(),
                                                   rTreeSearchMap = new HashMap<>();

    private KdTree<Relation> kdTreeRelations;
    private RTree<Integer, Relation> rTreeRelations;

    private List<Relation> kdTreeRelationSearchList = new ArrayList<>(),
                           rTreeRelationSearchList = new ArrayList<>();

    private final List<UserNode> userNodes = new ArrayList<>();
    private final List<Way> islands;

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
            ElementLongIndex<Relation> relationLongIndex,
            HashMap<ElementGroup, KdTree<Way>> kdTreeMap,
            HashMap<ElementGroup, RTree<Integer, Way>> rTreeMap,
            KdTree<Relation> kdTreeRelations,
            RTree<Integer, Relation> rTreeRelations,
            DirectedGraph directedGraph,
            float minX,
            float maxX,
            float minY,
            float maxY) {

        this.directedGraph = directedGraph;
        this.kdTreeMap = kdTreeMap;
        this.rTreeMap = rTreeMap;
        this.kdTreeRelations = kdTreeRelations;
        this.rTreeRelations = rTreeRelations;
        this.islands = islands;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        //We need to initially fill the search HashMaps with empty Lists.
        //This is to avoid issues when accessing the search map before a range search.
        for (ElementGroup elementGroup : ElementGroup.values()) {
            kdTreeSearchMap.put(elementGroup, new ArrayList<>());
            rTreeSearchMap.put(elementGroup, new ArrayList<>());
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
                    relationList.add(relation);
                }
            }

            //Build the data structures if none is present.
            if (directedGraph == null) {
                buildDirectedGraph(wayLongIndex.getElements());
            }
            if (kdTreeMap == null && rTreeMap == null) {
                buildSearchTreesForWays(wayLongIndex.getElements());
            }
            if (kdTreeRelations == null && rTreeRelations == null) {
                buildSearchTreesForRelations(relationList);
            }
        }
    }

    //TODO: Figure out how to merge relations...
    private void mergeRelations(List<Relation> relationList) {
        for (Relation relation : relationList) {
            for (Way way : relation.getWays()) {

            }
        }
    }

    /**
     * Builds a directed graph used for path finding.
     */
    public void buildDirectedGraph(List<Way> wayList) {
        directedGraph = new DirectedGraph(wayList.size());
        System.out.println("Building directed graph for path finding...");

        int idCount = 0;
        for (Way way : wayList) {
            if (way.getType() != null) {
                if (way.canNavigate()) {
                    int maxSpeed = way.getMaxSpeed();

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
     * Builds kd-tree or r-tree for Relations if the option is enabled
     * <p>
     * The trees are only built if no trees are given in the constructor of this class.
     * There is no need to build any trees if we loaded an .obj file.
     */
    public void buildSearchTreesForRelations(List<Relation> relationList) {
        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTreeRelations == null) {
                kdTreeRelations = new KdTree<>();
                kdTreeRelations.build(relationList);
                System.out.println("Built kd-tree for relations with depth: " + kdTreeRelations.getMaxDepth());
            }
        } else if (options.getBool(Option.USE_R_TREE)) {
            if (rTreeRelations == null) {
                rTreeRelations = RTree.star().maxChildren(6).create();

                for (Relation relation : relationList) {
                    rTreeRelations = rTreeRelations.add(0, relation);
                }
                System.out.println("Built r-tree for relations");
            }
        }
    }

    /**
     * Builds a HashMap of kd-trees or r-trees for Ways if the option is enabled.
     * <p>
     * A tree is built for every ElementGroup as we only need certain trees at a given zoom level.
     * <p>
     * The trees are only built if no trees are given in the constructor of this class.
     * There is no need to build any trees if we loaded an .obj file.
     */
    public void buildSearchTreesForWays(List<Way> wayList) {

        if (options.getBool(Option.USE_KD_TREE)) {
            if (kdTreeMap == null) {
                kdTreeMap = new HashMap<>();

                System.out.println("Building kd-trees...");
                HashMap<ElementGroup, List<Way>> wayMap = getElementMap(wayList);

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    List<Way> innerWayList = wayMap.get(elementGroup);

                    if (innerWayList.size() > 0) {
                        KdTree<Way> kdTree = new KdTree<>();
                        this.kdTreeMap.put(elementGroup, kdTree);
                        kdTree.build(innerWayList);
                        System.out.println("Built kd-tree for " + elementGroup.toString() + " with depth: " + kdTree.getMaxDepth());
                    }
                }
            }
        } else if (options.getBool(Option.USE_R_TREE)) {
            if (rTreeMap == null) {
                rTreeMap = new HashMap<>();

                System.out.println("Building r-trees...");
                HashMap<ElementGroup, List<Way>> wayMap = getElementMap(wayList);

                for (ElementGroup elementGroup : ElementGroup.values()) {

                    RTree<Integer, Way> rTree = RTree.star().maxChildren(6).create();

                    for (Way way : wayMap.get(elementGroup)) {
                        rTree = rTree.add(0, way);
                    }
                    this.rTreeMap.put(elementGroup, rTree);
                    System.out.println("Built r-tree for " + elementGroup.toString());
                }
            }
        }
    }

    /**
     * @return HashMap containing every ElementGroup and their list of Ways
     * The lists are built using the elements from the wayLongIndex
     */
    public HashMap<ElementGroup, List<Way>> getElementMap(List<Way> wayList) {
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
     * @return list of Relations found by the kd-tree or r-tree range search.
     */
    public List<Relation> getRelations() {
        if (options.getBool(Option.USE_KD_TREE)) {
            return kdTreeRelationSearchList;

        } else if (options.getBool(Option.USE_R_TREE)) {
            return rTreeRelationSearchList;
        }
        return new ArrayList<>();
    }

    /**
     * Returns a list of Ways with the specific ElementGroup.
     * List is retrieved from search map filled by the kd-tree or r-tree range search.
     *
     * @param elementGroup specific ElementGroup.
     * @return list of Ways with the specific ElementGroup.
     */
    public List<Way> getWays(ElementGroup elementGroup) {
        if (elementGroup.getType() == ElementType.ISLAND) {
            return islands;

        } else if (options.getBool(Option.USE_KD_TREE)) {
            return kdTreeSearchMap.get(elementGroup);

        } else if (options.getBool(Option.USE_R_TREE)) {
            return rTreeSearchMap.get(elementGroup);
        }
        return new ArrayList<>();
    }

    /**
     * Starts a range search within the screens BoundingBox for all r-trees
     * if the specific ElementGroup is enabled at the given zoomLevel.
     */
    public void rTreeRangeSearch(double x1, double y1, double x2, double y2, double zoomLevel) {
        //Search way r-trees
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
        //Search relation r-tree
        Iterable<Entry<Integer, Relation>> results =
            rTreeRelations.search(Geometries.rectangle(x1, y1, x2, y2));

        rTreeRelationSearchList = new ArrayList<>();

        for (Entry<Integer, Relation> result : results) {
            Relation relation = result.geometry();
            rTreeRelationSearchList.add(relation);
        }
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

    public HashMap<ElementGroup, KdTree<Way>> getKdTreeMap() {
        return kdTreeMap;
    }

    public HashMap<ElementGroup, RTree<Integer, Way>> getRTreeMap() {
        return rTreeMap;
    }

    public KdTree<Relation> getKdTreeRelations() {
        return kdTreeRelations;
    }

    public RTree<Integer, Relation> getrTreeRelations() {
        return rTreeRelations;
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
}
