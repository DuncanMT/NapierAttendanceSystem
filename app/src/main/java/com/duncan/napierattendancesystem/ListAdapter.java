package com.duncan.napierattendancesystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ListAdapter extends BaseAdapter {

    Context context;
    ArrayList<ListItem> data = new ArrayList<>();
    ArrayList<Integer> colouredItems = new ArrayList<>();
    private static LayoutInflater inflater = null;

    public ListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v=convertView;
        ListItem item = data.get(position);

        if(v==null){
            v = inflater.inflate(R.layout.student_present, null);
        }
        TextView text = (TextView) v.findViewById(R.id.text);
        text.setText(item.getText());

        if (item.getColour().equals("red")){
            v.setBackgroundColor(Color.RED);
        }else{
            v.setBackgroundColor(Color.BLUE);
        }

        return v;
    }

    public void clear(){
        data.clear();
    }

    public void add(ListItem newdata){
        data.add(newdata);
    }
}
