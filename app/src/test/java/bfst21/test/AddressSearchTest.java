package bfst21.test;

import bfst21.address.OsmAddress;
import bfst21.address.TST;
import bfst21.models.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class AddressSearchTest {

    private TST addressTries;
    private List<String> suggestions;
    private List<OsmAddress> osmSuggestions;

    @BeforeEach
    void setup() throws XMLStreamException, IOException, ClassNotFoundException {
        Model model = new Model("data/amager.zip", false);
        model.load(true);

        addressTries = model.getMapData().getAddressTries();
        suggestions = new ArrayList<>();
        osmSuggestions = new ArrayList<>();
    }

    @Test
    public void updateSuggestions_partialStreet_correctOutput() {
        addressTries.updateSuggestions("Amag", suggestions, osmSuggestions);

        assertEquals(10, suggestions.size());
        assertEquals(10, osmSuggestions.size());
    }

    @Test
    public void updateSuggestions_fullStreet_correctOutput() {
        addressTries.updateSuggestions("Amagerbrogade", suggestions, osmSuggestions);

        assertEquals(330, suggestions.size());
        assertEquals(330, osmSuggestions.size());
    }

    @Test
    public void updateSuggestions_streetWithNumber_correctOutput() {
        addressTries.updateSuggestions("Amagerbrogade 2,", suggestions, osmSuggestions);

        assertEquals(1, suggestions.size());
        assertEquals(1, osmSuggestions.size());
    }


    @Test
    public void updateSuggestions_fullAddress_correctOutput() {
        addressTries.updateSuggestions("Amagerbrogade 2, København S 2300", suggestions, osmSuggestions);

        assertEquals(1, suggestions.size());
        assertEquals(1, osmSuggestions.size());
    }

    @Test
    public void updateSuggestions_randomSearch_correctOutput() {
        addressTries.updateSuggestions("AAAAAAAAAAAAAA", suggestions, osmSuggestions);

        assertEquals(1, suggestions.size());
        assertEquals(1, osmSuggestions.size());

        addressTries.updateSuggestions("trfdfhha", suggestions, osmSuggestions);

        assertEquals(20, suggestions.size());
        assertEquals(20, osmSuggestions.size());

        addressTries.updateSuggestions("67899425", suggestions, osmSuggestions);

        assertEquals(0, suggestions.size());
        assertEquals(0, osmSuggestions.size());

        addressTries.updateSuggestions("?!!!?!", suggestions, osmSuggestions);

        assertEquals(0, suggestions.size());
        assertEquals(0, osmSuggestions.size());

        addressTries.updateSuggestions("A?!!!?", suggestions, osmSuggestions);

        assertEquals(128, suggestions.size());
        assertEquals(128, osmSuggestions.size());
    }
}
