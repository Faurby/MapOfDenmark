package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class WayLongIndex implements Serializable {

    private static final long serialVersionUID = 2352785882692399165L;
    private final List<Way> elements = new ArrayList<>();
    private boolean sorted = true;

    public List<Way> getElements() {
        return elements;
    }

    public void put(Way element) {
        elements.add(element);
        sorted = false;
    }

    public Way get(long ref) {
        if (!sorted) {
            elements.sort(Comparator.comparingLong(Way::getID));
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
        Way element = elements.get(lo);
        return element.getID() == ref ? element : null;
    }
}
