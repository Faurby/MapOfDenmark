package bfst21.vector;

import java.io.*;
import java.util.List;


public class BinaryFileManager {

    @SuppressWarnings("unchecked")
    public MapData loadOBJ(String filename, boolean jarFile) throws IOException, ClassNotFoundException {
        ObjectInputStream input;
        if (jarFile) {
            input = new ObjectInputStream(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(filename)));
        } else {
            input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
        }
        return new MapData(
            (List<Drawable>) input.readObject(),
            (List<Drawable>) input.readObject(),
            (List<Drawable>) input.readObject(),
            (List<Drawable>) input.readObject(),
            input.readFloat(),
            input.readFloat(),
            input.readFloat(),
            input.readFloat()
        );
    }

    public void saveOBJ(String filename, MapData mapData) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            output.writeObject(mapData.getShapes());
            output.writeObject(mapData.getBuildings());
            output.writeObject(mapData.getIslands());
            output.writeFloat(mapData.getMinx());
            output.writeFloat(mapData.getMaxx());
            output.writeFloat(mapData.getMiny());
            output.writeFloat(mapData.getMaxy());
        }
    }
}
