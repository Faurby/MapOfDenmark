package bfst21.test;


import bfst21.address.OsmAddress;
import bfst21.address.TST;
import bfst21.osm.Node;
import bfst21.view.controllers.MainController;
import bfst21.view.controllers.NavigationBoxController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AddressSearchTest {

    @BeforeEach
    void setup() {

    }

    @Test
    public void emptySearchSuggestions() {
        List<String> suggestions = new ArrayList<>();
        List<OsmAddress> osmSuggestions = new ArrayList<>();

        String input = "amager";

        TST tst = new TST();
        tst.put("amagerbro1234", Arrays.asList(new OsmAddress(new Node(1, 1))));
        tst.updateSuggestions(input, suggestions, osmSuggestions);

        System.out.println(suggestions.size());
        System.out.println(osmSuggestions.size());
    }

    @Test
    public void emptyTextAreaProvokesWarning() {
        //Type ""
    }

    @Test
    public void hasSearchSuggestions() {
        //Type Amar
        //Type Gyld
        //Type negativeTest
    }

    @Test
    public void providesExactMatch() {
        //Type Rued Langgards Vej
    }

    @Test
    public void providesCorrectSuggestionsWhenExactMatch() {
        //Type Rued Langgards Vej 7
    }

    @Test
    public void providesSuggestionsWhenGibberish() {
        //Type AAAAA
        //Type trfdfh
        //Type 6789
        //Type ?!!!?
        //Type A?!!!?
    }
}
