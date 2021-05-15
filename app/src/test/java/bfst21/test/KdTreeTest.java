package bfst21.test;

import bfst21.osm.MapWay;
import bfst21.osm.Node;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdTree;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class KdTreeTest {

    @Test
    public void buildKdTree_correctNearestNeighbor_correctRangeSearch() {
        KdTree<MapWay> kdTree = new KdTree<>();

        float[] coords1 = new float[]{1, 1};
        float[] coords2 = new float[]{2, 2};
        float[] coords3 = new float[]{3, 3};
        float[] coords4 = new float[]{4, 4};
        float[] coords5 = new float[]{5, 5};

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

        assertEquals(1, kdTree.getMaxDepth());

        float[] nearestCoords1 = kdTree.nearestNeighborSearch(coords1);
        assertEquals(coords1[0], nearestCoords1[0]);
        assertEquals(coords1[1], nearestCoords1[1]);

        float[] nearestCoords4 = kdTree.nearestNeighborSearch(coords4);
        assertEquals(coords4[0], nearestCoords4[0]);
        assertEquals(coords4[1], nearestCoords4[1]);

        BoundingBox boundingBox = new BoundingBox(4, 5, 4, 5);

        List<MapWay> searchList = kdTree.rangeSearch(boundingBox);
        float[] wayCoords = searchList.get(0).getCoords();

        assertEquals(coords4[0], wayCoords[0]);
        assertEquals(coords4[1], wayCoords[1]);
        assertEquals(coords5[0], wayCoords[2]);
        assertEquals(coords5[1], wayCoords[3]);
    }
}
