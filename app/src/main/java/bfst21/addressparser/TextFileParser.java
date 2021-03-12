package bfst21.addressparser;

import bfst21.models.City;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TextFileParser {

    private List<City> cityList;

    public TextFileParser() {
        readFile();
    }

    public void readFile() {
        List<City> cityList = new ArrayList<>();
        String path = "CityPostcodePair.txt";
        Scanner sc = new Scanner(getClass().getClassLoader().getResourceAsStream(path), "UTF-8");

        while (sc.hasNext()) {
            String str = sc.nextLine();
            int postcode = Integer.parseInt(str.substring(0, 4));
            String cityName = str.substring(5);
            City city = new City(postcode, cityName);
            cityList.add(city);
        }
        this.cityList = cityList;
    }

    public List<City> getCityList() {
        return cityList;
    }
}
