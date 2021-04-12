package bfst21.models;

import bfst21.data.BinaryFileManager;
import bfst21.data.XmlParser;
import bfst21.view.Drawable;
import bfst21.view.Line;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;


public class Model implements Iterable<Drawable> {

    private final List<Runnable> observers = new ArrayList<>();
    private final String defaultFileName;
    private final boolean jarFile;

    private MapData mapData;
    private String fileName;

    public Model(String defaultFileName, boolean jarFile) {
        this.defaultFileName = defaultFileName;
        this.jarFile = jarFile;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void load(boolean loadDefault) throws XMLStreamException, IOException, ClassNotFoundException {
        if (loadDefault) {
            load(defaultFileName);
        } else {
            load(fileName);
        }
    }

    public void load(String fileName) throws IOException, XMLStreamException, FactoryConfigurationError, ClassNotFoundException {
        long time = -System.nanoTime();
        Options options = Options.getInstance();

        if (fileName.endsWith(".osm")) {
            XmlParser xmlParser = new XmlParser();
            mapData = xmlParser.loadOSM(fileName);

        } else if (fileName.endsWith(".zip")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            loadZIP(fileName);
            if (options.getBool(Option.SAVE_OBJ_FILE)) {
                binaryFileManager.saveOBJ(fileName.split("\\.")[0] + ".obj", mapData);
            }

        } else if (fileName.endsWith(".obj")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            mapData = binaryFileManager.loadOBJ(fileName, jarFile);
        }
        time += System.nanoTime();
        Logger.getGlobal().info(String.format("Load time: %dms", time / 1000000));
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        XmlParser xmlParser = new XmlParser();
        mapData = xmlParser.loadOSM(zip);
    }

    public void save(String filename) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(filename)) {
            for (Drawable line : mapData.getShapes()) {
                out.println(line);
            }
        }
    }

    void addObserver(Runnable observer) {
        observers.add(observer);
    }

    void notifyObservers() {
        for (Runnable observer : observers) {
            observer.run();
        }
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
