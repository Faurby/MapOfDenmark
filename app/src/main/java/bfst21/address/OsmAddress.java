package bfst21.address;

import bfst21.osm.Node;

import java.io.Serializable;


/**
 * OsmAddress is an address created by parsing the given OSM data.
 * Every OsmAddress has a x and y value which determines the location of the address.
 * <p>
 * OsmAddress is placed as a node in a ternary search trie
 * which can be used to give relevant address suggestions.
 */
public class OsmAddress implements Serializable {

    private static final long serialVersionUID = -377246689553287254L;

    private String city, postcode, houseNumber, street;
    private final float x, y;

    public OsmAddress(Node node) {
        this.x = node.getX();
        this.y = node.getY();
    }

    public float[] getNodeCoords() {
        return new float[]{x, y};
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
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
        return postcode != null;
    }

    public String toString() {
        return street + " " + houseNumber + " " + city + " " + postcode;
    }

    public String omitHouseNumberToString() {
        return street + " " + city + " " + postcode;
    }
}
