package bfst21.osm;


public class OsmAddress {

    private String city, postcode, houseNumber, street;

    private final Node node;

    public OsmAddress(Node node) {
        this.node = node;
    }

    public float[] getNodeCoords() {
        return new float[]{node.getX(), node.getY()};
    }

    public Node getNode() {
        return node;
    }

    public String getCity() {
        return city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getStreet() {
        return street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean isValid() {
        if (street == null) {
            return false;
        }
        if (houseNumber == null) {
            return false;
        }
        if (city == null) {
            return false;
        }
        if (postcode == null) {
            return false;
        }
        return node != null;
    }

    public String toString() {
        return street + " " + houseNumber + " " + city + " " + postcode;
    }
}
