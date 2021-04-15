package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class RelationLongIndex implements Serializable {

    private static final long serialVersionUID = -4363131697594737474L;
    private final List<Relation> elements = new ArrayList<>();
    private boolean sorted = true;

    public List<Relation> getElements() {
        return elements;
    }

    public void put(Relation element) {
        elements.add(element);
        sorted = false;
    }

    public Relation get(long ref) {
        if (!sorted) {
            elements.sort(Comparator.comparingLong(Relation::getID));
            sorted = true;
        }
        int lo = 0;               // nodes.get(lo).getID() <= ref
        int hi = elements.size(); // nodes.get(hi).getID() > ref
        while (lo + 1 < hi) {
            int mi = (lo + hi) / 2;
            if (elements.get(mi).getID() <= ref) {
                lo = mi;
            } else {
                hi = mi;
            }
        }
        Relation element = elements.get(lo);
        return element.getID() == ref ? element : null;
    }
}
