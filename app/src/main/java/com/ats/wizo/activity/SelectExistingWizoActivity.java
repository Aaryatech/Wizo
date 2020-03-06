package com.ats.wizo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.WizoAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.Utils;
import com.ats.wizo.util.WizoDeviceConnection;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ats.wizo.activity.SelectHomeRouterActivity.frequency;
import static com.ats.wizo.util.Utils.getChannelFromFrequency;

public class SelectExistingWizoActivity extends AppCompatActivity {


    private static final String TAG = "SelectExist";
    WifiManager wifi;
    ListView lvWizoDevices;
    List<ScanResult> wizoDeviceList = new ArrayList<>();
    WizoAdapter wizoAdapter;

     ProgressDialog progressDialog ;
    WiFiChangeBrdRcr wiFiChangeBrdRcr;
    static boolean isWifiReg = false;
    static boolean isWizoReg = false;
    WizoScanReceiver wizoScanReceiver;
    static String mac = "";

    static private boolean isReset=false;

    static SelectExistingWizoActivity selectExistingWizoActivity;

    List<String> macList = new ArrayList<>();
    TextView tvErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_existing_wizo);
         progressDialog =new ProgressDialog(SelectExistingWizoActivity.this, R.style.MyAlertDialogStyle);
        Log.e(TAG, "OnCreate ");
        wizoDeviceList = new ArrayList<>();
        lvWizoDevices = findViewById(R.id.lvWizoDevices);

        selectExistingWizoActivity = this;
        if (wizoScanReceiver == null) {
            wizoScanReceiver = new SelectExistingWizoActivity.WizoScanReceiver();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            showSettingDialog();

        }


        ImageView ivRefresh =findViewById(R.id.ivRefresh);
        tvErrorMsg =findViewById(R.id.tvErrorMsg);


        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(getIntent());
                finish();
            }
        });


        DBHandler dbHandler = new DBHandler(getApplicationContext());
        macList = dbHandler.getAllScanDevices();

        lvWizoDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ScanResult scanResult = wizoDeviceList.get(position);
                String ssid = scanResult.SSID;
                mac = scanResult.BSSID;

                // ssid= String.format("\"%s\"",ssid);

                wiFiChangeBrdRcr = new SelectExistingWizoActivity.WiFiChangeBrdRcr("\"" + ssid + "\"", SelectExistingWizoActivity.this);
                registerReceiver(wiFiChangeBrdRcr, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                isWifiReg = true;

                Log.e(TAG, "Connecting to " + ssid);
                try {
                    if(!progressDialog.isShowing()) {
                        progressDialog.setTitle("Communicating with Deivce");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                WizoDeviceConnection.connectToWPAWiFi(SelectExistingWizoActivity.this, "\"" + ssid + "\"");

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        boolean statusOfGPS = true;

        boolean mobileDataAllowed = false; // Assume disabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Class cmClass = Class.forName(cm.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true);
                mobileDataAllowed = (Boolean) method.invoke(cm);
            } catch (Exception e) {
            }
        }

        if (statusOfGPS && !mobileDataAllowed) {
            if (Build.VERSION.SDK_INT <Build.VERSION_CODES.O) {

                progressDialog.setMessage("Please wait ...");
                progressDialog.show();
                progressDialog.setCancelable(false);
            }
            wifi.startScan();

            if (!isWizoReg) {
                Log.e("Registering Receiver", ".. ");

                registerReceiver(wizoScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                isWizoReg = true;
            }

        } else if (!statusOfGPS) {

            Utils.displayPromptForEnablingGPS(SelectExistingWizoActivity.this);

        } else if (mobileDataAllowed) {

            Utils.displayPromptForDisablingData(SelectExistingWizoActivity.this);

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (isWizoReg) {
                unregisterReceiver(wizoScanReceiver);
                isWizoReg = false;
            }
            if (isWifiReg) {
                unregisterReceiver(wiFiChangeBrdRcr);
                isWifiReg = false;
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            isReset = false;
            if (isWizoReg) {
                unregisterReceiver(wizoScanReceiver);
                isWizoReg = false;
            }
            if (isWifiReg) {
                unregisterReceiver(wiFiChangeBrdRcr);
                isWifiReg = false;

            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SelectExistingWizoActivity.this, HomeActivity.class));
        finish();
        try {
            selectExistingWizoActivity.finish();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public class WiFiChangeBrdRcr extends BroadcastReceiver {

        private final String TAG = "WizoBrdRcr";
        private String ssid;
        private Activity activity;


        public WiFiChangeBrdRcr(String ssid, Activity activity) {

            this.ssid = ssid;
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String connectedSSID = wifiInfo.getSSID();

                Log.e("WizoBrdRcr waiting for " + ssid, "Connected To SSID " + connectedSSID);
                if (connectedSSID.equalsIgnoreCase(ssid)) {

                    final Handler handler = new Handler();
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (!isReset) {
                                        Log.e(TAG, "Sending reset info" );

                                        isReset = true;
                                        resetDevice(activity);

                                    }
                                }

                            }, 1200);

                }

            }

        }

        private void resetDevice(final Activity activity) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            Call<JsonObject> call = myInterface.resetDevice();

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    try {
                        Log.e("onResponse", " : " + response.body());

                        JSONObject object = new JSONObject(response.body().toString());

                        String status = object.getString("status");

                        if (status.equalsIgnoreCase("success")) {

                            isReset = true;
                            wizoDeviceList.clear();

                            Toast.makeText(SelectExistingWizoActivity.this, "Device Reset Successfully !", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(activity.getApplicationContext(), SelectHomeRouterActivity.class);
                            intent.putExtra("resetMac", mac);
                            intent.putExtra("isForConfig", false);
                            activity.startActivity(intent);
                            activity.finish();
                        } else {

                            isReset = false;
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                            Toast.makeText(SelectExistingWizoActivity.this, "Failed To Connect, Try Again...", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("Fail", "" + t.getMessage());
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    isReset = false;

                    Toast.makeText(activity, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();

                }
            });

        }
    }


    private class WizoScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WizoScanReceiver";

        public void onReceive(Context c, Intent intent) {
            wizoDeviceList.clear();
            List<ScanResult> allList = wifi.getScanResults();

            Log.e(TAG, "onRcv #####");
            Log.e(TAG, "Available Wizo Device " + allList);
            Log.e(TAG, "MAC List" + macList.toString());
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            for (ScanResult scanResult : allList) {

                if (!scanResult.SSID.startsWith("Wizzo")) {
                    wizoDeviceList.add(scanResult);
                }

            }






            Log.e(TAG, "###wizoDeviceList##\n" + wizoDeviceList.toString());

            if(wizoDeviceList.isEmpty()){

                tvErrorMsg.setVisibility(View.VISIBLE);
            }else{

                tvErrorMsg.setVisibility(View.GONE);
            }
            
            try {

                wizoAdapter = new WizoAdapter(SelectExistingWizoActivity.this, wizoDeviceList);

                lvWizoDevices.setAdapter(wizoAdapter);
                wizoAdapter.notifyDataSetChanged();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }


    }


    public static SelectExistingWizoActivity getInstance() {
        return selectExistingWizoActivity;
    }

    private void showSettingDialog() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SelectExistingWizoActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Connect To Wizzo Device");
        builder.setMessage("Please connect to Wizzo device from WiFi Setting and come back");

        String positiveText = getString(R.string.go_to_wifi_setting);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });

        android.support.v7.app.AlertDialog dialog = builder.create();

        dialog.show();
    }
}
