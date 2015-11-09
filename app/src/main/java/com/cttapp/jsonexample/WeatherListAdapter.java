package com.cttapp.jsonexample;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;


import org.json.JSONObject;

import java.util.List;

/**
 * Created by Chris on 11/5/15.
 */
public class WeatherListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<DailyForecastObject> forcastList;


    public WeatherListAdapter(Activity activity, List<DailyForecastObject> forcastList) {
        this.activity = activity;
        this.forcastList = forcastList;
    }

    @Override
    public int getCount() {
        return forcastList.size();
    }

    @Override
    public Object getItem(int location) {
        return forcastList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item, null);


        TextView temp = (TextView) convertView.findViewById(R.id.temperature);
        TextView min = (TextView) convertView.findViewById(R.id.min);
        TextView max = (TextView) convertView.findViewById(R.id.max);
        TextView desc = (TextView) convertView.findViewById(R.id.description);
        TextView day = (TextView) convertView.findViewById(R.id.dayLabel);
        ImageView img = (ImageView) convertView.findViewById(R.id.thumbnail);

        // getting movie data for the row
        DailyForecastObject m = forcastList.get(position);

        // title
        temp.setText(m.getTemp());
        min.setText(m.getMinTemp());
        max.setText(m.getMaxTemp());
        desc.setText(m.getDescription());
        day.setText(m.getDate());

        int iconID = activity.getResources().getIdentifier(m.getIcon(), "drawable", activity.getApplicationContext().getPackageName());
        img.setImageDrawable(ContextCompat.getDrawable(activity, iconID));

        return convertView;
    }
}
