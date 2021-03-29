package bfst21.vector.osm;


public abstract class Element {

    private transient int id;

    public Element(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public abstract ElementType getType();

    enum ElementType {
        NODE, WAY, RELATION
    }
}
