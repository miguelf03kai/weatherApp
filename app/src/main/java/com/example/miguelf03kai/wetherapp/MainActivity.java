package com.example.miguelf03kai.wetherapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ProgressBar pb;
    private ImageView ivCondition,background;
    private TextView degree,conditionTv,cityTv,tvRegion,tvCountry;
    private SearchView search;
    private RelativeLayout rl,content;

    ArrayList<String> time,temp,speed,image;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    String cityName;

    String nightBG = "https://p0.pxfuel.com/preview/788/12/912/astrophotography-stars-clouds-sky.jpg";
    String dayBG = "https://p1.pxfuel.com/preview/158/967/699/sky-clouds-texture-background.jpg";

    api API;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to make the application full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        pb = (ProgressBar)findViewById(R.id.progressBar);
        rl = (RelativeLayout)findViewById(R.id.main);
        content = (RelativeLayout)findViewById(R.id.content);
        search = (SearchView)findViewById(R.id.searchView);
        degree = (TextView)findViewById(R.id.textView3);
        conditionTv = (TextView)findViewById(R.id.textView4);
        cityTv = (TextView)findViewById(R.id.textView);
        tvRegion = (TextView)findViewById(R.id.tvRegion);
        tvCountry = (TextView)findViewById(R.id.tvCountry);
        ivCondition = (ImageView)findViewById(R.id.imageView);
        background = (ImageView)findViewById(R.id.background);

        API = new api();

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);

        Picasso.with(MainActivity.this).load(dayBG).into(background);

        //used for controlling location updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //check permission for location api
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //ask for permission
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        //this takes last request done by the gps device, if not have no request done, the application will returns null
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location != null)
            cityName = getCityName(location.getLongitude(), location.getLatitude());
        else
            cityName = "sydney";

        getWeather(API.checkWaether(cityName));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                content.setVisibility(View.GONE);
                pb.setVisibility(View.VISIBLE);

                getWeather(API.checkWaether(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //handle user choice
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"Permission Granted", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

       try {

           List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
           /*for (type variableName : arrayName) {
               // code block to be executed
           }*/
           for(Address adr: addresses){
               if(adr!=null){
                   String city = adr.getLocality();
                   if(city!=null && !city.equals("")){
                       cityName = city;
                   }else{
                       Log.d("TAG","City not Found");
                   }

               }
           }
           /**
            * The Geocoder class requires a backend service that is not included in the core android framework.
            * The Geocoder query methods will return an empty list if there no backend service in the platform.
            * Use the isPresent() method to determine whether a Geocoder implementation exists.
            * */
           /*if(gcd.isPresent())
               cityName = "Service exist";
           else
               cityName = "Service not exist";
            */
       }catch (IOException e){
            e.printStackTrace();
       }

        return cityName;
    }

    public void getWeather(String url){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //hide progress bar.
                pb.setVisibility(View.GONE);
                // show app content
                content.setVisibility(View.VISIBLE);
                try {
                    //get json objects from server response
                    JSONObject location = response.getJSONObject("location");
                    JSONObject current = response.getJSONObject("current");
                    JSONObject forecastday = response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hour = forecastday.getJSONArray("hour");

                    // setting data to all views.
                    cityTv.setText(location.getString("name"));
                    degree.setText(current.getString("temp_c")+"°c");
                    conditionTv.setText(current.getJSONObject("condition").getString("text"));
                    tvRegion.setText(location.getString("region"));
                    tvCountry.setText(location.getString("country"));

                    //load the image from url using picasso library.
                    if(current.getInt("is_day") == 0)
                        Picasso.with(MainActivity.this).load(nightBG).into(background);
                    else
                        Picasso.with(MainActivity.this).load(dayBG).into(background);

                    Picasso.with(MainActivity.this).load("https:"+current.getJSONObject("condition").getString("icon")).into(ivCondition);


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

                    adapter = new Cards(time,temp,speed,image);
                    recyclerView.setAdapter(adapter);

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                pb.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "No matching location found.", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonObjectRequest);

    }

}
