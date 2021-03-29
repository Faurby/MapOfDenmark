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

        long maxNodeID = 8_492_965_134L;
        long minNodeID = 115_722L;

        long maxWayID = 921_067_438L;
        long minWayID = 2_080L;

        float minx = 0, miny = 0, maxx = 0, maxy = 0;
        boolean isCoastline = false, isBuilding = false;

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
                            long preNodeID = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            //int nodeID = Long.hashCode(preNodeID);
                            int nodeID = toNewRange(preNodeID, maxNodeID, minNodeID);

                            idToNode.put(new Node(nodeID, lat, lon));
                            break;

                        case "way":
                            int wayID = Integer.parseInt(reader.getAttributeValue(null, "id"));
                            way = new Way(wayID);
                            extendedWay = new ExtendedWay(wayID);
                            isCoastline = false;
                            isBuilding = false;
                            break;

//                        case "relation":
//                            long relationID = Long.parseLong(reader.getAttributeValue(null, "id"));
//                            relation = new Relation(relationID);
//                            break;
//
//                        case "member":
//                            String type = reader.getAttributeValue(null, "type");
//                            String memRef = reader.getAttributeValue(null, "ref");
//                            if (type != null) {
//                                if (type.equalsIgnoreCase("node")) {
//                                    Node memNode = (Node) idToNode.get(Long.parseLong(memRef));
//                                    if (memNode != null) {
//                                        relation.addMember(memNode);
//                                    }
//                                } else if (type.equalsIgnoreCase("way")) {
//                                    Way memWay = (Way) idToWay.get(Long.parseLong(memRef));
//                                    if (memWay != null) {
//                                        relation.addMember(memWay);
//                                    }
//                                } else if (type.equalsIgnoreCase("relation")) {
//                                    Relation memRelation = (Relation) idToRelation.get(Long.parseLong(memRef));
//                                    if (memRelation != null) {
//                                        relation.addMember(memRelation.getID());
//                                    }
//                                }
//                            }
//                            break;

                        case "tag":
                            String key = reader.getAttributeValue(null, "k");
                            String value = reader.getAttributeValue(null, "v");
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
                            long preRef = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            //int ref = Long.hashCode(preRef);
                            int ref = toNewRange(preRef, maxNodeID, minNodeID);

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

    //private List<Integer> numbers = new ArrayList<>();
    private HashMap<Integer, List<Long>> map = new HashMap<>();

    public int toNewRange(long oldValue, long oldMax, long oldMin) {
        int newMax = Integer.MAX_VALUE;
        int newMin = 1;

        long oldRange = (oldMax - oldMin);
        long newRange = (newMax - newMin);

        int toReturn;
        if (oldValue < Integer.MAX_VALUE) {
            toReturn = (int) oldValue;
        } else {
            toReturn = (int) (((oldValue - oldMin) * (newRange)) / (oldRange) + newMin) * -1;
            //System.out.println("Converted "+oldValue+" to "+toReturn);
        }
        //int newMax = Integer.MAX_VALUE;
        //int newMin = Integer.MIN_VALUE;

        //Try negative values in the range too?

        //long oldRange = (oldMax - oldMin);
        //long newRange = (newMax - newMin);

        //System.out.println("--------------------------------");
        //System.out.println("Old range: " + oldRange);
        //System.out.println("New range: " + newRange);

        //int toReturn = Long.hashCode(oldValue);

        //Converted 20930776

        //int toReturn = (int) (((oldValue - oldMin) * (newRange)) / (oldRange) + newMin);

        //long oldRange = (oldMax - oldMin);
        //long newRange = (newMax - newMin);
        //long newValue = (((oldValue - oldMin) * newRange) / oldRange) + newMin;
        //Converted 4707529748 to -981725805

        //oldRange = 8_492_849_412L;
        //newRange = 2_147_483_646;

        //4707529748 - 115722
        //4.707.414.026
        //4.707.414.026 *

        //int toReturn = (int) newValue;

        //System.out.println("Converted "+oldValue+" to "+(int)newValue);

//        List<Long> list = new ArrayList<>();
//        if (map.containsKey(toReturn)) {
//            list = map.get(toReturn);
//        }
//        if (!list.contains(oldValue)) {
//            list.add(oldValue);
//        }
//        if (list.size() > 1) {
//            String valStr = "";
//            for (long val : list) {
//                valStr += val + " ";
//            }
//            System.out.println("Same toReturn: "+toReturn+ " old values: "+valStr);
//        }
//        map.put(toReturn, list);

        return toReturn;
    }
}
