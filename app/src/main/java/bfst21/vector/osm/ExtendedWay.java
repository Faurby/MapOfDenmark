package bfst21.vector.osm;

import java.util.List;


public class ExtendedWay extends Way {

    private String key;
    private String value;

    public ExtendedWay(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
