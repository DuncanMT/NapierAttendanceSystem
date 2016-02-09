package com.duncan.napierattendancesystem;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EventActivity extends NfcActivity  {

    private static String TAG = EventActivity.class.getSimpleName();

    private String username;

    private int currentWeek = 0;

    private TextView weekTextView;

    private Spinner classesSpinner;
    private ArrayAdapter<String> classesAdapter;

    private ListView studentListView;
    private ListAdapter listAdapter;

    private Button prevButton, nextButton;

    private final String baseurl = "http://napierattendance-duncanmt.rhcloud.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        username = LoginState.getUserName(EventActivity.this);
        Log.d(TAG, "Login State username: " + username);
        if(username.length() == 0)
        {
            Intent mainIntent = new Intent(EventActivity.this, LoginActivity.class);
            EventActivity.this.startActivity(mainIntent);
            EventActivity.this.finish();
        }

        weekTextView = (TextView) findViewById(R.id.week);
        classesSpinner = (Spinner) findViewById(R.id.module);
        studentListView = (ListView) findViewById(R.id.listView);
        prevButton = (Button) findViewById(R.id.prevButton);
        nextButton = (Button) findViewById(R.id.nextButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek > 1) {
                    currentWeek--;
                    weekTextView.setText("Week "+ currentWeek);
                    makeClassesRequest();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek < 18) {
                    currentWeek++;
                    weekTextView.setText("Week " + currentWeek);
                    makeClassesRequest();
                }
            }
        });

        makeWeekNoRequest();
        listAdapter = new ListAdapter(this);
        studentListView.setAdapter(listAdapter);

        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String event = parent.getItemAtPosition(position).toString();
                makeAttendsRequest(event);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        classesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        classesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classesSpinner.setAdapter(classesAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            byte [] idInBinary = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String cardID = readID(idInBinary);
            makeRegisterRequest(cardID);

        }
    }

    private String readID(byte [] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private void makeWeekNoRequest() {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("CardID.php");
        url.appendQueryParameter("weeknum", "");
        String finishedurl = url.toString();
        JsonArrayRequest req = new JsonArrayRequest(finishedurl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject event = (JSONObject) response
                                        .get(i);
                                String curweek = event.getString("n");
                                currentWeek = Integer.parseInt(curweek);
                                makeClassesRequest();
                                weekTextView.setText("Week "+ currentWeek);

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
                        "Volley weeknum Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeClassesRequest() {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("CardID.php");
        url.appendQueryParameter("name", username);
        url.appendQueryParameter("week", Integer.toString(currentWeek));
        String finishedurl = url.toString();
        JsonArrayRequest req = new JsonArrayRequest(finishedurl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            classesAdapter.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject event = (JSONObject) response
                                        .get(i);

                                String name = event.getString("id");
                                classesAdapter.add(name);
                            }
                            classesAdapter.notifyDataSetChanged();

                            if(classesSpinner.getSelectedItem()!= null)
                                makeAttendsRequest(classesSpinner.getSelectedItem().toString());
                            else
                                Toast.makeText(getApplicationContext(),
                                        "Spinner error",
                                        Toast.LENGTH_LONG).show();


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
                        "Volley classes Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeAttendsRequest(String event) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("CardID.php");
        url.appendQueryParameter("attends", event);
        url.appendQueryParameter("week", Integer.toString(currentWeek));
        String finishedurl = url.toString();
        JsonArrayRequest req = new JsonArrayRequest(finishedurl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listAdapter.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject event = (JSONObject) response
                                        .get(i);

                                String name = event.getString("matric_no");
                                String present = event.getString("trk_val");

                                if(present.equals("0")) {
                                    listAdapter.add(new ListItem(name, "red"));
                                }else{
                                    listAdapter.add(new ListItem(name, "blue"));
                                }
                            }
                            listAdapter.notifyDataSetChanged();

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
                        "Volley attends Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeRegisterRequest(String cardID) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("CardID.php");
        url.appendQueryParameter("trimester", "TR1");
        url.appendQueryParameter("week", Integer.toString(currentWeek));
        url.appendQueryParameter("day", "4");
        url.appendQueryParameter("time","15:00" );
        url.appendQueryParameter("module", "SET10109");
        url.appendQueryParameter("event", classesSpinner.getSelectedItem().toString() );
        url.appendQueryParameter("matric", cardID);

        String finishedurl = url.toString();

        JsonArrayRequest req = new JsonArrayRequest(finishedurl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        makeAttendsRequest(classesSpinner.getSelectedItem().toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                makeAttendsRequest(classesSpinner.getSelectedItem().toString());
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

        return super.onOptionsItemSelected(item);
    }
}
