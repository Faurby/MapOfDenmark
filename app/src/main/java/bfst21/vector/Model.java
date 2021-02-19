package bfst21.vector;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;

import static javax.xml.stream.XMLStreamConstants.*;

public class Model implements Iterable<Drawable> {
    List<Drawable> shapes;
    List<Drawable> buildings = new ArrayList<>();
    List<Runnable> observers = new ArrayList<>();
    float minx,miny,maxx,maxy;

    public Model(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        load(filename);
    }

    public void load(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        long time = -System.nanoTime();
        if (filename.endsWith(".txt")) {
            shapes = Files.lines(Path.of(filename)).map(Line::new).collect(Collectors.toList());
        } else if (filename.endsWith(".osm")) {
            loadOSM(filename);
        }
        time += System.nanoTime();
        Logger.getGlobal().info(String.format("Load time: %dms", time / 1000000));
    }

    private void loadOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        XMLStreamReader reader = XMLInputFactory
            .newInstance()
            .createXMLStreamReader(new BufferedInputStream(new FileInputStream(filename)));
        var idToNode = new HashMap<Long,Node>();
        Way way = null;
        shapes = new ArrayList<>();
        boolean iscoastline = false;
        boolean isbuilding = false;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minx = Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            maxx = Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxy = Float.parseFloat(reader.getAttributeValue(null, "minlat"))/-0.56f;
                            miny = Float.parseFloat(reader.getAttributeValue(null, "maxlat"))/-0.56f;
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            idToNode.put(id, new Node(lat, lon));
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
                            if (iscoastline) shapes.add(way);
                            if (isbuilding) buildings.add(way);
                            break;  
                    }
                    break;
            }
        }
    }

    public void save(String filename) throws FileNotFoundException {
        try (var out = new PrintStream(filename)) {
            for (var line : shapes)
                out.println(line);
        }
    }

    void addObserver(Runnable observer) {
        observers.add(observer);
    }

    void notifyObservers() {
        for (var observer : observers) observer.run();
    }

    @Override
    public Iterator<Drawable> iterator() {
        return shapes.iterator();
    }

	public void add(Line line) {
        shapes.add(line);
        notifyObservers();
	}
}
