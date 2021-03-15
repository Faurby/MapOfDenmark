package bfst21.vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bfst21.vector.osm.Node;

public class LongIndex {
    List<Node> nodes = new ArrayList<>();
    boolean sorted = true;

    public void put(Node node) {
        nodes.add(node);
        sorted = false;
    }

    public Node get(long ref) {
        if (!sorted) {
            nodes.sort(Comparator.comparingLong(Node::getID));
            sorted = true;
        }
        int lo = 0;            // nodes.get(lo).getID() <= ref
        int hi = nodes.size(); // nodes.get(hi).getID() > ref
        while (lo + 1 < hi) {
            int mi = (lo + hi) / 2;
            if (nodes.get(mi).getID() <= ref) {
                lo = mi;
            } else {
                hi = mi;
            }
        }
        Node node = nodes.get(lo);
        return node.getID() == ref ? node : null;
    }
}
