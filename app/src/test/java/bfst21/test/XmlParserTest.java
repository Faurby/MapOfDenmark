package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.models.DistanceUtil;
import bfst21.models.TransportOption;
import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.BoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;


public class XmlParserTest {

    private Model model;

    @BeforeEach
    void setup() throws XMLStreamException, IOException, ClassNotFoundException {
        model = new Model("data/amager.zip", false);
        model.load(true);
    }

    @Test
    public void getBuildingsSize_correctAmount() {
        ElementGroup elementGroup = new ElementGroup(ElementType.BUILDING, ElementSize.SMALL);
        List<MapWay> ways = model.getMapData().getKdTree(elementGroup).getAllElements();

        assertEquals(67292, ways.size());
    }

    @Test
    public void getRelationsSize_correctAmount() {
        List<Relation> relations = model.getMapData().getKdTreeRelations().getAllElements();

        assertEquals(807, relations.size());
    }

    @Test
    public void buildDirectedGraph_correctVertexAndEdgeAmount() {
        MapData mapData = model.getMapData();
        DirectedGraph directedGraph = mapData.getDirectedGraph();

        assertEquals(88797, directedGraph.getVertexAmount());
        assertEquals(201818, directedGraph.getEdgeAmount());
    }

    @Test
    public void kdTreeRangeSearch_correctResult() {
        MapData mapData = model.getMapData();

        BoundingBox boundingBox = new BoundingBox(12.49f, 12.69f, -99.49f, -99.35f);
        mapData.kdTreeRangeSearch(boundingBox, 7000.0D);

        assertEquals(11, mapData.getMapTexts().size());
        assertEquals(716, mapData.getRelations().size());

        ElementGroup eg1 = new ElementGroup(ElementType.MOTORWAY, ElementSize.DEFAULT);
        assertEquals(3, mapData.getWays(eg1).size());

        ElementGroup eg2 = new ElementGroup(ElementType.PRIMARY, ElementSize.DEFAULT);
        assertEquals(204, mapData.getWays(eg2).size());
    }

    @Test
    public void kdTreeNearestNeighborSearch_foundCorrectCoords() {
        MapData mapData = model.getMapData();

        float[] query = new float[]{12.60263f, -99.39921f};
        float[] found = mapData.kdTreeNearestNeighborSearch(query, TransportOption.ALL);

        assertEquals(12.60261f, found[0], 0.01D);
        assertEquals(-99.39921f, found[1], 0.01D);
    }

    @Test
    public void haversineFormula_correctDistance1() {
        double lon1 = 12.6224313f;
        double lat1 = 55.6571112f;
        double lon2 = 12.6238016f;
        double lat2 = 55.6573865f;

        lat1 = -lat1 / 0.56f;
        lat2 = -lat2 / 0.56f;

        double distance = DistanceUtil.distTo(lon1, lat1, lon2, lat2);
        assertEquals(0.09125, distance, 0.0001);
    }

    @Test
    public void haversineFormula_correctDistance2() {
        double lon1 = 12.485718754160853;
        double lat1 = 55.71871866715029;
        double lon2 = 11.559933323788288;
        double lat2 = 55.58933736433195;

        lat1 = -lat1 / 0.56f;
        lat2 = -lat2 / 0.56f;

        double distance = DistanceUtil.distTo(lon1, lat1, lon2, lat2);
        assertEquals(59.83386186513035, distance, 0.1);
    }
}
