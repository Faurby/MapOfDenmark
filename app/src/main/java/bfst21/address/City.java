package bfst21.address;


public class City {

    private final int postcode;
    private final String name;

    public City(int postcode, String name) {
        this.postcode = postcode;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPostcode() {
        return postcode;
    }
}
