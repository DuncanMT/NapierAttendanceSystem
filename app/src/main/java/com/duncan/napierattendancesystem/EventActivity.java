package com.duncan.napierattendancesystem;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EventActivity extends NfcActivity  {

    private static String TAG = EventActivity.class.getSimpleName();

    private String username;
    private int currentWeek = 0;

    private TextView weekTextView;

    private Spinner classesSpinner;
    private SpinnerAdapter classesAdapter;

    private ListView studentListView;
    private ListAdapter listAdapter;

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
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);

        makeWeekNoRequest();
        listAdapter = new ListAdapter(this);
        studentListView.setAdapter(listAdapter);

        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EventData event = (EventData) parent.getItemAtPosition(position);
                Log.d(TAG, "event and current week" + event.getEvent()+ currentWeek);
                makeAttendsRequest(event.getEvent(), Integer.toString(currentWeek));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        classesAdapter = new SpinnerAdapter(this, currentWeek);
        classesSpinner.setAdapter(classesAdapter);

        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek > 1) {
                    currentWeek--;
                    weekTextView.setText("Week " + currentWeek);
                    classesAdapter.setCurrentWeek(currentWeek);
                    classesSpinner.setSelection(0);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek < 18) {
                    currentWeek++;
                    weekTextView.setText("Week " + currentWeek);
                    classesAdapter.setCurrentWeek(currentWeek);
                    classesSpinner.setSelection(0);
                }
            }
        });
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
            EventData event = (EventData) classesSpinner.getSelectedItem();
            if(!event.getEvent().equals("Select an Event")) {
                makeRegisterRequest(cardID, event, Integer.toString(currentWeek));
            }else{
                Toast.makeText(EventActivity.this, "Error: no event selected",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void makeWeekNoRequest() {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/weeknum.php");
        String finishedurl = url.toString();
        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Volley response: " + response);
                        if(response.length()>0) {
                            setCurrentWeek(Integer.parseInt(response));
                        }
                    }
                }, CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("weeknum", "");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeClassesRequest(final String username) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/classes.php");
        String finishedurl = url.toString();
        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONArray data = new JSONArray(response);
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject event = data.getJSONObject(i);

                                String trimester = "TR1";
                                String day="0";
                                switch(event.getString("dy")){
                                    case "Monday":day ="1"; break;
                                    case "Tuesday":day ="2"; break;
                                    case "Wednesday":day ="3"; break;
                                    case "Thursday":day ="4"; break;
                                    case "Friday": day ="5";break;
                                }
                                String time = event.getString("st");
                                String module = event.getString("modul");
                                String eventName = event.getString("id");
                                int week = event.getInt("week");
                                if(classesAdapter.contains(eventName)){
                                    classesAdapter.addWeek(week, eventName);
                                }else{
                                    classesAdapter.add(new EventData(trimester, day, time,module,eventName,week));
                                }
                            }
                            classesAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "VolleyJsonClassesError :" + e.getMessage());
                        }

                    }
                },CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("name", username);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeAttendsRequest(final String event, final String currentWeek) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/attends.php");
        String finishedurl = url.toString();

        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listAdapter.clear();
                        try {
                            JSONArray data = new JSONArray(response);

                            for (int i = 0; i < data.length(); i++) {

                                JSONObject event = data.getJSONObject(i);

                                String id = event.getString("matric_no");
                                String fname = event.getString("fname");
                                String sname = event.getString("sname");
                                String present = event.getString("trk_val");

                                listAdapter.add(new ListItem(id, fname, sname, present));
                            }
                            listAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "VolleyJsonAttendsError :" + e.getMessage());
                        }

                    }
                }, CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("attends", event);
                params.put("week", currentWeek);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeRegisterRequest(final String cardID, final EventData event, final String currentWeek ) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/register_attendance.php");

        String finishedurl = url.toString();
        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG,"Register Response :"+ response);
                        if(response.equals("exists")) {
                            Toast.makeText(EventActivity.this, "Student already registered",
                                    Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(EventActivity.this, "Student registered",
                                    Toast.LENGTH_LONG).show();
                            makeAttendsRequest(event.getEvent(), currentWeek);
                        }
                    }
                }, CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("trimester", event.getTrimester());
                params.put("week", currentWeek);
                params.put("day",event.getDay());
                params.put("module", event.getModule());
                params.put("time", event.getTime());
                params.put("event", event.getEvent());
                params.put("cardID", cardID);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private Response.ErrorListener CreateErrorListener(){
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
            }
        };
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
            Intent intent = new Intent(EventActivity.this, LoginActivity.class);
            EventActivity.this.startActivity(intent);
            EventActivity.this.finish();
            return true;
        }

        if (id == R.id.action_add_student) {
            Intent intent = new Intent(EventActivity.this, AddStudentActivity.class);
            EventData event = (EventData) classesSpinner.getSelectedItem();
            intent.putExtra("event",event);
            EventActivity.this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCurrentWeek(int currentWeek) {
        this.currentWeek = currentWeek;
        weekTextView.setText("Week "+ currentWeek);
        makeClassesRequest(username);
    }
}
