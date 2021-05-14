package bfst21.test;

import bfst21.models.Model;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchTest {

    @BeforeEach
    void setup() {

    }

    @Test
    public void emptySearchSuggestions() {
        //Type " "
        //Type
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
