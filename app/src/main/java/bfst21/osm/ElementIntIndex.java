package bfst21.osm;

import bfst21.pathfinding.Vertex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ElementIntIndex<T extends Vertex> implements Serializable {

    private static final long serialVersionUID = 2352785882692399165L;
    private final List<T> elements = new ArrayList<>();
    private boolean sorted = true;

    public List<T> getElements() {
        return elements;
    }

    public void put(T element) {
        elements.add(element);
        sorted = false;
    }

    public T get(int ref) {
        if (!sorted) {
            elements.sort(Comparator.comparingInt(T::getID));
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
        T element = elements.get(lo);
        return element.getID() == ref ? element : null;
    }
}
