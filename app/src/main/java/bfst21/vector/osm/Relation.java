package bfst21.vector.osm;

import java.util.ArrayList;
import java.util.List;


public class Relation extends Element {

    private List<Element> members;

    public Relation(long id) {
        super(id);
        members = new ArrayList<>();
    }

    public List<Element> getMembers() {
        return members;
    }

    public void addMember(Element element) {
        members.add(element);
    }

    @Override
    public ElementType getType() {
        return ElementType.RELATION;
    }
}
