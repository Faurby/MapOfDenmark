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

        System.out.println("Model loading file: "+fileName);
        long totalTime = -System.nanoTime();
        DisplayOptions displayOptions = DisplayOptions.getInstance();

        if (fileName.endsWith(".osm")) {
            XmlParser xmlParser = new XmlParser();
            mapData = xmlParser.loadOSM(fileName);

        } else if (fileName.endsWith(".zip")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            loadZIP(fileName);
            if (displayOptions.getBool(DisplayOption.SAVE_OBJ_FILE)) {
                long time = -System.nanoTime();
                binaryFileManager.saveOBJ(fileName.split("\\.")[0] + ".obj", mapData);
                time += System.nanoTime();
                System.out.println("Saved .obj file in: "+time / 1_000_000+"ms");
            }

        } else if (fileName.endsWith(".obj")) {
            BinaryFileManager binaryFileManager = new BinaryFileManager();
            mapData = binaryFileManager.loadOBJ(fileName, jarFile);
        }
        totalTime += System.nanoTime();
        System.out.println("Total load time: "+totalTime / 1_000_000+"ms");
    }

    private void loadZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
        zip.getNextEntry();
        XmlParser xmlParser = new XmlParser();
        mapData = xmlParser.loadOSM(zip);
    }

    public MapData getMapData() {
        return mapData;
    }

    public String getFileName() {
        return fileName;
    }
}
