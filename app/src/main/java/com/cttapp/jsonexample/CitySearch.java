package com.cttapp.jsonexample;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CitySearch extends AppCompatActivity{

    ListView listView;
    GoogleApiClient mGoogleApiClient;
    JSONArray predictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);
        setTitle("City Search");

        listView = (ListView) findViewById(R.id.cityList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                int itemPosition = position;
                Log.d("SEARCHPRESS", "Pressed index " + position);
                try {
                    Log.d("SEARCHPRESS", "Object " + predictions.getJSONObject(position));

                    String placeID = predictions.getJSONObject(position).getString("place_id");
                    new GetLocationData("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=AIzaSyANtQLTuo1ypXb-G_AYRoVu4CihVkwu_Tw", 1).execute();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });

        EditText editText = (EditText) findViewById(R.id.citySearch);

        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s){
                try {
                    searchForCity(s.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){
                // you can check for enter key here
            }
            public void  onTextChanged (CharSequence s, int start, int before,int count) {
            }
        };


        editText.addTextChangedListener(tw);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    //sendMessage();
                    handled = true;

                    try {
                        searchForCity(v.getText().toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return handled;
            }
        });


    }


    public void searchForCity(String data) throws UnsupportedEncodingException {
        Log.d("SEARCH", data);

        String searchURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(data, "UTF-8") + "&types=geocode&types=(cities)&key=AIzaSyANtQLTuo1ypXb-G_AYRoVu4CihVkwu_Tw";
        new GetLocationData(searchURL, 0).execute();

    }

    public void gotAutoCompleteData(String data) throws JSONException {

        JSONObject obj = new JSONObject(data);
        predictions = obj.getJSONArray("predictions");

        List<String> adapterList = new ArrayList<String>();

        for(int x=0; x < predictions.length(); x++)
        {
            adapterList.add(predictions.getJSONObject(x).getString("description"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, adapterList);
        listView.setAdapter(adapter);
    }

    public void gotGeoData(String data) throws JSONException {

        JSONObject obj = new JSONObject(data);
        JSONObject location = obj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

        Intent intent = new Intent();
        intent.putExtra("location", "lat=" + location.getString("lat") + "&lon=" + location.getString("lng"));
        intent.putExtra("city", obj.getJSONObject("result").getString("formatted_address"));
        setResult(RESULT_OK, intent);
        finish();
    }

    // create a new class
    private class GetLocationData extends
            AsyncTask<Void, Void, Void> {

        private String result;
        private String dataURL = "";
        private int callback;

        public GetLocationData(String pathURL, int scallback) {
            super();
            dataURL = pathURL;
            callback = scallback;
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
                switch (callback)
                {
                    case 0:
                        try {
                            gotAutoCompleteData(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            gotGeoData(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
            else {
                Log.d("DATA", "No data received");
            }
        }
    }

}
