package bfst21.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class XmlParserTest {

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    public void testCountBuildings() throws IOException, XMLStreamException, ClassNotFoundException {
        Model model = new Model("data/amager.zip", false);

        assertEquals(67946, model.getMapData().getBuildings().size());
        //der mangler cirka 650 buildings fordi ...
        // der er relations med k="building", men lige nu ser vi slet ikke relations.
    }
}
