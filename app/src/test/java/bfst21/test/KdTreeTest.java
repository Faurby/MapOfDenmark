package bfst21.test;

import bfst21.osm.MapWay;
import bfst21.osm.Node;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class KdTreeTest {

    private final KdTree<MapWay> kdTree = new KdTree<>();
    private final float[] coords1 = new float[]{1, 1};
    private final float[] coords2 = new float[]{2, 2};
    private final float[] coords3 = new float[]{3, 3};
    private final float[] coords4 = new float[]{4, 4};
    private final float[] coords5 = new float[]{5, 5};
    private final float[] coords6 = new float[]{0, 2};


    @BeforeEach
    private void setup() {
        Node node1 = new Node(coords1);
        Node node2 = new Node(coords2);
        Node node3 = new Node(coords3);
        Node node4 = new Node(coords4);
        Node node5 = new Node(coords5);

        MapWay way1 = new MapWay();
        way1.setNodes(Arrays.asList(node1, node2));

        MapWay way2 = new MapWay();
        way2.setNodes(Arrays.asList(node2, node3));

        MapWay way3 = new MapWay();
        way3.setNodes(Arrays.asList(node1, node3));

        MapWay way4 = new MapWay();
        way4.setNodes(Arrays.asList(node2, node3));

        MapWay way5 = new MapWay();
        way5.setNodes(Arrays.asList(node4, node5));

        List<MapWay> wayList = Arrays.asList(way1, way2, way3, way4, way5);

        kdTree.build(wayList);
    }

    @Test
    public void buildKdTree_correctRangeSearch() {

        BoundingBox boundingBox = new BoundingBox(4, 5, 4, 5);

        List<MapWay> searchList = kdTree.rangeSearch(boundingBox);
        float[] wayCoords = searchList.get(0).getCoords();

        assertEquals(coords4[0], wayCoords[0]);
        assertEquals(coords4[1], wayCoords[1]);
        assertEquals(coords5[0], wayCoords[2]);
        assertEquals(coords5[1], wayCoords[3]);
    }

    @Test
    public void buildKdTree_correctRangeSearchOnlyLeftSide() {
        BoundingBox boundingBox = new BoundingBox(0, 1, 0, 1);

        List<MapWay> searchList = kdTree.rangeSearch(boundingBox);
        float[] wayCoords = searchList.get(1).getCoords();

        assertEquals(coords1[0], wayCoords[0]);
        assertEquals(coords1[1], wayCoords[1]);
        assertEquals(coords2[0], wayCoords[2]);
        assertEquals(coords2[1], wayCoords[3]);

        float[] wayCoords2 = searchList.get(0).getCoords();

        assertEquals(coords1[0], wayCoords2[0]);
        assertEquals(coords1[1], wayCoords2[1]);
        assertEquals(coords3[0], wayCoords2[2]);
        assertEquals(coords3[1], wayCoords2[3]);
    }

    @Test
    public void kdTree_correctDepth() {
        assertEquals(1, kdTree.getMaxDepth());
    }

    @Test
    public void kdTree_correctNearestNeighbor() {
        float[] nearestCoords1 = kdTree.nearestNeighborSearch(coords1);
        assertEquals(coords1[0], nearestCoords1[0]);
        assertEquals(coords1[1], nearestCoords1[1]);

        float[] nearestCoords4 = kdTree.nearestNeighborSearch(coords4);
        assertEquals(coords4[0], nearestCoords4[0]);
        assertEquals(coords4[1], nearestCoords4[1]);

        float[] nearestCoords6 = kdTree.nearestNeighborSearch(coords6);
        assertEquals(coords1[0], nearestCoords6[0]);
        assertEquals(coords1[1], nearestCoords6[1]);

    }
}
