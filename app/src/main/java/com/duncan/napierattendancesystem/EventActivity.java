package com.duncan.napierattendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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

public class EventActivity extends AppCompatActivity {

    private static String TAG = EventActivity.class.getSimpleName();
    private Spinner classesSpinner;
    private ArrayList<String> classes = new ArrayList<>();

    private ListView studentListView;
    private ArrayList<String> listItems=new ArrayList<>();
    private ArrayAdapter<String> listAdapter;

    private final String urlClasses = "http://napierattendance-duncanmt.rhcloud.com/CardID.php?classes=";
    private final String urlAttends = "http://napierattendance-duncanmt.rhcloud.com/CardID.php?attends=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Log.d(TAG, "Login State username: " + LoginState.getUserName(EventActivity.this));
        if(LoginState.getUserName(EventActivity.this).length() == 0)
        {
            Intent mainIntent = new Intent(EventActivity.this, LoginActivity.class);
            EventActivity.this.startActivity(mainIntent);
            EventActivity.this.finish();
        }

        classesSpinner = (Spinner) findViewById(R.id.module);
        studentListView = (ListView) findViewById(R.id.listView);

        String Finishedurl=urlClasses + LoginState.getUserName(EventActivity.this);
        Finishedurl = Finishedurl.replaceAll(" ","%20");
        Log.d(TAG, Finishedurl);

        makeClassesRequest(Finishedurl);

        listAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        studentListView.setAdapter(listAdapter);

        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String module = parent.getItemAtPosition(position).toString();
                Log.d(TAG,"module = "+module);
                String Finished = urlAttends+module;
                Log.d(TAG,"Attends Request = "+Finished);
                makeAttendsRequest(Finished);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void makeClassesRequest(String url) {
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject event = (JSONObject) response
                                        .get(i);

                                String name = event.getString("event");
                                classes.add(name);
                            }
                            ArrayAdapter adapter = new ArrayAdapter<>(EventActivity.this, android.R.layout.simple_spinner_item, classes);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            classesSpinner.setAdapter(adapter);

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

    private void makeAttendsRequest(String url) {
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listAdapter.clear();
                        Log.d(TAG, "Attends response: " + response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject event = (JSONObject) response
                                        .get(i);

                                String name = event.getString("SPR_CODE");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            LoginState.clearUserName(this);
            Intent mainIntent = new Intent(EventActivity.this, LoginActivity.class);
            EventActivity.this.startActivity(mainIntent);
            EventActivity.this.finish();
            return true;
        }
        if (id == R.id.action_timetable) {
            Intent eventIntent = new Intent(EventActivity.this, TimetableActivity.class);
            EventActivity.this.startActivity(eventIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
