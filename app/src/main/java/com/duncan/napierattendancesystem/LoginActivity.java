package com.duncan.napierattendancesystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends NfcActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    private String Response;
    private String urlString = "http://napierattendance-duncanmt.rhcloud.com/CardID.php?login=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            byte [] idInBinary = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String CardID = readID(idInBinary);
            Log.v("login", CardID);
            String url = urlString+CardID;
            makeJsonArrayRequest(url);
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

    private void makeJsonArrayRequest(String url) {
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        try {
                            if(response.length()==0) {
                                Toast.makeText(getApplicationContext(),
                                        "Error: User not found",
                                        Toast.LENGTH_LONG).show();
                            }else{
                                JSONObject event = (JSONObject) response
                                        .get(0);
                                String name = event.getString("spname");
                                Log.d(TAG, "Response username = " + name);
                                LoginState.setUserName(LoginActivity.this, name);
                                Intent eventIntent = new Intent(LoginActivity.this, EventActivity.class);
                                LoginActivity.this.startActivity(eventIntent);
                                LoginActivity.this.finish();
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
