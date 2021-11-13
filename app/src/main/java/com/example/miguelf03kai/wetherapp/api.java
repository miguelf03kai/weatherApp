package com.example.miguelf03kai.wetherapp;

public class api {

    public String checkWaether(String city){

        String url = "http://api.weatherapi.com/v1/forecast.json?key=e04229e6c12c4b9dadf112621213110&q="+city+"&days=1&aqi=no&alerts=no";

        return url;
    }
}