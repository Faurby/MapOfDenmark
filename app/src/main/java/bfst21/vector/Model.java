package bfst21.vector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;
import static javax.xml.stream.XMLStreamConstants.*;

public class Model implements Iterable<Drawable> {

    private List<Runnable> observers = new ArrayList<>();
    private MapData mapData;

    public Model(String filename, boolean jarFile) throws IOException, XMLStreamException, FactoryConfigurationError,
            ClassNotFoundException {
        load(filename, jarFile);
    }

    public void load(String filename, boolean jarFile) throws IOException, XMLStreamException, FactoryConfigurationError,
            ClassNotFoundException {
        long time = -System.nanoTime();
        if (filename.endsWith(".osm")) {
            XmlParser xmlParser = new XmlParser();
            mapData = xmlParser.loadOSM(filename);
        } else if (filename.endsWith(".zip")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            loadZIP(filename);
            binaryFileManager.saveOBJ(filename + ".obj", mapData);
        } else if (filename.endsWith(".obj")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            mapData = binaryFileManager.loadOBJ(filename, jarFile);
        }
        time += System.nanoTime();
        Logger.getGlobal().info(String.format("Load time: %dms", time / 1000000));
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        XmlParser xmlParser = new XmlParser();
        mapData = xmlParser.loadOSM(zip);
    }

    public void save(String filename) throws FileNotFoundException {
        try (var out = new PrintStream(filename)) {
            for (var line : mapData.getShapes())
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
        return mapData.getShapes().iterator();
    }

    public void add(Line line) {
        mapData.getShapes().add(line);
        notifyObservers();
    }

    public MapData getMapData() {
        return mapData;
    }
}
