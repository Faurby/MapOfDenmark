package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class WayLongIndex implements Serializable {

    private static final long serialVersionUID = 2352785882692399165L;
    private final List<Way> elements = new ArrayList<>();
    private boolean sorted = true;

    public HashMap<ElementGroup, List<Way>> getElementMap() {
        HashMap<ElementGroup, List<Way>> elementMap = new HashMap<>();

        for (ElementGroup elementGroup : ElementGroup.values()) {
            elementMap.put(elementGroup, new ArrayList<>());
        }
        for (Way element : elements) {

            ElementType type = element.getType();
            if (type != null) {

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    if (elementGroup.getType() == type) {

                        ElementSize size = element.getElementSize();
                        if (elementGroup.getSize() == size) {

                            List<Way> elementList = elementMap.get(elementGroup);
                            elementList.add(element);
                            elementMap.put(elementGroup, elementList);
                        }
                    }
                }
            }
        }
        return elementMap;
    }

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
