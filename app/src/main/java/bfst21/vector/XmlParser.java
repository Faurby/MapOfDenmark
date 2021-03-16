package bfst21.vector;

import bfst21.vector.osm.*;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;


public class XmlParser {

    private XMLStreamReader reader;

    public MapData loadOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(filename));
    }

    //TODO: Consider using a stack for parent elements
    // You can then peek to see what type of parent it has
    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        reader = XMLInputFactory
            .newInstance()
            .createXMLStreamReader(new BufferedInputStream(input));

        Way way = null;
        ExtendedWay extendedWay = null;
        Relation relation = null;

        LongIndex idToNode = new LongIndex();
        LongIndex idToWay = new LongIndex();
        LongIndex idToRelation = new LongIndex();

        List<Drawable> shapes = new ArrayList<>();

        List<Way> buildings = new ArrayList<>();
        List<Way> extendedWays = new ArrayList<>();
        List<Way> coastlines = new ArrayList<>();
        List<Way> islands;

        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false, isBuilding = false;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = getFloat("minlon");
                            maxx = getFloat("maxlon");
                            maxy = getFloat("minlat") / -0.56f;
                            miny = getFloat("maxlat") / -0.56f;
                            break;

                        case "node":
                            long nodeID = getLong("id");
                            float lon = getFloat("lon");
                            float lat = getFloat("lat");
                            idToNode.put(new Node(nodeID, lat, lon));
                            break;

                        case "way":
                            long wayID = getLong("id");
                            way = new Way(wayID);
                            extendedWay = new ExtendedWay(wayID);
                            isCoastline = false;
                            isBuilding = false;
                            break;

                        case "relation":
                            long relationID = getLong("id");
                            relation = new Relation(relationID);
                            break;

                        case "member":
                            String type = getAttribute("type");
                            String memRef = getAttribute("ref");
                            if (type != null) {
                                if (type.equalsIgnoreCase("node")) {
                                    Node memNode = (Node) idToNode.get(Long.parseLong(memRef));
                                    if (memNode != null) {
                                        relation.addMember(memNode);
                                    }
                                } else if (type.equalsIgnoreCase("way")) {
                                    Way memWay = (Way) idToWay.get(Long.parseLong(memRef));
                                    if (memWay != null) {
                                        relation.addMember(memWay);
                                    }
                                } else if (type.equalsIgnoreCase("relation")) {
                                    Relation memRelation = (Relation) idToRelation.get(Long.parseLong(memRef));
                                    if (memRelation != null) {
                                        relation.addMember(memRelation.getID());
                                    }
                                }
                            }
                            break;

                        case "tag":
                            String key = getAttribute("k");
                            String value = getAttribute("v");
                            if (way != null) {
                                if (key.equals("natural") && value.equals("coastline")) {
                                    isCoastline = true;
                                } else if (key.equals("building")) {
                                    isBuilding = true;
                                } else {
                                    extendedWay.addTag(key, value);
                                }
                            }
                            break;

                        case "nd":
                            long ref = getLong("ref");
                            way.add((Node) idToNode.get(ref));
                            break;
                    }
                    break;

                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            idToWay.put(way);
                            if (isCoastline) {
                                coastlines.add(way);

                            } else if (isBuilding) {
                                buildings.add(way);

                            } else if (extendedWay != null) {
                                extendedWays.add(extendedWay);
                                extendedWay.setNodes(way.getNodes());
                            }
                            break;

                        case "relation":
                            idToRelation.put(relation);
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);
        return new MapData(shapes, buildings, islands, extendedWays, idToRelation, minx, maxx, miny, maxy);
    }

    private List<Way> mergeCoastLines(List<Way> coastlines) {
        Map<Node, Way> pieces = new HashMap<>();

        for (Way coast : coastlines) {
            Way before = pieces.remove(coast.first());
            Way after = pieces.remove(coast.last());
            if (before == after) after = null;
            Way merged = Way.merge(before, coast, after);
            pieces.put(merged.first(), merged);
            pieces.put(merged.last(), merged);
        }
        List<Way> merged = new ArrayList<>();
        pieces.forEach((node, way) -> {
            if (way.last() == node) {
                merged.add(way);
            }
        });
        return merged;
    }

    private String getAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    private float getFloat(String name) {
        return Float.parseFloat(getAttribute(name));
    }

    private long getLong(String name) {
        return Long.parseLong(getAttribute(name));
    }
}
