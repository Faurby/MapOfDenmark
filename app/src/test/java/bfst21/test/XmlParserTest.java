package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.models.Model;
import bfst21.models.Util;
import bfst21.osm.ElementGroup;
import bfst21.osm.ElementSize;
import bfst21.osm.ElementType;
import bfst21.pathfinding.Coordinate;
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
        int actual = model.getMapData().getWays(ElementGroup.getElementGroup(ElementType.BUILDING, ElementSize.DEFAULT)).size();
        assertEquals(67946, actual);
        //der mangler cirka 650 buildings fordi ...
        // der er relations med k="building", men lige nu ser vi slet ikke relations.
    }

    @Test
    public void getRelationsSize_correctAmount() {
        int actual = model.getMapData().getRelations().size();
        assertEquals(1725, actual);
    }

    @Test
    public void getCorrectDistanceBetween2Coordinates() {
        Coordinate c1 = new Coordinate(12.6224313f, 55.6571112f);
        Coordinate c2 = new Coordinate(12.6238016f, 55.6573865f);

        double distance = Util.distTo(c1.getX(), c1.getY(), c2.getX(), c2.getY());
        assertEquals(0.09125, distance, 0.0001);
    }
}
