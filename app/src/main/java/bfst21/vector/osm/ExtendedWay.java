package bfst21.vector.osm;

import java.util.HashMap;
import java.util.List;


public class ExtendedWay extends Way {

    private HashMap<String, String> tags;

    public ExtendedWay(long id) {
        super(id);
        this.tags = new HashMap<>();
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getValue(String key) {
        return tags.get(key);
    }

    public HashMap<String, String> getTags() {
        return tags;
    }
}
