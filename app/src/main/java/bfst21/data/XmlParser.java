package bfst21.data;

import bfst21.models.Option;
import bfst21.models.Options;
import bfst21.osm.*;
import bfst21.view.Drawable;
import bfst21.models.MapData;
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

    public MapData loadOSM(String fileName) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(fileName));
    }

    //TODO: Consider using a stack for parent elements
    // You can then peek to see what type of parent it has
    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        Options options = Options.getInstance();

        XMLStreamReader reader = XMLInputFactory
                .newInstance()
                .createXMLStreamReader(new BufferedInputStream(input));

        Way way = null;
        Relation relation = null;

        boolean useRtree = options.getBool(Option.USE_R_TREE);

        NodeLongIndex idToNode = new NodeLongIndex();
        WayLongIndex idToWay = new WayLongIndex();
        ElementLongIndex idToRelation = new ElementLongIndex();

        List<Drawable> shapes = new ArrayList<>();

        List<Way> ways = new ArrayList<>();
        List<TreeWay> treeWays = new ArrayList<>();
        List<Way> coastlines = new ArrayList<>();
        List<Way> islands;

        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false;

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
                            long nodeID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));

                            idToNode.put(new NodeID(nodeID, new Node(lat, lon)));
                            break;

                        case "way":
                            long wayID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            if (useRtree) {
                                way = new TreeWay(wayID);
                            } else {
                                way = new Way(wayID);
                            }
                            isCoastline = false;
                            break;

                        case "relation":
                            if (options.getBool(Option.LOAD_RELATIONS)) {
                                long relationID = Long.parseLong(reader.getAttributeValue(null, "id"));
                                relation = new Relation(relationID);
                            }
                            break;

                        case "member":
                            if (options.getBool(Option.LOAD_RELATIONS)) {
                                String type = reader.getAttributeValue(null, "type");
                                String memRef = reader.getAttributeValue(null, "ref");
                                if (type != null) {
                                    if (type.equalsIgnoreCase("node")) {
                                        Node memNode = idToNode.get(Long.parseLong(memRef));
                                        if (memNode != null) {
                                            relation.addMember(memNode);
                                        }
                                    } else if (type.equalsIgnoreCase("way")) {
                                        Way memWay = idToWay.get(Long.parseLong(memRef));
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
                            }
                            break;

                        case "tag":
                            String key = reader.getAttributeValue(null, "k");
                            String value = reader.getAttributeValue(null, "v");
                            if (way != null) {
                                if (key.equals("natural") && value.equals("coastline")) {
                                    isCoastline = true;
                                } else if (options.getBool(Option.LOAD_TAGGED_WAYS)) {
                                    way.addTag(key, value.toUpperCase());
                                }
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
                            idToWay.put(way);
                            if (isCoastline) {
                                coastlines.add(way);

                            } else if (way.getTags() != null && options.getBool(Option.LOAD_TAGGED_WAYS)) {
                                if (useRtree) {
                                    treeWays.add((TreeWay) way);
                                } else {
                                    ways.add(way);
                                }
                            }
                            break;

                        case "relation":
                            if (options.getBool(Option.LOAD_RELATIONS)) {
                                idToRelation.put(relation);
                            }
                            break;
                    }
                    break;
            }
        }
        islands = mergeCoastLines(coastlines);

        return new MapData(
            shapes,
            islands,
            ways,
            treeWays,
            idToRelation,
            null,
            minx,
            maxx,
            miny,
            maxy
        );
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
}
