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

    @BeforeEach
    void setup() throws XMLStreamException, IOException, ClassNotFoundException {
        Model model = new Model("data/amager.zip", false);
        model.load(true);

        addressTries = model.getMapData().getAddressTries();
        suggestions = new ArrayList<>();
    }

    @Test
    public void findAddress_partialStreet_correctOsmAddress() {
        OsmAddress osmAddress = addressTries.findAddress("Amag");

        assertEquals("Amag", osmAddress.toString());
    }

    @Test
    public void findAddress_fullStreet_correctOsmAddress() {
        OsmAddress osmAddress = addressTries.findAddress("Amagerbrogade");

        assertEquals("Amagerbrogade", osmAddress.toString());
    }

    @Test
    public void findAddress_streetWithNumber_correctOsmAddress() {
        OsmAddress osmAddress = addressTries.findAddress("Amagerbrogade 2");

        assertEquals("Amagerbrogade 2", osmAddress.toString());
    }

    @Test
    public void findAddress_fullAddress_correctOsmAddress() {
        OsmAddress osmAddress = addressTries.findAddress("Amagerbrogade 2 København S 2300");

        assertEquals("Amagerbrogade 2 København S 2300", osmAddress.toString());
    }

    @Test
    public void findAddress_randomSearch_correctOsmAddress() {
        OsmAddress osmAddress1 = addressTries.findAddress("AAAAAAAAAAAAAA");
        assertEquals("AAAAAAAAAAAAAA", osmAddress1.toString());
    }

    @Test
    public void updateSuggestions_partialStreet_correctOutput() {
        addressTries.updateAddressSuggestions("Amag", suggestions);

        assertEquals(10, suggestions.size());

        OsmAddress osmAddress = addressTries.findAddress("");
        assertEquals("", osmAddress.toString());
    }

    @Test
    public void updateSuggestions_fullStreet_correctOutput() {
        addressTries.updateAddressSuggestions("Amagerbrogade", suggestions);

        assertEquals(330, suggestions.size());
    }

    @Test
    public void updateSuggestions_streetWithNumber_correctOutput() {
        addressTries.updateAddressSuggestions("Amagerbrogade 2", suggestions);

        assertEquals(128, suggestions.size());
    }

    @Test
    public void updateSuggestions_fullAddress_correctOutput() {
        addressTries.updateAddressSuggestions("Amagerbrogade 2 København S 2300", suggestions);

        assertEquals(1, suggestions.size());
    }

    @Test
    public void updateSuggestions_randomSearch_correctOutput() {
        addressTries.updateAddressSuggestions("AAAAAAAAAAAAAA", suggestions);

        assertEquals(1, suggestions.size());

        addressTries.updateAddressSuggestions("trfdfhha", suggestions);

        assertEquals(20, suggestions.size());

        addressTries.updateAddressSuggestions("67899425", suggestions);

        assertEquals(0, suggestions.size());

        addressTries.updateAddressSuggestions("?!!!?!", suggestions);

        assertEquals(0, suggestions.size());

        addressTries.updateAddressSuggestions("A?!!!?", suggestions);

        assertEquals(128, suggestions.size());
    }
}
