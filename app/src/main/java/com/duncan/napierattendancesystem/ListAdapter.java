package com.duncan.napierattendancesystem;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ListItem> data = new ArrayList<>();
    private static LayoutInflater inflater = null;

    public ListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(data);

        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ListItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void clear(){
        data.clear();
    }

    public void add(ListItem newData){
        data.add(newData);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v=convertView;
        ListItem item = data.get(position);

        if(v==null){
            v = inflater.inflate(R.layout.student, null);
        }
        TextView idView = (TextView) v.findViewById(R.id.id);
        idView.setText(item.getId());
        TextView fnameView = (TextView) v.findViewById(R.id.fname);
        fnameView.setText(item.getFname());
        TextView snameView = (TextView) v.findViewById(R.id.sname);
        snameView.setText(item.getSname());
        TextView presentView = (TextView) v.findViewById(R.id.present);
        ImageView status = (ImageView) v.findViewById(R.id.status);


        if (item.getPresent().equals("1")){
            status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.blue_circle));
            presentView.setText(R.string.present);
        }else{
            status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.red_circle));
            presentView.setText(R.string.absent);
        }

        return v;
    }
}
