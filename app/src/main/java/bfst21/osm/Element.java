package bfst21.osm;


/**
 * Helper class used to parse elements from OSM data.
 * <p>
 * The ID is used to retrive Elements from ElementLongIndex
 * We no longer need the ID when parsing is complete.
 */
public class Element<T> {

    private transient final long id;
    private final T innerElement;

    public Element(long id, T innerElement) {
        this.id = id;
        this.innerElement = innerElement;
    }

    public T getInnerElement() {
        return innerElement;
    }

    public long getID() {
        return id;
    }
}