package com.cttapp.jsonexample;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ListActivity implements OnMapReadyCallback {

    private static final String TAG_CITY="name";
    private static final String TAG_LATITUDE="latitude";
    private static final String TAG_LONGITUDE="longitude";

    private static final String TAG_TEMP="day";
    private static final String TAG_TEMP_MIN="min";
    private static final String TAG_TEMP_MAX="max";
    private static final String TAG_DESCRIPTION="description";
    private static final String TAG_IMAGE="image";
    private GoogleMap map;
    private LatLng currentLocation = null;

    ArrayList<HashMap<String,String>> weatherData;

    private List<DailyForecastObject> weatherList = new ArrayList<DailyForecastObject>();
    private WeatherListAdapter newAdapter;

    private ProgressDialog ringProgressDialog;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherData = new ArrayList<HashMap<String, String>>();

        // create a new AsyncTask
        ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Loading Weather Data", true);
        new GetWeatherData(getURLForZip("11554")).execute();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setTitle("Weather");

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        int id = item.getItemId();
                        if (id == R.id.search) {
                            Intent intent = new Intent(MainActivity.this, CitySearch.class);
                            startActivityForResult(intent, 1);
                            return true;
                        }

                        return true;
                    }
                });
    }

    public static Drawable loadImageFromIcon(String icon) {
        try {
            InputStream is = (InputStream) new URL("http://openweathermap.org/img/w/" + icon + ".png").getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    private String getURLForZip(String zipCode) {
        return "http://api.openweathermap.org/data/2.5/forecast/daily?zip=" + zipCode + ",us&units=imperial&APPID=c59c8be06eb651401da3f4e32aa4371e&cnt=10";
    }

    private String getUrlForLatLng(String point) {
        return "http://api.openweathermap.org/data/2.5/forecast/daily?" + point + "&units=imperial&APPID=c59c8be06eb651401da3f4e32aa4371e&cnt=10";
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String location = data.getStringExtra("location");
                String city = data.getStringExtra("city");
                ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Loading Weather Data for " + city, true);
                new GetWeatherData(getUrlForLatLng(location)).execute();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        Log.d("MAPS", "maps is ready");
    }

    // create a new class
    private class GetWeatherData extends
            AsyncTask<Void, Void, Void> {

        private String result;
        private String dataURL = "";

        public GetWeatherData(String pathURL) {
            super();
            dataURL = pathURL;
        }

        public GetWeatherData() {
            super();
            dataURL = getURLForZip("11530");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // create a HttpURLConnection
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(dataURL);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inStream = urlConnection.getInputStream();
                // if no input is received, then return from method
                if (inStream == null)
                {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inStream));
                result = "";
                String str;
                while ((str = reader.readLine()) != null)
                {
                    result += str;
                }
                Log.d("RESULT", "The string is " + result);
            } catch (Exception e) {
                Log.i("HttpAsyncTask", "EXCEPTION: " +
                        e.getMessage());
            } finally {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void r)
        {
            super.onPostExecute(r);
            // check to make sure the result != null

            if (result != null)
            {
                try {

                    Log.d("JSONRETURN", "on post execute");

                    JSONObject jsonObj = new JSONObject(result);

                    Log.d("JSONRETURN", "JSON Object");
                    Log.d("JSONRETURN", jsonObj.toString());

                    Log.d("JSONRETURN", "JSON CITY");
                    Log.d("JSONRETURN", jsonObj.getJSONObject("city").toString());

                    JSONObject coord = jsonObj.getJSONObject("city").getJSONObject("coord");

                    double lat = coord.getDouble("lat");
                    double lng = coord.getDouble("lon");

                    currentLocation = new LatLng(lat, lng);

                    if(map != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
                    }

                    Log.d("MAPS", "trying to update map");

                    Log.d("PARSING", "lat: " + lat + " lon: " + lng);

                    Log.d("PARSING", "Weather Array: ");
                    Log.d("PARSING", jsonObj.getJSONArray("list").toString());

                    JSONArray weather = jsonObj.getJSONArray("list");

                    Log.d("JSONRETURN", "got result");
                    Log.d("JSONRETURN", weather.toString());

                    JSONObject dayDetail2 = weather.getJSONObject(0);
                    JSONObject temp2 = dayDetail2.getJSONObject("temp");
                    JSONObject desc2 = dayDetail2.getJSONArray("weather").getJSONObject(0);
                    if(map != null) {
                        map.addMarker(new MarkerOptions().position(currentLocation).title(jsonObj.getJSONObject("city").getString("name")).snippet(temp2.getString("day") + "78\u00B0" + "F\n" + desc2.getString("description")));
                    }

                    toolbar.setTitle(jsonObj.getJSONObject("city").getString("name"));

                    for(int i = 0 ; i < weather.length(); i++) {

                        HashMap<String, String>day = new HashMap<String, String>();

                        weatherList.add(new DailyForecastObject(weather.getJSONObject(i)));

                        JSONObject dayDetail = weather.getJSONObject(i);
                        JSONObject temp = dayDetail.getJSONObject("temp");
                        JSONObject desc = dayDetail.getJSONArray("weather").getJSONObject(0);

                        day.put(TAG_TEMP, temp.getString("day"));
                        day.put(TAG_TEMP_MIN, temp.getString("min"));
                        day.put(TAG_TEMP_MAX, temp.getString("max"));
                        day.put(TAG_DESCRIPTION, desc.getString("description"));

                        weatherData.add(day);

                        Log.d("OBJECT FOR INDEX " + i, day.toString());


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ringProgressDialog.dismiss();
            }
            else {
                Log.d("DATA", "No data received");
            }

            // add to the view
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, weatherData, R.layout.list_item,
                    new String[] { TAG_TEMP, TAG_TEMP_MIN, TAG_TEMP_MAX, TAG_DESCRIPTION },
                    new int[] { R.id.temperature, R.id.min, R.id.max, R.id.description });


            newAdapter = new WeatherListAdapter(MainActivity.this, weatherList);

            setListAdapter(newAdapter);
        }
    }
}
