package com.miguelf03kai.weatherapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {

    // Return location time formatted
    public String dateTimeFormatting(String time){
        String pattern = "yyyy-MM-dd HH:mm";
        String formatedDate = "";
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        try {
            date = simpleDateFormat.parse(time);
            SimpleDateFormat formatting = new SimpleDateFormat("E dd MMMM hh:mm a");
            formatedDate = formatting.format(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return formatedDate;
    }
}