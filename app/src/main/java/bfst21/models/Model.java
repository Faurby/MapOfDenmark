package bfst21.models;

import bfst21.data.BinaryFileManager;
import bfst21.data.XmlParser;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;


public class Model {

    private final String defaultFileName;
    private final boolean jarFile;
    boolean errorLoading;

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

        System.out.println("Model loading file: " + fileName);
        long totalTime = -System.nanoTime();

        File file = new File(fileName);
        boolean loadDefaultFile = fileName.equalsIgnoreCase(defaultFileName);

        //Check if file exists unless we are loading the default file within a jar file.
        //The file check does not work properly for files within a jar file.
        if (file.exists() || (loadDefaultFile && jarFile)) {
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
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("File doesn't exist!");
                alert.setContentText("It seems the default file doesn't exist");
                alert.showAndWait();
            });
        }
        totalTime += System.nanoTime();
        System.out.println("Total load time: " + totalTime / 1_000_000L + "ms");
    }

    public boolean isErrorLoading() {
        return errorLoading;
    }

    public MapData getMapData() {
        return mapData;
    }

    public String getFileName() {
        return fileName;
    }
}
