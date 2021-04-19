package bfst21.data;

import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.KdTree;
import bfst21.models.MapData;

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
                (ElementLongIndex<Relation>) input.readObject(),
                (HashMap<ElementGroup, KdTree<Way>>) input.readObject(),
                (KdTree<Relation>) input.readObject(),
                (DirectedGraph) input.readObject(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat()
        );
    }

    public void saveOBJ(String fileName, MapData mapData) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {

            output.writeObject(mapData.getWays(ElementGroup.getElementGroup(ElementType.ISLAND, ElementSize.DEFAULT)));
            output.writeObject(null);
            output.writeObject(null);

            output.writeObject(mapData.getKdTreeMap());
            output.writeObject(mapData.getKdTreeRelations());

            //TODO: For some reason, it fails to save the directed graph
            // Set to null for now...
            //output.writeObject(null);
            output.writeObject(mapData.getDirectedGraph());

            output.writeFloat(mapData.getMinX());
            output.writeFloat(mapData.getMaxX());
            output.writeFloat(mapData.getMinY());
            output.writeFloat(mapData.getMaxY());
        }
    }
}
