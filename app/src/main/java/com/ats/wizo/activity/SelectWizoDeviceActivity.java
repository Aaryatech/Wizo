package com.ats.wizo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.ats.wizo.util.WizoDeviceConnection;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ats.wizo.activity.SelectHomeRouterActivity.frequency;
import static com.ats.wizo.util.Utils.getChannelFromFrequency;


public class SelectWizoDeviceActivity extends AppCompatActivity {

    private static final String TAG = "SelectWizoActivity";
    WifiManager wifi;
    ListView lvWizoDevices;
    List<ScanResult> wizoDeviceList = new ArrayList<>();
    WizoAdapter wizoAdapter;

    static ProgressDialog progressDialog;
    ProgressDialog scanProgressDialog;

    private WiFiChangeBrdRcr wiFiChangeBrdRcr;
    boolean isWifiReg = false;

    private WizoScanReceiver wizoScanReceiver;
    boolean isWizoReg = false;


    EditText edRouterName;
    EditText edPassword;
    Button btnChangeRouter;
    static boolean isPrevFinished = true;
    static boolean isChannelReboot = false;

    List<String> macList = new ArrayList<>();
    TextView tvErrorMsg;
    ImageView ivRefresh;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_wizo_device);

        lvWizoDevices = findViewById(R.id.lvWizoDevices);
        edRouterName = findViewById(R.id.edRouterName);
        edPassword = findViewById(R.id.edPassword);

        btnChangeRouter = findViewById(R.id.btnChangeRouter);

        tvErrorMsg = findViewById(R.id.tvErrorMsg);

        ivRefresh = findViewById(R.id.ivRefresh);

        wizoAdapter = new WizoAdapter(getApplicationContext(), wizoDeviceList);

        dialog = new Dialog(SelectWizoDeviceActivity.this, android.R.style.Theme_Dialog);


        if (wizoScanReceiver == null) {
            wizoScanReceiver = new WizoScanReceiver();
        }

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        macList = dbHandler.getAllScanDevices();

        String homeRouter = Variables.sh.getString("ssid", "");
        String routerPassword = Variables.sh.getString("password", "");

        edRouterName.setText(homeRouter);
        edPassword.setText(routerPassword);

        edRouterName.setEnabled(false);
        edPassword.setEnabled(false);

        btnChangeRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), SelectHomeRouterActivity.class);
                intent.putExtra("isForConfig", true);
                startActivity(intent);
                finish();

            }
        });

        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(getIntent());
                finish();

            }
        });


        lvWizoDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Log.e("Select Wizo Device", "----------------------- " + wizoDeviceList.get(position));

                    ScanResult scanResult = wizoDeviceList.get(position);
                    String ssid = scanResult.SSID;

                    // ssid= String.format("\"%s\"",ssid);

                    wiFiChangeBrdRcr = new WiFiChangeBrdRcr("\"" + ssid + "\"", SelectWizoDeviceActivity.this);
                    registerReceiver(wiFiChangeBrdRcr, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    isWifiReg = true;

                    Log.e(TAG, "Connecting to " + ssid);
                    progressDialog = new ProgressDialog(SelectWizoDeviceActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Communicating with Device");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    WizoDeviceConnection.connectToWPAWiFi(SelectWizoDeviceActivity.this, "\"" + ssid + "\"");


                } catch (Exception e) {
                    Log.e("Exception ", "-------lvWizoDevices-------- " + e.getMessage());
                    e.printStackTrace();
                }

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();
/*

        scanProgressDialog = new ProgressDialog(SelectWizoDeviceActivity.this, R.style.MyAlertDialogStyle);
        scanProgressDialog.setTitle("Configuring With Your Home Router");

        scanProgressDialog.setCancelable(false);
        scanProgressDialog.show();
*/

        showCustomProgressDialog();

        if (!isWizoReg) {
            registerReceiver(wizoScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isWizoReg = true;
        }
    }

    private void showCustomProgressDialog() {

        if (dialog.isShowing() && dialog != null)
            dialog.dismiss();

        if (!dialog.isShowing()) {

            dialog = new Dialog(SelectWizoDeviceActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_wizzo_searching);
            dialog.setCancelable(false);
            dialog.show();


        }

    }

    private void dismissCustomProgressDialog() {


        if (dialog.isShowing()) {


            dialog.dismiss();

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
        } catch (Exception e) {
        }

        try {
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
            if (isWizoReg) {
                unregisterReceiver(wizoScanReceiver);
                isWizoReg = false;
            }
        } catch (Exception e) {
        }

        try {
            if (isWifiReg) {
                unregisterReceiver(wiFiChangeBrdRcr);
                isWifiReg = false;
            }
        } catch (Exception e) {
        }
    }


    public class WiFiChangeBrdRcr extends BroadcastReceiver {

        private static final String TAG = "WizoBrdRcr";
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

                try {
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    String connectedSSID = wifiInfo.getSSID();
                    Log.e("WizoBrdRcr waiting for " + ssid, "Connected To SSID " + connectedSSID);
                } catch (Exception e) {
                    Log.e("Exception ","--------WiFiChangeBrdRcr-----------"+e.getMessage());
                }



//
//                if (connectedSSID.equalsIgnoreCase(ssid) && !isChannelReboot && isPrevFinished) {
//
//                    Log.e(TAG, "Sending channel info to" + connectedSSID);
//                    isPrevFinished = false;
//
//                    final Handler handler = new Handler();
//                    handler.postDelayed(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                   // sendChannelData(activity);
//                                }
//
//                            }, 1000);
//
//                } else if (isChannelReboot && isPrevFinished && !connectedSSID.equalsIgnoreCase(ssid)) {
//
//                    WizoDeviceConnection.connectToWPAWiFi(activity, ssid);
//
//                } else if (isChannelReboot && isPrevFinished && connectedSSID.equalsIgnoreCase(ssid)) {

                isPrevFinished = false;
                Log.e("Starting config ", "activity.. ");

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }


                Intent configIntent = new Intent(activity.getApplicationContext(), DeviceConfigActivity.class);
                configIntent.putExtra("wizzoSsid", ssid);
                String macAddress = ssid.substring(6, 23);
                Log.e("Mac Address ", "is.. " + macAddress.toString());
                configIntent.putExtra("macAddress", macAddress);


                if (ssid.contains("#1")) {
                    // configIntent.putExtra("isSwitch", true);
                    configIntent.putExtra("deviceId", 1);

                } else if (ssid.contains("#2")) {
                    //configIntent.putExtra("isSwitch", false);
                    configIntent.putExtra("deviceId", 2);

                } else if (ssid.contains("#3")) {

                    configIntent.putExtra("deviceId", 3);

                } else if (ssid.contains("#4")) {

                    configIntent.putExtra("deviceId", 4);
                }else if (ssid.contains("#5")) {

                    configIntent.putExtra("deviceId", 5);
                }
                activity.startActivity(configIntent);


            }

        }

        private void sendChannelData(final Activity activity) {

            String channel = String.valueOf(getChannelFromFrequency(frequency));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            Log.e("key " + Constants.authKey, " channel: " + channel);


            String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.sendChannel(channel);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    try {
                        Log.e("onResponse", " : " + response.body());

                        JSONObject object = new JSONObject(response.body().toString());

                        String status = object.getString("status");
                        if (status.equalsIgnoreCase("success")) {
                            isChannelReboot = true;
                            isPrevFinished = true;
                        } else {

                            isChannelReboot = false;
                            isPrevFinished = true;
                            Toast.makeText(activity, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();
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
                    isChannelReboot = false;
                    isPrevFinished = true;
                    Toast.makeText(activity, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();


                }
            });

        }
    }


    private class WizoScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WizoScanReceiver";

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> allList = wifi.getScanResults();

            Log.e(TAG, "Scan Mac List " + macList.toString());
            Log.e(TAG, "Available Wizzo Device " + allList);
            wizoDeviceList = new ArrayList<>();

            for (ScanResult scanResult : allList) {

                if (scanResult.SSID.startsWith("Wizzo")) {
                    wizoDeviceList.add(scanResult);
                }

            }

          /*  if (scanProgressDialog.isShowing()) {
                scanProgressDialog.dismiss();
            }*/

            dismissCustomProgressDialog();

            if (wizoDeviceList.isEmpty()) {
                tvErrorMsg.setVisibility(View.VISIBLE);
            } else {
                tvErrorMsg.setVisibility(View.GONE);
            }

            try {
                wizoAdapter = new WizoAdapter(getApplicationContext(), wizoDeviceList);
                lvWizoDevices.setAdapter(wizoAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
