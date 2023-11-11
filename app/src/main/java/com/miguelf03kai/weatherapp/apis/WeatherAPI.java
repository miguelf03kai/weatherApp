package com.miguelf03kai.weatherapp.apis;

public class WeatherAPI {

    private String key = "";

    public String requestUrl(String city){
        String url = "http://api.weatherapi.com/v1/forecast.json?key="+key+"&q="+city+
                "&days=1&aqi=no&alerts=no&lang=";
        return url;
    }
}