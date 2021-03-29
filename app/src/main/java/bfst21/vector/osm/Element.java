package bfst21.vector.osm;


public abstract class Element {

    private final transient long id;

    public Element(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public abstract ElementType getType();

    enum ElementType {
        NODE, WAY, RELATION
    }
}
