package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.models.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public class XmlParserTest {

    private Model model;

    @BeforeEach
    void setUp() throws XMLStreamException, IOException, ClassNotFoundException {
        model = new Model("data/amager.osm", false);
        model.load();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void getBuildingsSize_correctAmount() {
        int actual = model.getMapData().getBuildings().size();
        assertEquals(67946, actual);
        //der mangler cirka 650 buildings fordi ...
        // der er relations med k="building", men lige nu ser vi slet ikke relations.
    }

    @Test
    public void getRelationsSize_correctAmount() {
        int actual = model.getMapData().getIdToRelation().getElements().size();
        assertEquals(1725, actual);
    }
}
