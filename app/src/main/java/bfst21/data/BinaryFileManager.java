package bfst21.data;

import bfst21.address.TST;
import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.tree.KdTree;
import bfst21.models.MapData;

import java.io.*;
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
                (List<Way>) input.readObject(),
                (ElementLongIndex<Way>) input.readObject(),
                (ElementLongIndex<Relation>) input.readObject(),
                (HashMap<ElementGroup, KdTree<Way>>) input.readObject(),
                (KdTree<Relation>) input.readObject(),
                (DirectedGraph) input.readObject(),
                (TST<List<OsmAddress>>) input.readObject(),
                (List<UserNode>) input.readObject(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat(),
                input.readFloat()
        );
    }

    public void saveOBJ(String path, String fileName, MapData mapData) throws IOException {
        //TODO: Måske vi skal ændre i saveOBJ() metoden så man ikke skal give en tom String med
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path + fileName)))) {

            output.writeObject(mapData.getWays(ElementGroup.getElementGroup(ElementType.ISLAND, ElementSize.DEFAULT)));
            output.writeObject(null);
            output.writeObject(null);

            output.writeObject(mapData.getKdTreeMap());
            output.writeObject(mapData.getKdTreeRelations());

            output.writeObject(mapData.getDirectedGraph());

            output.writeObject(mapData.getAddressTries());

            output.writeObject(mapData.getUserNodes());

            output.writeFloat(mapData.getMinX());
            output.writeFloat(mapData.getMaxX());
            output.writeFloat(mapData.getMinY());
            output.writeFloat(mapData.getMaxY());
        }
    }
}
