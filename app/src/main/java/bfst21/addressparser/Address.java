package bfst21.addressparser;

import java.util.regex.*;

public class Address {
  public final String street, house, floor, side, postcode, city;

  private Address(
      String _street, String _house, String _floor, String _side, 
      String _postcode, String _city) {
    street = _street;
    house = _house;
    floor = _floor;
    side = _side;
    postcode = _postcode;
    city = _city;
  }

  public String toString() {
    return street + " " + house + ", " + floor + " " + side + "\n"
      + postcode + " " + city;
  }

  static String regex = "(?<street>.*?) +(?<house>\\d{1,3}[A-z]?)? *?\\,? *((?<floor>\\d{1,2}\\.?|st\\.?)\\,? *)? *?((?<side>[A-z]{1,2}\\.? *?)\\,? ?)?" +
          " *?((?<postcode>\\d{4}) *)? *?(?<city>([A-z]*|[^A-z\\d\\,])*)?$";
  static Pattern pattern = Pattern.compile(regex);

  public static Address parse(String input) {
    Matcher matcher = pattern.matcher(input);
    if (matcher.matches()) {
      return new Builder().street(matcher.group("street"))
              .house(matcher.group("house"))
              .floor(matcher.group("floor"))
              .side(matcher.group("side"))
              .postcode(matcher.group("postcode"))
              .city(matcher.group("city")).build();
    } else {
      return new Builder().build();
    }
  }

  public static class Builder {
    private String street, house, floor, side, postcode, city;

    public Builder street(String _street) {
      street = _street;
      return this;
    }

    public Builder house(String _house) {
      house = _house;
      return this;
    }

    public Builder floor(String _floor) {
      floor = _floor;
      return this;
    }

    public Builder side(String _side) {
      side = _side;
      return this;
    }

    public Builder postcode(String _postcode) {
      postcode = _postcode;
      return this;
    }

    public Builder city(String _city) {
      city = _city;
      return this;
    }

    public Address build() {
      return new Address(street, house, floor, side, postcode, city);
    }
  }
}
