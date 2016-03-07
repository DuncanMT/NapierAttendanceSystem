package com.duncan.napierattendancesystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Duncan on 01/03/2016.
 * Custom Adapter for the spinner in the events activity so that it can store all the event data
 * and hide events not occurring on the selected week.
 */
public class SpinnerAdapter extends BaseAdapter {
    private Context context;
    private int currentWeek;
    private LayoutInflater inflater;
    private ArrayList<EventData> data = new ArrayList<>();

    public SpinnerAdapter(Context context, int currentWeek){
        this.context = context;
        this.currentWeek = currentWeek;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public EventData getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public void setCurrentWeek(int currentWeek) {
        this.currentWeek = currentWeek;
    }


    public void clear(){
        data.clear();
    }

    public void add(EventData newData){
        data.add(newData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v=convertView;
        EventData item = data.get(position);

        if(v==null){
            v = inflater.inflate(R.layout.spinner_event, null);
        }

        TextView idView = (TextView) v.findViewById(R.id.event);
        idView.setText(item.getEvent());
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = null;
        EventData event  = data.get(position);
        if (event.getWeek() != currentWeek) {
            TextView tv = new TextView(context);
            tv.setVisibility(View.GONE);
            tv.setHeight(0);
            v = tv;
        } else {
            v = super.getDropDownView(position, null, parent);
        }
        return v;
    }
}
