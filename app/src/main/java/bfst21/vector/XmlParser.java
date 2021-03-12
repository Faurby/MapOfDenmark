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
        LongIndex idToNode = new LongIndex();
        Way way = null;
        List<Drawable> shapes = new ArrayList<>();
        List<Drawable> buildings = new ArrayList<>();
        List<Drawable> islands;
        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false;
        boolean isBuilding = false;
        ArrayList<Way> coastlines = new ArrayList<>();
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
                            long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            idToNode.put(new Node(id, lat, lon));
                            break;
                        case "way":
                            way = new Way();
                            isCoastline = false;
                            isBuilding = false;
                            break;
                        case "tag":
                            String k = reader.getAttributeValue(null, "k");
                            String v = reader.getAttributeValue(null, "v");
                            if (k.equals("natural") && v.equals("coastline")) {
                                isCoastline = true;
                            } else if (k.equals("building")) {
                                isBuilding = true;
                            }
                            break;
                        case "nd":
                            long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.add(idToNode.get(ref));
                            break;

                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            if (isCoastline) coastlines.add(way);
                            if (isBuilding) buildings.add(way);
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);
        return new MapData(shapes, buildings, islands, minx, maxx, miny, maxy);
    }

    private List<Drawable> mergeCoastLines(ArrayList<Way> coastlines) {
        Map<Node, Way> pieces = new HashMap<>();
        for (Way coast : coastlines) {
            Way before = pieces.remove(coast.first());
            Way after = pieces.remove(coast.last());
            if (before == after) after = null;
            Way merged = Way.merge(before, coast, after);
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
