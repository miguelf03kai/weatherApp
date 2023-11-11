package com.miguelf03kai.weatherapp.utils;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class Localization {

    public String getCityName(double longitude, double latitude, Geocoder geocoder){
        String cityName = "Not found";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,10);

            for(Address adr: addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("TAG", "City not Found");
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return cityName;
    }
}