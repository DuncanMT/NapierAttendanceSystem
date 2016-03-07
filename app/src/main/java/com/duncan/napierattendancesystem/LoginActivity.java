package com.duncan.napierattendancesystem;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends NfcActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    private final String baseurl = "http://napierattendance-duncanmt.rhcloud.com";

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
            String cardID = readID(idInBinary);
            Log.v("login", cardID);
            makeLoginRequest(cardID);
        }
    }

    private void makeLoginRequest(final String cardID) {
        Uri.Builder url = Uri.parse(baseurl).buildUpon();
        url.path("/post/login.php");
        String finishedurl = url.toString();
        Log.d(TAG, finishedurl);
        StringRequest req = new StringRequest(Request.Method.POST,finishedurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            if(response.length()==0) {
                                Toast.makeText(getApplicationContext(),
                                        "Error: User not found",
                                        Toast.LENGTH_LONG).show();
                            }else{
                                JSONArray data = new JSONArray(response);
                                JSONObject user = data.getJSONObject(0);
                                String name = user.getString("spname");
                                Log.d(TAG, "Response username = " + name);
                                LoginState.setUserName(LoginActivity.this, name);
                                Intent eventIntent = new Intent(LoginActivity.this, EventActivity.class);
                                LoginActivity.this.startActivity(eventIntent);
                                LoginActivity.this.finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "VolleyJsonLoginError :"+e.getMessage());
                        }
                    }
                },CreateErrorListener()){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("login", cardID);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(req);
    }

    private Response.ErrorListener CreateErrorListener(){
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        };
    }
}
