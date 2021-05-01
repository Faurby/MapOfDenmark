package bfst21.models;

import bfst21.data.BinaryFileManager;
import bfst21.data.XmlParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;


public class Model {

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

    public void load(boolean loadDefaultFile) throws XMLStreamException, IOException, ClassNotFoundException {
        if (loadDefaultFile) {
            load(defaultFileName);
        } else {
            load(fileName);
        }
    }

    public void load(String fileName) throws IOException, XMLStreamException, FactoryConfigurationError, ClassNotFoundException {
        this.fileName = fileName;

        //TODO: Check if file at fileName is actually present
        // Present an error to the user if it doesn't exist.
        // No need for a NullPointerException in the console.

        System.out.println("Model loading file: " + fileName);
        long totalTime = -System.nanoTime();

        if (fileName.endsWith(".osm")) {
            XmlParser xmlParser = new XmlParser();
            mapData = xmlParser.loadOSM(fileName);

        } else if (fileName.endsWith(".zip")) {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(fileName));
            zip.getNextEntry();
            XmlParser xmlParser = new XmlParser();
            mapData = xmlParser.loadOSM(zip);

        } else if (fileName.endsWith(".obj")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            mapData = binaryFileManager.loadOBJ(fileName, jarFile);
        }
        totalTime += System.nanoTime();
        System.out.println("Total load time: " + totalTime / 1_000_000 + "ms");
    }

    public MapData getMapData() {
        return mapData;
    }

    public String getFileName() {
        return fileName;
    }
}
