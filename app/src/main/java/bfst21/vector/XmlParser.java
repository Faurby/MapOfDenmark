package bfst21.vector;

import bfst21.vector.osm.ExtendedWay;
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
        ExtendedWay extendedWay = null;
        List<Drawable> shapes = new ArrayList<>();
        List<Drawable> buildings = new ArrayList<>();
        List<Drawable> islands;
        List<Drawable> extendedWays = new ArrayList<>();
        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false, isBuilding = false, isExtendedWay = false;
        ArrayList<Way> coastlines = new ArrayList<>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = getFloat(reader, "minlon");
                            maxx = getFloat(reader, "maxlon");
                            maxy = getFloat(reader, "minlat") / -0.56f;
                            miny = getFloat(reader, "maxlat") / -0.56f;
                            break;
                        case "node":
                            long id = getLong(reader, "id");
                            float lon = getFloat(reader, "lon");
                            float lat = getFloat(reader, "lat");
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
                            } else {
                                if (way != null) {
                                    extendedWay = new ExtendedWay(k, v);
                                    isExtendedWay = true;
                                    extendedWay.setNodes(way.getNodes());
                                }
                            }
                            break;
                        case "nd":
                            long ref = getLong(reader, "ref");
                            way.add(idToNode.get(ref));
                            break;

                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            if (isCoastline) coastlines.add(way);
                            if (isBuilding) buildings.add(way);
                            if (isExtendedWay) extendedWays.add(extendedWay);
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);
        return new MapData(shapes, buildings, islands, extendedWays, minx, maxx, miny, maxy);
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

    private float getFloat(XMLStreamReader reader, String localName) {
        return Float.parseFloat(reader.getAttributeValue(null, localName));
    }

    private long getLong(XMLStreamReader reader, String localName) {
        return Long.parseLong(reader.getAttributeValue(null, localName));
    }
}
