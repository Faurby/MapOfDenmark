package bfst21.data;

import bfst21.address.TST;
import bfst21.models.DisplayOption;
import bfst21.models.DisplayOptions;
import bfst21.osm.*;
import bfst21.models.MapData;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import com.ctc.wstx.osgi.InputFactoryProviderImpl;
import org.codehaus.stax2.XMLInputFactory2;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * XmlParser is used to parse the XML data given by OpenStreetMaps.
 * Nodes are parsed an added to the relevant Ways or Relations as a float array of coordinates.
 *
 * An ElementType can be found by looking at the tags of a Way or Relation.
 *
 * An OsmAddress is created by looking at the address tags of a Node.
 * It is then placed in a ternary search tries for address searching.
 *
 * Coastlines will be merged.
 */
public class XmlParser {

    public MapData loadOSM(String fileName) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(fileName));
    }

    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        long time = -System.nanoTime();

        DisplayOptions displayOptions = DisplayOptions.getInstance();

        InputFactoryProviderImpl iprovider = new InputFactoryProviderImpl();

        XMLInputFactory2 xmlif = iprovider.createInputFactory();
        xmlif.configureForSpeed();

        XMLStreamReader reader = xmlif.createXMLStreamReader(new BufferedInputStream(input));

        Way way = null;
        Relation relation = null;
        OsmAddress osmAddress = null;
        Node node = null;
        ElementType elementType = null;
        //TriesMap triesMap = new TriesMap();
        TST<float[]> addressTries = new TST<>();

        ElementLongIndex<NodeID> nodeLongIndex = new ElementLongIndex<>();
        ElementLongIndex<Way> wayLongIndex = new ElementLongIndex<>();
        ElementLongIndex<Relation> relationLongIndex = new ElementLongIndex<>();

        List<Way> coastlines = new ArrayList<>();
        List<Way> islands;

        float minX = 0, minY = 0, maxX = 0, maxY = 0;
        boolean isCoastline = false;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {

                        case "bounds":
                            minX = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxX = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxY = Float.parseFloat(reader.getAttributeValue(null, "minlat")) / -0.56f;
                            minY = Float.parseFloat(reader.getAttributeValue(null, "maxlat")) / -0.56f;
                            break;

                        case "node":
                            long nodeID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));

                            //Convert latitude to ensure the map is drawn correctly.
                            lat = -lat / 0.56f;

                            node = new Node(lon, lat);
                            nodeLongIndex.put(new NodeID(nodeID, node));
                            break;

                        case "way":
                            long wayID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            way = new Way(wayID);
                            isCoastline = false;
                            break;

                        case "relation":
                            if (displayOptions.getBool(DisplayOption.LOAD_RELATIONS)) {
                                long relationID = Long.parseLong(reader.getAttributeValue(null, "id"));
                                relation = new Relation(relationID);
                            }
                            break;

                        case "member":
                            if (displayOptions.getBool(DisplayOption.LOAD_RELATIONS)) {
                                String type = reader.getAttributeValue(null, "type");
                                String memRef = reader.getAttributeValue(null, "ref");
                                if (type != null) {
                                    if (type.equalsIgnoreCase("node")) {
                                        NodeID memNode = nodeLongIndex.get(Long.parseLong(memRef));
                                        if (memNode != null) {
                                            relation.addNode(memNode.getNode().getCoords());
                                        }
                                    } else if (type.equalsIgnoreCase("way")) {
                                        Way memWay = wayLongIndex.get(Long.parseLong(memRef));
                                        if (memWay != null) {
                                            String role = reader.getAttributeValue(null, "role");
                                            if (role != null && !role.isEmpty()) {
                                                memWay.setRole(role);
                                            }
                                            relation.addWay(memWay);
                                        }
                                    } else if (type.equalsIgnoreCase("relation")) {
                                        Relation memRelation = relationLongIndex.get(Long.parseLong(memRef));
                                        if (memRelation != null) {
                                            relation.addRelation(memRelation);
                                        }
                                    }
                                }
                            }
                            break;

                        case "tag":
                            String key = reader.getAttributeValue(null, "k");
                            String value = reader.getAttributeValue(null, "v");

                            if (way != null || relation != null || key.contains("addr:")) {
                                switch (key) {
                                    case "addr:city":
                                        osmAddress = new OsmAddress(node);
                                        osmAddress.setCity(value.intern());
                                        break;
                                    case "addr:housenumber":
                                        osmAddress.setHouseNumber(value.intern());
                                        break;
                                    case "addr:postcode":
                                        osmAddress.setPostcode(Integer.parseInt(value));
                                        break;
                                    case "addr:street":
                                        osmAddress.setStreet(value.intern());
                                        break;
                                    case "building":
                                        elementType = ElementType.BUILDING;
                                        break;
                                    case "bridge":
                                        if (value.equals("yes")) {
                                            elementType = ElementType.TERTIARY;
                                        }
                                        break;
                                    case "highway":
                                        switch (value) {
                                            case "motorway":
                                            case "motorway_link":
                                                elementType = ElementType.MOTORWAY;
                                                break;
                                            case "primary":
                                                elementType = ElementType.PRIMARY;
                                                break;
                                            case "residential":
                                                elementType = ElementType.RESIDENTIAL;
                                                break;
                                            case "footway":
                                            case "footpath":
                                            case "path":
                                                elementType = ElementType.FOOTWAY;
                                                break;
                                            case "pedestrian":
                                                elementType = ElementType.PEDESTRIAN;
                                                break;
                                            case "cycleway":
                                            case "track":
                                                elementType = ElementType.CYCLEWAY;
                                                break;
                                            case "road":
                                            case "service":
                                                elementType = ElementType.ROAD;
                                                break;
                                            case "trunk":
                                                elementType = ElementType.TRUNK;
                                                break;
                                            case "tertiary":
                                            case "secondary":
                                            case "unclassified":
                                                elementType = ElementType.TERTIARY;
                                                break;
                                        }
                                        break;
                                    case "railway":
                                        if (value.equals("rail")) {
                                            elementType = ElementType.RAILWAY;
                                        }
                                        break;
                                    case "aeroway":
                                        switch (value) {
                                            case "taxiway":
                                            case "runway":
                                                elementType = ElementType.AEROWAY;
                                                break;
                                        }
                                        break;
                                    case "landuse":
                                        if (value.equals("grass") ||
                                                value.equals("meadow") ||
                                                value.equals("orchard") ||
                                                value.equals("allotments")) {
                                            elementType = ElementType.LANDUSE;

                                        } else if (value.equals("forest")) {
                                            elementType = ElementType.FOREST;
                                        }
                                        break;
                                    case "leisure":
                                        if (value.equals("park")) {
                                            elementType = ElementType.LANDUSE;
                                        }
                                        break;
                                    case "maxspeed":
                                        if (way != null) {
                                            if (way.getType() != null) {
                                                if (way.getType().canDrive()) {
                                                    try {
                                                        way.setMaxSpeed(Integer.parseInt(value));
                                                    } catch (NumberFormatException ignored) {
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    case "natural":
                                        switch (value) {
                                            case "coastline":
                                                isCoastline = true;
                                                break;
                                            case "water":
                                                elementType = ElementType.WATER;
                                                break;
                                            case "wood":
                                                elementType = ElementType.FOREST;
                                                break;
                                        }
                                        break;
                                    case "oneway":
                                        if (value.equals("yes")) {
                                            if (way != null) {
                                                way.setOneWay(true);
                                            }
                                        }
                                        break;
                                    case "junction":
                                        if (value.equals("roundabout")) {
                                            if (way != null) {
                                                way.setOneWay(true);
                                            }
                                        }
                                        break;
                                    case "oneway:bicycle":
                                        if (value.equals("yes")) {
                                            if (way != null) {
                                                way.setOneWayBike(true);
                                            }
                                        }
                                        break;
                                    case "waterway":
                                        elementType = ElementType.WATERWAY;
                                        break;
                                    case "type":
                                        if (relation != null) {
                                            if (value.equals("multipolygon")) {
                                                relation.setMultipolygon(true);
                                            }
                                        }
                                        break;
                                }
                            }
                            break;

                        case "nd":
                            long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            way.addNode(nodeLongIndex.get(ref).getNode().getCoords());
                            break;
                    }
                    break;

                case END_ELEMENT:
                    switch (reader.getLocalName()) {

                        case "node":
                            if (osmAddress != null && osmAddress.isValid()) {
                                //triesMap.addAddress(osmAddress);
                                addressTries.put(osmAddress.toString().intern(), osmAddress.getNodeCoords());
                            }
                            break;

                        case "relation":
                            if (displayOptions.getBool(DisplayOption.LOAD_RELATIONS)) {
                                if (elementType != null) {
                                    relation.setType(elementType);
                                    elementType = null;
                                }
                                relationLongIndex.put(relation);
                                relation = null;
                            }
                            break;

                        case "way":
                            wayLongIndex.put(way);
                            if (isCoastline) {
                                coastlines.add(way);

                            } else {
                                if (elementType != null) {
                                    way.setType(elementType);
                                    elementType = null;
                                }
                                way = null;
                            }
                            break;
                    }
                    break;
            }
        }
        time += System.nanoTime();
        System.out.println("Parsed OSM data in: "+ time / 1_000_000+"ms");
        islands = mergeCoastLines(coastlines);

        return new MapData(
                islands,
                wayLongIndex,
                relationLongIndex,
                null,
                null,
                null,
                addressTries,
                minX,
                maxX,
                minY,
                maxY
        );
    }

    private List<Way> mergeCoastLines(List<Way> coastlines) {
        Map<Node, Way> pieces = new HashMap<>();

        for (Way coast : coastlines) {
            Way before = pieces.remove(coast.first());
            Way after = pieces.remove(coast.last());
            if (before == after) {
                after = null;
            }
            Way merged = Way.merge(before, coast, after);
            pieces.put(merged.first(), merged);
            pieces.put(merged.last(), merged);
        }
        List<Way> merged = new ArrayList<>();
        pieces.forEach((node, way) -> {
            if (way.last().equals(node)) {
                merged.add(way);
            }
        });
        return merged;
    }
}
