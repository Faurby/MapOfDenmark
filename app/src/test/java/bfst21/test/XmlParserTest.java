package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.models.Model;
import bfst21.osm.Node;
import bfst21.osm.WayType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public class XmlParserTest {

    private Model model;

    @BeforeEach
    void setUp() throws XMLStreamException, IOException, ClassNotFoundException {
        model = new Model("data/amager.zip", false);
        model.load(true);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void getBuildingsSize_correctAmount() {
        int actual = model.getMapData().getWays(WayType.BUILDING).size();
        assertEquals(67946, actual);
        //der mangler cirka 650 buildings fordi ...
        // der er relations med k="building", men lige nu ser vi slet ikke relations.
    }

    @Test
    public void getRelationsSize_correctAmount() {
        int actual = model.getMapData().getIdToRelation().getElements().size();
        assertEquals(1725, actual);
    }

    @Test
    public void getCorrectDistanceBetween2Nodes() {
        Node node1 = new Node(12.6224313f, 55.6571112f);
        Node node2 = new Node(12.6238016f, 55.6573865f);
        double distance = node1.distTo(node2);
        assertEquals(0.09125, distance, 0.0001);
    }
}
