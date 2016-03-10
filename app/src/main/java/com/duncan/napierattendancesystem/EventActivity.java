package com.duncan.napierattendancesystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
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
    private String cardID;
    private boolean verified= false;
    private int currentWeek = 0;

    private TextView weekTextView;

    private Spinner classesSpinner;
    private SpinnerAdapter classesAdapter;

    private ListView studentListView;
    private ListAdapter listAdapter;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            verified = false;
        }
    };

    private final String baseurl = "http://napierattendance-duncanmt.rhcloud.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        username = LoginState.getUserName(EventActivity.this);
        cardID = LoginState.getCardID(EventActivity.this);
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
        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem student = (ListItem) studentListView.getItemAtPosition(position);
                if(student.getPresent().equals("1")) {
                    if (verified) {
                        confirmationMessage(student);
                    } else {
                        Toast.makeText(EventActivity.this, "You are not verified, scan your ID card", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(EventActivity.this, student.getFname()+" is already not present", Toast.LENGTH_LONG).show();
                }
            }
        });

        classesAdapter = new SpinnerAdapter(this, currentWeek);
        classesSpinner.setAdapter(classesAdapter);
        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EventData event = (EventData) parent.getItemAtPosition(position);
                Log.d(TAG, "event and current week" + event.getEvent() + currentWeek);
                makeAttendsRequest(event.getEvent(), Integer.toString(currentWeek));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek > 1) {
                    currentWeek--;
                    setCurrentWeek(currentWeek);
                    EventData event = (EventData) classesSpinner.getSelectedItem();
                    if(event.getWeeks().contains(currentWeek)) {
                        makeAttendsRequest(event.getEvent(), Integer.toString(currentWeek));
                    }else{
                        classesSpinner.setSelection(0);
                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentWeek < 18) {
                    currentWeek++;
                    setCurrentWeek(currentWeek);
                    EventData event = (EventData) classesSpinner.getSelectedItem();
                    if(event.getWeeks().contains(currentWeek)) {
                        makeAttendsRequest(event.getEvent(), Integer.toString(currentWeek));
                    }else{
                        classesSpinner.setSelection(0);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                EventData event = (EventData) classesSpinner.getSelectedItem();
                makeAttendsRequest(event.getEvent(), Integer.toString(currentWeek));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            byte[] idInBinary = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String cardID = readID(idInBinary);
            Log.d(TAG, "Scanned Card ID :"+ cardID+" Saved card ID : "+this.cardID);
            if(cardID.equals(this.cardID)){
                verfiy();
                Toast.makeText(EventActivity.this, "User verified for 2 minutes", Toast.LENGTH_LONG).show();
            }else {
                EventData event = (EventData) classesSpinner.getSelectedItem();
                if (!event.getEvent().equals("Select an Event")) {
                    makeRegisterRequest(cardID, event, Integer.toString(currentWeek));
                } else {
                    Toast.makeText(EventActivity.this, "Error: no event selected",
                            Toast.LENGTH_LONG).show();
                }
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
                                String cardID = event.getString("SPR_CODE");
                                String fname = event.getString("fname");
                                String sname = event.getString("sname");
                                String present = event.getString("trk_val");

                                listAdapter.add(new ListItem(id, cardID, fname, sname, present));
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
                        Log.d(TAG, "Register Response :" + response);
                        if(response.equals("exists")) {
                            Toast.makeText(EventActivity.this, "Student card not recognised",
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
                params.put("week", currentWeek);
                params.put("event", event.getEvent());
                params.put("cardID", cardID);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private void makeSetAbsentRequest(final ListItem student, final EventData event, final String currentWeek ) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/mark_absent.php");

        String finishedurl = url.toString();
        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Register Response :" + response);
                        if(response.equals("exists")) {
                            Toast.makeText(EventActivity.this, "Student already absent",
                                    Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(EventActivity.this, "Student marked absent",
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
                params.put("week", currentWeek);
                params.put("event", event.getEvent());
                params.put("cardID", student.getCardID());

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
            LoginState.clearUserInfo(this);
            Intent intent = new Intent(EventActivity.this, LoginActivity.class);
            EventActivity.this.startActivity(intent);
            EventActivity.this.finish();
            return true;
        }

        if (id == R.id.action_add_student) {
            if(verified) {
                Intent intent = new Intent(EventActivity.this, AddStudentActivity.class);
                EventData event = (EventData) classesSpinner.getSelectedItem();
                intent.putExtra("event", event);
                EventActivity.this.startActivityForResult(intent, 1);
            }else{
                Toast.makeText(EventActivity.this, "You are not verified, scan your ID card", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCurrentWeek(int currentWeek) {
        this.currentWeek = currentWeek;
        weekTextView.setText("Week " + currentWeek);
        classesAdapter.setCurrentWeek(currentWeek);
        makeClassesRequest(username);
    }

    private void verfiy(){
        verified = true;
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 120000);
    }

    public void confirmationMessage(final ListItem student) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                        EventData event = (EventData) classesSpinner.getSelectedItem();
                        if(!event.getEvent().equals("Select an Event")) {
                            makeSetAbsentRequest(student, event, Integer.toString(currentWeek));
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE: // No button clicked // do nothing
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to mark " + student.getFname()+" absent?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
