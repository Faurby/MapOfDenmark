package bfst21.data;

import bfst21.address.TST;
import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.KdTree;
import bfst21.models.MapData;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * BinaryFileManager is used to save and load .obj files.
 * All the important data from the MapData is saved to an .obj file.
 * This heavily increases startup performance as we do not need to parse any OSM data.
 * <p>
 * All objects saved in the .obj file must implement Serializable.
 */
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
            (List<MapWay>) input.readObject(),
            (List<Way>) input.readObject(),
            (List<Relation>) input.readObject(),
            (List<MapText>) input.readObject(),

            (TST) input.readObject(),

            (HashMap<ElementGroup, KdTree<MapWay>>) input.readObject(),
            (KdTree<Relation>) input.readObject(),
            (KdTree<MapText>) input.readObject(),
            (DirectedGraph) input.readObject(),
            (List<UserNode>) input.readObject(),

            input.readFloat(),
            input.readFloat(),
            input.readFloat(),
            input.readFloat()
        );
    }

    public void saveOBJ(String path, MapData mapData) throws IOException {

        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {

            output.writeObject(mapData.getWays(ElementGroup.getElementGroup(ElementType.ISLAND, ElementSize.DEFAULT)));
            output.writeObject(new ArrayList<Way>());
            output.writeObject(new ArrayList<Relation>());
            output.writeObject(new ArrayList<MapText>());

            output.writeObject(mapData.getAddressTries());

            output.writeObject(mapData.getKdTreeMap());
            output.writeObject(mapData.getKdTreeRelations());
            output.writeObject(mapData.getKdTreeMapTexts());
            output.writeObject(mapData.getDirectedGraph());
            output.writeObject(mapData.getUserNodes());

            output.writeFloat(mapData.getMinX());
            output.writeFloat(mapData.getMaxX());
            output.writeFloat(mapData.getMinY());
            output.writeFloat(mapData.getMaxY());
        }
    }
}
