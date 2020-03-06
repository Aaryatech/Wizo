package com.ats.wizo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.WizoAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.WiFiService;
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

public class ReconfigActivity extends AppCompatActivity {

    private static final String TAG = "ReconfigActivity";


    WifiManager wifi;

    ListView lvWizoDevices;
    List<ScanResult> wizoDeviceList = new ArrayList<>();
    List<String> scanDeviceList = new ArrayList<>();
    WizoAdapter wizoAdapter;
    private boolean isWizoReg = false;
    private WizoScanReceiver wizoScanReceiver;
    private boolean isWifiReg = false;
    private WiFiChangeBrdRcr wiFiChangeBrdRcr;

    ProgressDialog scanProgressDialog;
    static ProgressDialog progressDialog;
    static ProgressDialog reConfProgressDialog;
    private boolean isAlreadyScanning = false;
    Handler handler = new Handler();
    int status = 0;

    static Button btnReconfigure;
    static String ssid;
    boolean isPrevFinished = true;
    boolean isTouched = false;
    TextView tvErrorMsg;
    ImageView ivRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconfig);

        lvWizoDevices = findViewById(R.id.lvWizoDevices);
        btnReconfigure = findViewById(R.id.btnReconfigure);
        btnReconfigure.setVisibility(View.INVISIBLE);

        wizoDeviceList = new ArrayList<>();
        wizoAdapter = new WizoAdapter(getApplicationContext(), wizoDeviceList);

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        scanDeviceList = dbHandler.getAllScanDevices();
        tvErrorMsg = findViewById(R.id.tvErrorMsg);

        ivRefresh = findViewById(R.id.ivRefresh);

        scanProgressDialog = new ProgressDialog(ReconfigActivity.this, R.style.MyAlertDialogStyle);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            showSettingDialog();

        }

        lvWizoDevices.setAdapter(wizoAdapter);

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

                ScanResult scanResult = wizoDeviceList.get(position);
                ssid = scanResult.SSID;

                if (!Constants.isChannelReboot) {
                    wiFiChangeBrdRcr = new WiFiChangeBrdRcr("\"" + ssid + "\"", ReconfigActivity.this);
                    registerReceiver(wiFiChangeBrdRcr, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    isWifiReg = true;

                    Log.e(TAG, "Connecting to " + ssid);
                    progressDialog = new ProgressDialog(ReconfigActivity.this, R.style.MyAlertDialogStyle);
                    progressDialog.setTitle("Connecting...");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    WizoDeviceConnection.connectToWPAWiFi(ReconfigActivity.this, "\"" + ssid + "\"");

                } else {

                    Toast.makeText(ReconfigActivity.this, "Please Press Reconfigure Button", Toast.LENGTH_SHORT).show();

                }
            }
        });


        btnReconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (isWifiReg) {
                        unregisterReceiver(wiFiChangeBrdRcr);
                        isWifiReg = false;
                    }

                    wiFiChangeBrdRcr = new WiFiChangeBrdRcr("\"" + ssid + "\"", ReconfigActivity.this);
                    registerReceiver(wiFiChangeBrdRcr, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                    isWifiReg = true;
                    isTouched = true;
                    Log.e(TAG, "Connecting to " + ssid);
                    reConfProgressDialog = new ProgressDialog(ReconfigActivity.this, R.style.MyAlertDialogStyle);
                    reConfProgressDialog.setTitle("Reconfiguring...");
                    reConfProgressDialog.setMessage("Please Wait...");
                    reConfProgressDialog.setCancelable(false);
                    reConfProgressDialog.show();

                    WizoDeviceConnection.connectToWPAWiFi(ReconfigActivity.this, "\"" + ssid + "\"");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("Is channel", " send " + Constants.isChannelReboot);


        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();

        if (wizoScanReceiver == null) {
            wizoScanReceiver = new WizoScanReceiver();
        }


        if (!isWizoReg) {
            registerReceiver(wizoScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            );

            isWizoReg = true;
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {


            try {
                scanProgressDialog.setMessage("Please Wait...");
                scanProgressDialog.setCancelable(false);
                scanProgressDialog.show();

            } catch (Exception e) {

            }

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

                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String connectedSSID = wifiInfo.getSSID();

                Log.e("WizoBrdRcr waiting for " + ssid, "Connected To SSID " + connectedSSID);

               /* if (connectedSSID.equalsIgnoreCase(ssid) && !Constants.isChannelReboot && isPrevFinished) {

                    Log.e(TAG, "Sending channel info to" + connectedSSID);
                    progressDialog.setTitle("Communicating with device");

                    isPrevFinished = false;

                    final Handler handler = new Handler();
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //   sendChannelData(activity);

                                    reConfigSwitchDevice(activity);
                                }

                            }, 1000);

                } else if (Constants.isChannelReboot && isPrevFinished && connectedSSID.equalsIgnoreCase(ssid) && isTouched) {

                    Log.e("Reconfig ", "method.. ");
                    isPrevFinished = false;

                    final Handler handler = new Handler();
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    reConfigSwitchDevice(activity);
                                }

                            }, 1000);

                }*/

                if (connectedSSID.equalsIgnoreCase(ssid) && isPrevFinished) {
                    isPrevFinished = false;
                    final Handler handler = new Handler();
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //   sendChannelData(activity);

                                    reConfigSwitchDevice(activity);
                                }

                            }, 1000);
                }
            }

        }

        private void reConfigSwitchDevice(final Activity activity) {

            try {

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://192.168.137.29")
                        .addConverterFactory(GsonConverterFactory.create()).build();

                MyInterface myInterface = retrofit.create(MyInterface.class);

                String channel = String.valueOf(getChannelFromFrequency(frequency));


                String ssid = Variables.sh.getString("ssid", "");
                String password = Variables.sh.getString("password", "");


                Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);

                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        Log.e("OnResponse ", " .. " + response.body());


                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());

                            final String ip = jsonObject.getString("ip");
                            final String mac = jsonObject.getString("mac");

                            try {
                                String ssid = Variables.sh.getString("ssid", "");
                                String password = Variables.sh.getString("password", "");

                                WiFiService.connectToWPAWiFi(activity, "\"" + ssid + "\"", "\"" + password + "\"");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            final Handler handler = new Handler();
                            handler.postDelayed(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            try {

                                                DBHandler dbHandler = new DBHandler(activity.getApplicationContext());
                                                dbHandler.updateDeviceIP(ip, mac);
                                                Log.e("Ip ", "updated ");
                                                registerDeviceToServer();
                                                isPrevFinished = true;
                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Constants.isChannelReboot = false;

                                        }

                                    }, 10000);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                        Log.e("OnFailure ", " .. " + t.getMessage());

                        try {

                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            isPrevFinished = true;


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(activity, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (Exception e) {
                isPrevFinished = true;

                e.printStackTrace();
            }


        }


        private void sendChannelData(final Activity activity) {

            String channel = String.valueOf(getChannelFromFrequency(frequency));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            Log.e("key " + Constants.authKey, " channel: " + channel);

            // s p k c

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
                            isPrevFinished = true;
                            Constants.isChannelReboot = true;


                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }


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

                            startActivity(new Intent(getIntent()));
                            finish();


                            //   Toast.makeText(ReconfigActivity.this, "Press Reconfigure Button When Wizzo device is available", Toast.LENGTH_LONG).show();

                        } else {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            isPrevFinished = true;
                            Constants.isChannelReboot = false;
                            Toast.makeText(activity, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        isPrevFinished = true;
                        Constants.isChannelReboot = false;
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("Fail", "" + t.getMessage());
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Constants.isChannelReboot = false;
                    isPrevFinished = true;
                    Toast.makeText(activity, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();

                }
            });

        }
    }


    private void registerDeviceToServer() {

        final ProgressDialog progressDialog2 = new ProgressDialog(ReconfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog2.setTitle("Loading");
        progressDialog2.setMessage("Please Wait...");
        progressDialog2.show();

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Device> allDeviceList = dbHandler.getAllDevices();
        List<DataUploadDevices> deviceList = new ArrayList<>();

        for (Device device : allDeviceList) {

            DataUploadDevices dataUploadDevices = new DataUploadDevices(Integer.parseInt(Constants.userId), device.getDevIp(), device.getDevMac(), device.getDevCaption(), device.getDevType(), device.getDevPosition(), device.getDevSsid(), device.getDevRoomId(), device.getDevIsUsed());

            deviceList.add(dataUploadDevices);
        }

        Call<JsonObject> call = Constants.myInterface.uploadDeviceData(deviceList);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog2.isShowing()) {
                        progressDialog2.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    if (!jsonObject.getBoolean("error")) {

                        Toast.makeText(ReconfigActivity.this, "Device Reconfigured & Uploaded Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ReconfigActivity.this, "Device Reconfigured But Failed To Upload, Please Try Uploading Data From Setting Menu", Toast.LENGTH_SHORT).show();

                    }

//                    try {
//                        if (reConfProgressDialog.isShowing()) {
//                            reConfProgressDialog.dismiss();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    Constants.isChannelReboot = false;
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);

                    intent.putExtra("isFromConfig", true);
                    startActivity(intent);
                    finish();


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Data upload ", "failed " + t.getMessage());
                Toast.makeText(ReconfigActivity.this, "Failed, Please try uploading data from setting menu", Toast.LENGTH_SHORT).show();


                if (reConfProgressDialog.isShowing()) {
                    reConfProgressDialog.dismiss();
                }

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);

                intent.putExtra("isFromConfig", true);
                startActivity(intent);
                finish();

            }
        });


    }


    private class WizoScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WizoScanReceiver";

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> allList = wifi.getScanResults();
            Log.e(TAG, "Available Wizo Device " + allList);

            wizoDeviceList.clear();


            String resetMac = getIntent().getStringExtra("resetMac");

            try {

                if (scanProgressDialog.isShowing()) {
                    scanProgressDialog.dismiss();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            for (ScanResult scanResult : allList) {
                if (scanResult.BSSID.equalsIgnoreCase(resetMac)) {
                    wizoDeviceList.add(scanResult);
                }
            }

            if (wizoDeviceList.isEmpty()) {

                tvErrorMsg.setVisibility(View.VISIBLE);
                wizoAdapter.notifyDataSetChanged();

            } else {

                tvErrorMsg.setVisibility(View.GONE);

                if (Constants.isChannelReboot) {
                    btnReconfigure.setVisibility(View.VISIBLE);
                }

                try {
                    wizoAdapter.notifyDataSetChanged();

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

    private void showSettingDialog() {


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ReconfigActivity.this, R.style.AppCompatAlertDialogStyle);
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
