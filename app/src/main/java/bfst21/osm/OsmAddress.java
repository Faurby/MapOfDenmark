package bfst21.osm;


public class OsmAddress {

    private String city, houseNumber, name, postcode, street;

    public String getCity() {
        return city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
