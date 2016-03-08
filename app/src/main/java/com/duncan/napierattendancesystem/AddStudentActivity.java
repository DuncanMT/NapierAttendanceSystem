package com.duncan.napierattendancesystem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddStudentActivity extends NfcActivity {

    private static String TAG = AddStudentActivity.class.getSimpleName();

    private EditText matricNotxt, firstNametxt, lastNametxt, cardIDtxt;
    private TextView eventNametxt;

    private final String baseurl = "http://napierattendance-duncanmt.rhcloud.com";
    private EventData event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        eventNametxt = (TextView) findViewById(R.id.event_name);
        matricNotxt = (EditText) findViewById(R.id.matric_num);
        firstNametxt = (EditText) findViewById(R.id.first_name);
        lastNametxt = (EditText) findViewById(R.id.last_name);
        cardIDtxt = (EditText) findViewById(R.id.card_id);

        event = (EventData) getIntent().getSerializableExtra("event");
        eventNametxt.setText(event.getEvent());

        Button addButton = (Button) findViewById(R.id.add_student_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (validateInput()) {
                    confirmationMessage();
                } else {
                    Toast.makeText(AddStudentActivity.this,
                            "Please enter a value for all of the input fields",
                            Toast.LENGTH_LONG).show();
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
            cardIDtxt.setText(cardID);
        }
    }

    public boolean validateInput(){
        if(!eventNametxt.getText().toString().equals("Select an Event") &&
                matricNotxt.getText().toString().length()>0 &&
                firstNametxt.getText().toString().length()>0 &&
                lastNametxt.getText().toString().length()>0 &&
                cardIDtxt.getText().toString().length()>0){
            return true;
        }else{
            return false;
        }
    }

    private void makeAddStudentRequest(final EventData event, final String matric, final String fname, final String sname, final String cardID) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("post/add_student.php");
        String finishedurl = url.toString();
        StringRequest req = new StringRequest(Request.Method.POST, finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Volley response: " + response);
                        try {
                            JSONObject result = new JSONObject(response);
                            if(result.get("classes").toString().equals("fail")){
                                Toast.makeText(AddStudentActivity.this,
                                        "Error adding Student to Event",
                                        Toast.LENGTH_LONG).show();
                                if(result.get("students").toString().equals("fail")){
                                    Toast.makeText(AddStudentActivity.this,
                                            "Error adding student details",
                                            Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(AddStudentActivity.this,
                                            "Student added to Event",
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "VolleyJsonAttendsError :" + e.getMessage());
                        }
                        /*if(response.equals("fail")){
                            Toast.makeText(AddStudentActivity.this, "Error, ",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(AddStudentActivity.this, "Student already registered",
                                    Toast.LENGTH_LONG).show();
                        }*/
                    }
                }, CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("weekStart", "2");
                params.put("weekEnd", "13");
                params.put("trimester", event.getTrimester());
                params.put("day", event.getDay());
                params.put("time", event.getTime());
                params.put("module", event.getModule());
                params.put("event", event.getEvent());
                params.put("matric", matric);
                params.put("fname", fname);
                params.put("sname", sname);
                params.put("cardID", cardID);
                Log.d(TAG, params.toString());
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

    public void confirmationMessage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: // Yes button clicked
                        makeAddStudentRequest(event, matricNotxt.getText().toString(), firstNametxt.getText().toString(), lastNametxt.getText().toString(), cardIDtxt.getText().toString());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE: // No button clicked // do nothing
                        Toast.makeText(AddStudentActivity.this, "Add canceled", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
