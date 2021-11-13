package com.example.miguelf03kai.wetherapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;

    private ProgressBar pb;
    private ImageView condition;
    private TextView degree,conditionTv,cityTv;
    private Button search;
    private EditText city;
    private RelativeLayout rl,content;

    ArrayList<String> time,temp,speed,image;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    String cityName;

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
        search = (Button)findViewById(R.id.button);
        degree = (TextView)findViewById(R.id.textView3);
        city = (EditText)findViewById(R.id.editText);
        conditionTv = (TextView)findViewById(R.id.textView4);
        cityTv = (TextView)findViewById(R.id.textView);

        API = new api();

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);

        //get location from internet
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //check permission for location api
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //ask for permission
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        //this takes last request done by the gps device, if not have no request the application will returns null
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location != null)
            cityName = getCityName(location.getLongitude(), location.getLatitude());
        else
            cityName = "sydney";

        weather(API.checkWaether(cityName));

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weather(API.checkWaether(city.getText().toString()));
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

    private void weather(String url) {
        performNetworkRequest request = new performNetworkRequest(url, null, CODE_GET_REQUEST);
        request.execute();
    }

    //INNER CLASS to perform network request extending an AsyncTask
    private class performNetworkRequest extends AsyncTask<Void, Void, String> {

        //the url where we need to send the request
        String url;

        //the parameters
        HashMap<String, String> params;

        //the request code to define whether it is a GET or POST
        int requestCode;

        //constructor to initialize values
        performNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        //when the task started displaying a progressbar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        //this method will give the response from the request
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pb.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            try {
                JSONObject object = new JSONObject(s);

                JSONObject val = object.getJSONObject("current");
                JSONObject city = object.getJSONObject("location");
                JSONObject icon = object.getJSONObject("current").getJSONObject("condition");

                degree.setText(val.getString("temp_c") + "°c");

                conditionTv.setText(icon.getString("text"));
                cityTv.setText(city.getString("name"));

                new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute("https:" + icon.getString("icon"));

                int day = val.getInt("is_day");

                rl.setBackgroundResource(background(day));

                JSONObject forecast = object.getJSONObject("forecast");
                JSONObject forecastday = forecast.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hour = forecastday.getJSONArray("hour");

                time = new ArrayList<>();
                temp = new ArrayList<>();
                speed = new ArrayList<>();
                image = new ArrayList<>();

                for(int i = 0;i < hour.length();i++){
                    JSONObject hourObj = hour.getJSONObject(i);
                    String timeObj = hourObj.getString("time");
                    String temperature = hourObj.getString("temp_c");
                    String imageObj = hourObj.getJSONObject("condition").getString("icon");
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
                Toast.makeText(getApplicationContext(), "No matching location found.", Toast.LENGTH_LONG).show();
            }

        }

        //the network operation will be performed in background
        @Override
        protected String doInBackground(Void... voids) {
            requestHandler requestHandler = new requestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);


            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }
    //change background image between day and night
    int background(int isDay){

        int imageResource;

        if(isDay == 1)
            imageResource = getResources().getIdentifier("@drawable/day",null,this.getPackageName());
        else
            imageResource = getResources().getIdentifier("@drawable/night",null,this.getPackageName());

        return imageResource;
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
                       //Toast.makeText(getApplicationContext(), "city not found", Toast.LENGTH_LONG).show();
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
}
