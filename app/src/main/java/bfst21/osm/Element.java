package bfst21.osm;

import java.io.Serializable;


public abstract class Element implements Serializable {

    private static final long serialVersionUID = -2234832342114559254L;
    protected transient long id; //opridentligt var denne final

    public Element(long id) {
        this.id = id;
    }

    public Element(){};

    public long getID() {
        return id;
    }
}
