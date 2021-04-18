package bfst21.data;

import bfst21.osm.*;
import bfst21.tree.KdTree;
import bfst21.models.MapData;
import com.github.davidmoten.rtree2.RTree;

import java.io.*;
import java.util.HashMap;
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
                (List<Way>) input.readObject(),
                (ElementLongIndex<Way>) input.readObject(),
                (List<Relation>) input.readObject(),
                (HashMap<ElementGroup, KdTree<Way>>) input.readObject(),
                (HashMap<ElementGroup, RTree<Integer, Way>>) input.readObject(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat()
        );
    }

    public void saveOBJ(String fileName, MapData mapData) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
            output.writeObject(mapData.getWays(ElementGroup.getElementGroup(ElementType.ISLAND, ElementSize.DEFAULT)));
            output.writeObject(mapData.getWayLongIndex());
            output.writeObject(mapData.getRelations());
            output.writeObject(mapData.getKdTreeMap());
            output.writeObject(mapData.getRTreeMap());
            output.writeFloat(mapData.getMinX());
            output.writeFloat(mapData.getMaxX());
            output.writeFloat(mapData.getMinY());
            output.writeFloat(mapData.getMaxY());
        }
    }
}
