package bfst21.address;


public class Postcode {

    private String city;
    private int postcode;

    public Postcode(String city, int postcode) {
        this.city = city;
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public int getPostcode() {
        return postcode;
    }
}