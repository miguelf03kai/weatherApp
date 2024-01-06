package com.miguelf03kai.weatherapp.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connection {

    // Check if the device have internet connection
    public static boolean checkConnection(ConnectivityManager connectivityManager){
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);

        return connected;
    }
}