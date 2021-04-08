package bfst21.data;

import bfst21.osm.WayType;
import bfst21.tree.KdTree;
import bfst21.view.Drawable;
import bfst21.models.MapData;
import bfst21.osm.ElementLongIndex;
import bfst21.osm.Way;
import java.io.*;
import java.util.List;


public class BinaryFileManager {

    @SuppressWarnings("unchecked")
    public MapData loadOBJ(String fileName, boolean jarFile) throws IOException, ClassNotFoundException {
        ObjectInputStream input;
        if (jarFile) {
            input = new ObjectInputStream(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(fileName)));
        } else {
            input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)));
        }
        return new MapData(
            (List<Drawable>) input.readObject(),
            (List<Way>) input.readObject(),
            (List<Way>) input.readObject(),
            (ElementLongIndex) input.readObject(),
            (KdTree) input.readObject(),
            input.readFloat(),
            input.readFloat(),
            input.readFloat(),
            input.readFloat()
        );
    }

    public void saveOBJ(String fileName, MapData mapData) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
            output.writeObject(mapData.getShapes());
            output.writeObject(mapData.getWays(WayType.ISLAND));
            output.writeObject(mapData.getWays());
            output.writeObject(mapData.getIdToRelation());
            output.writeObject(mapData.getKdTree());
            output.writeFloat(mapData.getMinx());
            output.writeFloat(mapData.getMaxx());
            output.writeFloat(mapData.getMiny());
            output.writeFloat(mapData.getMaxy());
        }
    }
}
