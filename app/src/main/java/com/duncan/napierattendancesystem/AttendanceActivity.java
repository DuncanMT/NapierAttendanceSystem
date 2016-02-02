package com.duncan.napierattendancesystem;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AttendanceActivity extends NfcActivity {

    private static String TAG = EventActivity.class.getSimpleName();

    private ListView students;
    private ArrayList<String> listItems=new ArrayList<>();
    private ArrayAdapter<String> listAdapter;

    private TextView studentGroup;

    private final String url = "http://napierattendance-duncanmt.rhcloud.com/CardID.php?students=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        studentGroup = (TextView) findViewById(R.id.textView2);
        students = (ListView) findViewById(R.id.listView2);
        listAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listItems);
        students.setAdapter(listAdapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("StudentGroup");
            String finishedURL= url+ value;
            studentGroup.setText(value);
            makeStudentsRequest(finishedURL);
        }else{
            studentGroup.setText("Error!");
        }


    }

    private void makeStudentsRequest(String url) {
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listAdapter.clear();
                        Log.d(TAG, "Students request: " + response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject event = (JSONObject) response
                                        .get(i);

                                String name = event.getString("id");
                                listAdapter.add(name);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }
}
