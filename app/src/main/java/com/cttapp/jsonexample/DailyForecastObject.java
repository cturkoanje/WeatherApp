package com.cttapp.jsonexample;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Chris on 11/5/15.
 */
public class DailyForecastObject {

    private String temp, min, max, desc, icon;
    private int timeStamp;

    public DailyForecastObject(JSONObject data) throws JSONException {

        JSONObject cData = data.getJSONObject("temp");
        JSONObject descData = data.getJSONArray("weather").getJSONObject(0);

        timeStamp = data.getInt("dt");
        temp = String.format("%.0f", cData.getDouble("day"));
        min = String.format("%.0f", cData.getDouble("min"));
        max = String.format("%.0f", cData.getDouble("max"));
        desc = descData.getString("description");
        icon = descData.getString("icon");

        Log.d("TIMESTAMP", ""+timeStamp);
    }

    public String getTemp() {
        return temp + "\u2109";
    }

    public String getMinTemp() {
        return min + "\u2109";
    }

    public String getMaxTemp() {
        return max + "\u2109";
    }

    public String getDescription() {
        return desc;
    }

    public String getIcon() {
        return "i"+icon;
    }

    public String getDate() {
        long dv = Long.valueOf(timeStamp)*1000;// its need to be in milisecond
        Date df = new java.util.Date(dv);
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return format.format(df);
    }
}
