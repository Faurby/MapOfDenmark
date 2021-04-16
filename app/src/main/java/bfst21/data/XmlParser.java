package bfst21.data;

import bfst21.models.Option;
import bfst21.models.Options;
import bfst21.osm.*;
import bfst21.view.Drawable;
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


public class XmlParser {

    public MapData loadOSM(String fileName) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        return loadOSM(new FileInputStream(fileName));
    }

    public MapData loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError {

        Options options = Options.getInstance();

        InputFactoryProviderImpl iprovider = new InputFactoryProviderImpl();

        XMLInputFactory2 xmlif = iprovider.createInputFactory();
        xmlif.configureForSpeed();

        XMLStreamReader reader = xmlif.createXMLStreamReader(new BufferedInputStream(input));

        Way way = null;
        Relation relation = null;
        OsmAddress osmAddress = new OsmAddress();
        WayType wayType = null;

        NodeLongIndex idToNode = new NodeLongIndex();
        WayLongIndex idToWay = new WayLongIndex();
        RelationLongIndex idToRelation = new RelationLongIndex();

        List<Drawable> shapes = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
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

                            idToNode.put(new NodeID(nodeID, new Node(lon, lat)));
                            break;

                        case "way":
                            long wayID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            way = new Way(wayID);
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
                                            String role = reader.getAttributeValue(null, "role");
                                            if (role != null && !role.isEmpty()) {
                                                memWay.setRole(role);
                                            }
                                            relation.addMember(memWay);
                                        }
                                    } else if (type.equalsIgnoreCase("relation")) {
                                        Relation memRelation = idToRelation.get(Long.parseLong(memRef));
                                        if (memRelation != null) {
                                            relation.addMember(memRelation);
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
                                        osmAddress = new OsmAddress();
                                        osmAddress.setCity(value);
                                        break;
                                    case "addr:housenumber":
                                        osmAddress.setHouseNumber(value);
                                        break;
                                    case "addr:name":
                                        osmAddress.setName(value);
                                        break;
                                    case "addr:postcode":
                                        osmAddress.setPostcode(value);
                                        break;
                                    case "addr:street":
                                        osmAddress.setStreet(value);
                                        break;
                                    case "building":
                                        wayType = WayType.BUILDING;
                                        break;
                                    case "highway":
                                        switch (value) {
                                            case "motorway":
                                            case "motorway_link":
                                                wayType = WayType.MOTORWAY;
                                                break;
                                            case "primary":
                                                wayType = WayType.PRIMARY;
                                                break;
                                            case "residential":
                                                wayType = WayType.RESIDENTIAL;
                                                break;
                                            case "footway":
                                            case "footpath":
                                            case "path":
                                            case "pedestrian":
                                                wayType = WayType.FOOTWAY;
                                                break;
                                            case "cycleway":
                                            case "track":
                                                wayType = WayType.CYCLEWAY;
                                                break;
                                            case "road":
                                            case "service":
                                                wayType = WayType.ROAD;
                                                break;
                                            case "trunk":
                                                wayType = WayType.TRUNK;
                                                break;
                                            case "tertiary":
                                            case "secondary":
                                                wayType = WayType.TERTIARY;
                                                break;
                                        }
                                        break;
                                    case "landuse":
                                        if (value.equals("grass") ||
                                            value.equals("meadow") ||
                                            value.equals("orchard") ||
                                            value.equals("allotments")) {
                                            wayType = WayType.LANDUSE;
                                        }
                                        break;
                                    case "leisure":
                                        if (value.equals("park")) {
                                            wayType = WayType.LANDUSE;
                                        }
                                        break;
                                    case "maxspeed":
                                        if (way != null) {
                                            if (way.getType() != null) {
                                                if (way.canDrive()) {
                                                    try {
                                                        way.setMaxSpeed(Integer.parseInt(value));
                                                    } catch (NumberFormatException ex) {
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
                                                wayType = WayType.WATER;
                                                break;
                                        }
                                        break;
                                    case "waterway":
                                        wayType = WayType.WATERWAY;
                                        break;
                                }
                            } else if (relation != null) {
                                if (key.equals("type")) {
                                    if (value.equals("multipolygon")) {
                                        relation.setMultipolygon(true);
                                    }
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

                        case "node":
                            if (osmAddress.getCity() != null) {
//                                System.out.println("Found: "+osmAddress.getCity());
                            }
                            break;

                        case "relation":
                            if (options.getBool(Option.LOAD_RELATIONS)) {
                                if (wayType != null) {
                                    relation.setType(wayType);
                                    wayType = null;
                                }
                                relations.add(relation);
                                idToRelation.put(relation);
                                relation = null;
                            }
                            break;

                        case "way":
                            idToWay.put(way);
                            if (isCoastline) {
                                coastlines.add(way);

                            } else {
                                if (wayType != null) {
                                    way.setType(wayType);
                                    wayType = null;
                                }
                                ways.add(way);
                                way = null;
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
                relations,
                null,
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
