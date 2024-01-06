package com.miguelf03kai.weatherapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.miguelf03kai.weatherapp.cards.CardView;
import com.miguelf03kai.weatherapp.utils.Connection;
import com.miguelf03kai.weatherapp.utils.Formatter;
import com.miguelf03kai.weatherapp.apis.WeatherAPI;
import com.miguelf03kai.weatherapp.utils.Localization;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ProgressBar loaderProgressBar;
    private ImageView conditionIconImageView, backgroundImageView;
    private TextView degreeTextView, conditionTextView, cityTextView, regionTextView, countryTextView, localTimeTextView;
    private SearchView searchView;
    private RelativeLayout contentRelativeLayout, noConnectionRelativeLayout;

    private Button btnTryAgain;
    ConnectivityManager connectivityManager;
    boolean connected;

    Handler handler;

    ArrayList<String> time, temp, speed, image;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapterRecycleView;
    RecyclerView.LayoutManager layoutManagerRecycleView;

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    String cityName;

    String nightBG = "https://p0.pxfuel.com/preview/788/12/912/astrophotography-stars-clouds-sky.jpg";
    String dayBG = "https://p1.pxfuel.com/preview/158/967/699/sky-clouds-texture-background.jpg";

    WeatherAPI weatherAPI;
    Localization localization;
    Formatter formatter;

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn the application full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        loaderProgressBar = (ProgressBar)findViewById(R.id.loaderProgressBar);
        contentRelativeLayout = (RelativeLayout)findViewById(R.id.contentRelativeLayout);
        noConnectionRelativeLayout = (RelativeLayout)findViewById(R.id.noConnectionRelativeLayout);
        searchView = (SearchView)findViewById(R.id.searchView);
        degreeTextView = (TextView)findViewById(R.id.deqreeTextView);
        conditionTextView = (TextView)findViewById(R.id.conditionTextView);
        cityTextView = (TextView)findViewById(R.id.cityNameTextView);
        regionTextView = (TextView)findViewById(R.id.regionTextView);
        countryTextView = (TextView)findViewById(R.id.countryTextView);
        conditionIconImageView = (ImageView)findViewById(R.id.conditionIconImageView);
        backgroundImageView = (ImageView)findViewById(R.id.backgroundImageView);
        localTimeTextView = (TextView)findViewById(R.id.localTimeTextView);

        btnTryAgain = (Button)findViewById(R.id.button);

        handler = new Handler();

        recyclerView = (RecyclerView)findViewById(R.id.cardsRecycleView);
        recyclerView.setHasFixedSize(true);
        layoutManagerRecycleView = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManagerRecycleView);

        Picasso.with(MainActivity.this).load(dayBG).into(backgroundImageView);

        weatherAPI = new WeatherAPI();
        localization = new Localization();

        // Used for controlling location updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location permissions from device and access location
        getLocation();

        checkConnection();

        if(location != null)
            cityName = localization.getCityName(location.getLongitude(), location.getLatitude(),
                                                                        new Geocoder(getBaseContext(),
                                                                        Locale.getDefault()));
        else
            cityName = "sydney";

        getAndShowWeatherData(weatherAPI.requestUrl(cityName));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contentRelativeLayout.setVisibility(View.GONE);
                loaderProgressBar.setVisibility(View.VISIBLE);

                cityName = query;
                getAndShowWeatherData(weatherAPI.requestUrl(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noConnectionRelativeLayout.setVisibility(View.GONE);
                loaderProgressBar.setVisibility(View.VISIBLE);
                getLocation();
                getAndShowWeatherData(weatherAPI.requestUrl(cityName));
            }
        });
    }

    // Handle user choice
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Permission Granted", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Please provide the permission, in order to " +
                                        "find your region automatically.", Toast.LENGTH_LONG).show();
            }
        }
    }

    void getLocation(){
        // Check permission for location api
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            // Ask for permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_CODE);
        }

        try {
            /* This takes last location provided by network, if not have no information about location,
             will be returned null */
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception ex){
            Log.i("PERMISSION ERROR", ex.getMessage());
        }
    }

    void checkConnection() {
        // ConnectivityManager for connection checking
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check the internet connection
        connected = Connection.checkConnection(connectivityManager);
        Log.i("ACCESS NETWORK STATE", "Connected: " + connected);
    }

    void getAndShowWeatherData(final String url){
        checkConnection();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(connected){
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    formatter = new Formatter();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // Hide progress bar.
                                    loaderProgressBar.setVisibility(View.GONE);
                                    // Show app content
                                    contentRelativeLayout.setVisibility(View.VISIBLE);
                                    try {
                                        // Get JSON objects from server response
                                        JSONObject location = response.getJSONObject("location");
                                        JSONObject current = response.getJSONObject("current");
                                        JSONObject forecastday = response.getJSONObject("forecast").getJSONArray("forecastday")
                                                .getJSONObject(0);
                                        JSONArray hour = forecastday.getJSONArray("hour");

                                        // Setting data to all views.
                                        cityTextView.setText(location.getString("name"));
                                        degreeTextView.setText(current.getString("temp_c")+"°c");
                                        conditionTextView.setText(current.getJSONObject("condition").getString("text"));
                                        regionTextView.setText(location.getString("region"));
                                        countryTextView.setText(location.getString("country"));
                                        localTimeTextView.setText(formatter.dateTimeFormatting(location.getString("localtime")));

                                        // Load the image from url using picasso library.
                                        if(current.getInt("is_day") == 0)
                                            Picasso.with(MainActivity.this).load(nightBG).into(backgroundImageView);
                                        else
                                            Picasso.with(MainActivity.this).load(dayBG).into(backgroundImageView);

                                        Picasso.with(MainActivity.this).load("https:"+current.getJSONObject("condition")
                                                .getString("icon"))
                                                .into(conditionIconImageView);

                                        time = new ArrayList<>();
                                        temp = new ArrayList<>();
                                        speed = new ArrayList<>();
                                        image = new ArrayList<>();

                                        for(int i = 0;i < hour.length();i++){
                                            JSONObject hourObj = hour.getJSONObject(i);
                                            String timeObj = hourObj.getString("time");
                                            String temperature = hourObj.getString("temp_c");
                                            String imageObj = "https:"+hourObj.getJSONObject("condition").getString("icon");
                                            String wind = hourObj.getString("wind_kph");

                                            time.add(timeObj);
                                            temp.add(temperature);
                                            speed.add(wind);
                                            image.add(imageObj);
                                        }

                                        adapterRecycleView = new CardView(time, temp, speed, image);
                                        recyclerView.setAdapter(adapterRecycleView);

                                    } catch (JSONException e) {

                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loaderProgressBar.setVisibility(View.GONE);
                            contentRelativeLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.this, "No matching location found.", Toast.LENGTH_LONG).show();
                        }
                    });

                    queue.add(jsonObjectRequest);
                }
                else{
                    loaderProgressBar.setVisibility(View.GONE);
                    noConnectionRelativeLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Unable to get weather information.", Toast.LENGTH_LONG).show();
                }
            }
        }, 2000);   //2 seconds
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
