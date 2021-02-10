package bfst21.addressparser;

import bfst21.models.City;

import java.util.List;

public class Main {
    public static void main(){
        FileReader filereader = new FileReader();
        List<City> cityList = filereader.getCityList();

        for (City c : cityList){
            System.out.println(c.getName());
        }
    }
}
