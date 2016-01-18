package com.duncan.napierattendancesystem;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private RequestQueue mVolleyQueue;

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
            String url = "http://napierattendance-duncanmt.rhcloud.com/CardID.php?card="+CardID;

            // Request a string response from the provided URL.
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest( url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.v("onResponselogin", response.toString());
                            try {
                                JSONObject jb = (JSONObject) response.get(0);
                                String present = jb.getString("PRESENT");
                                String name = jb.getString("SPR_FNM1");
                                Log.v("onResponselogin", present);
                                Log.v("onResponselogin", name);
                                if(present.equals("1")){
                                    LoginState.setUserName(LoginActivity.this, name );
                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    LoginActivity.this.startActivity(mainIntent);
                                    LoginActivity.this.finish();
                                }else{
                                    Toast.makeText(LoginActivity.this, "Not Present", Toast.LENGTH_SHORT).show();
                                }
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("Volley Error", error.toString());
                }
            });
            mVolleyQueue.add(jsonArrayRequest);
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
}
