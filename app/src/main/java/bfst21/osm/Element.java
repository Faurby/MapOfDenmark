package bfst21.osm;

import java.io.Serializable;


public abstract class Element implements Serializable {

    private static final long serialVersionUID = -2234832342114559254L;
    protected final transient long id;

    public Element(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }
}
