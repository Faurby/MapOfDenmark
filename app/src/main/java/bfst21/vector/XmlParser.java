package bfst21.vector;

import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class XmlParser {



    public MapData loadOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(filename));
    }

    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(new BufferedInputStream(input));
        var idToNode = new LongIndex();
        Way way = null;
        List<Drawable> shapes = new ArrayList<>();
        List<Drawable> buildings = new ArrayList<>();
        List<Drawable> islands = new ArrayList<>();
        float minx = 0;
        float miny = 0;
        float maxx = 0;
        float maxy = 0;
        boolean iscoastline = false;
        boolean isbuilding = false;
        var coastlines = new ArrayList<Way>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxx = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxy = Float.parseFloat(reader.getAttributeValue(null, "minlat")) / -0.56f;
                            miny = Float.parseFloat(reader.getAttributeValue(null, "maxlat")) / -0.56f;
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            idToNode.put(new Node(id, lat, lon));
                            break;
                        case "way":
                            way = new Way();
                            iscoastline = false;
                            isbuilding = false;
                            break;
                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (k.equals("natural") && v.equals("coastline")) {
                                iscoastline = true;
                            } else if (k.equals("building") && v.equals("yes")) {
                                isbuilding = true;
                            }
                            break;
                        case "nd":
                            var ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.add(idToNode.get(ref));
                            break;

                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            if (iscoastline) coastlines.add(way);
                            if (isbuilding) buildings.add(way);
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);
        return new MapData( shapes, buildings, islands, minx, miny, maxx, maxy);
    }

    private List<Drawable> mergeCoastLines(ArrayList<Way> coastlines) {
        Map<Node, Way> pieces = new HashMap<>();
        for (var coast : coastlines) {
            var before = pieces.remove(coast.first());
            var after = pieces.remove(coast.last());
            if (before == after) after = null;
            var merged = Way.merge(before, coast, after);
            pieces.put(merged.first(), merged);
            pieces.put(merged.last(), merged);
        }
        List<Drawable> merged = new ArrayList<>();
        pieces.forEach((node, way) -> {
            if (way.last() == node) {
                merged.add(way);
            }
        });
        return merged;
    }
}
