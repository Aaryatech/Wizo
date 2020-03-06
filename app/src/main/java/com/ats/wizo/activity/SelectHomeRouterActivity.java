package com.ats.wizo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.HomeRouterAdapter;
import com.ats.wizo.adapter.RouterSpinnerAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.Utils;
import com.ats.wizo.util.WiFiService;
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

import static com.ats.wizo.util.Utils.getChannelFromFrequency;

public class SelectHomeRouterActivity extends AppCompatActivity {

    private String TAG = "SelectHomeRouter Activity";

    WifiManager wifi;
    WifiScanReceiver wifiReceiver;

    List<ScanResult> wifiScanList = new ArrayList<>();
    HomeRouterAdapter homeRouterAdapter;

    public static int frequency = 0;
    private boolean isNewConfig = true;
    private ProgressDialog progressDialog;
    private Spinner spinner;

    private ScanResult scanResult;
    private EditText edPassword;
    private boolean isForConfig;
    private String wizzoSsid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_home_router);


        isForConfig = getIntent().getBooleanExtra("isForConfig", true);

        // permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000);

        }
        wifiScanList = new ArrayList<>();
        homeRouterAdapter = new HomeRouterAdapter(getApplicationContext(), wifiScanList);



        spinner = (Spinner) findViewById(R.id.spHomeRouter);
        edPassword = findViewById(R.id.edPassword);

        RouterSpinnerAdapter routerSpinnerAdapter = new RouterSpinnerAdapter(getApplicationContext(), wifiScanList);
        spinner.setAdapter(routerSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                scanResult = wifiScanList.get(position);
                Log.e("Selected WiFi ", " .. " + scanResult);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final Button btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String strPassword = edPassword.getText().toString();

                if (!strPassword.equalsIgnoreCase("")) {

                    Log.e(" ## Current Text"," "+btnNext.getText().toString());

                    if (btnNext.getText().toString().equalsIgnoreCase("Continue")) {

                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String mac = wifiInfo.getBSSID();
                        wizzoSsid = wifiInfo.getSSID();
                        Log.e("Current Wifi ", "MAC Address " + mac);
                        Log.e("Current Wifi ", "SSID " + wifiInfo.getSSID());


                                Intent configIntent = new Intent(getApplicationContext(), SelectWizoDeviceActivity.class);
                                configIntent.putExtra("wizzoSsid", wizzoSsid);

                                if (wizzoSsid.contains("#1")) {
                                    // configIntent.putExtra("isSwitch", true);
                                    configIntent.putExtra("deviceId", 1);

                                } else if(wizzoSsid.contains("#2")){
                                    //configIntent.putExtra("isSwitch", false);
                                    configIntent.putExtra("deviceId", 2);

                                }else if(wizzoSsid.contains("#3")){

                                    configIntent.putExtra("deviceId", 3);

                                }else if(wizzoSsid.contains("#4")){

                                    configIntent.putExtra("deviceId", 4);
                                }else if(wizzoSsid.contains("#5")){

                                    configIntent.putExtra("deviceId", 5);//for dimmer
                                }
                                startActivity(configIntent);


                    } else {

                        try {
                            WiFiService.connectToWPAWiFi(SelectHomeRouterActivity.this, "\"" + scanResult.SSID + "\"", "\"" + strPassword + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        frequency = scanResult.frequency;

                        Variables.e.putString("ssid", scanResult.SSID);
                        Variables.e.putString("password", strPassword);
                        Variables.e.putBoolean("isHomeRouterSet", true);
                        Variables.e.apply();

                        String resetMac = getIntent().getStringExtra("resetMac");

                        Intent intent;

                        if (isForConfig) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                btnNext.setText("Continue");
                                showSettingDialog();

                            } else {
                                startActivity(new Intent(getApplicationContext(), SelectWizoDeviceActivity.class));
                            }

                        } else {
                            intent = new Intent(getApplicationContext(), ReconfigActivity.class);
                            intent.putExtra("resetMac", resetMac);
                            startActivity(intent);
                            finish();
                        }
                    }

                } else {

                    edPassword.setError("Please Enter Password");
                }

            }
        });


    }

   /* private void

    sendChannelInfo() {

        progressDialog = new ProgressDialog(SelectHomeRouterActivity.this);
        progressDialog.setTitle("Communicating With Device");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

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
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Log.e("onResponse", " : " + response.body());

                    JSONObject object = new JSONObject(response.body().toString());

                    String status = object.getString("status");
                    if (status.equalsIgnoreCase("success")) {

                        Intent configIntent = new Intent(getApplicationContext(), DeviceConfigActivity.class);
                        configIntent.putExtra("wizzoSsid", wizzoSsid);
                        if (wizzoSsid.contains("#1")) {
                            // configIntent.putExtra("isSwitch", true);
                            configIntent.putExtra("deviceId", 1);

                        } else if(wizzoSsid.contains("#2")){
                            //configIntent.putExtra("isSwitch", false);
                            configIntent.putExtra("deviceId", 2);

                        }else if(wizzoSsid.contains("#3")){

                            configIntent.putExtra("deviceId", 3);

                        }else if(wizzoSsid.contains("#4")){

                            configIntent.putExtra("deviceId", 4);
                        }
                        startActivity(configIntent);


                    } else {
                        Toast.makeText(SelectHomeRouterActivity.this, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(SelectHomeRouterActivity.this, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();


            }
        });

    }
*/

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, " inside resume");
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiScanReceiver();

        boolean statusOfGPS = true;

        boolean mobileDataAllowed = false; // Assume disabled

        try {
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


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

            if (progressDialog == null) {
                progressDialog = new ProgressDialog(SelectHomeRouterActivity.this, R.style.MyAlertDialogStyle);
                progressDialog.setTitle("Searching WiFi Devices");
              //  progressDialog.setMessage("It May Take Around 1 Minute.\n Caution : If it takes too long\n 1.Check Your Mobile, Home Router & Wizzo Device is in Close Vicinity\n 2.Wizzo Devices Should Displayed in Your Mobile WiFi List\n Troubleshoot: Close App Fully (from Background) & Repeat Process");

                progressDialog.show();
            }

            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }

            wifi.startScan();

            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        } else if (!statusOfGPS) {

            Utils.displayPromptForEnablingGPS(SelectHomeRouterActivity.this);

        } else if (mobileDataAllowed) {

            Utils.displayPromptForDisablingData(SelectHomeRouterActivity.this);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(wifiReceiver);
        } catch (Exception e) {
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isForConfig) {

            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(), SelectExistingWizoActivity.class));

            finish();
        }

    }

    private void showPasswordAlert(final ScanResult scanResult) {
        final Dialog dialog = new Dialog(this, R.style.AppCompatAlertDialogStyle);
        dialog.setContentView(R.layout.dialog_password);


        ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        EditText edRouter = dialog.findViewById(R.id.edRouter);
        final EditText edPassword = dialog.findViewById(R.id.edPassword);
        Button btnConnectRouter = dialog.findViewById(R.id.btnConnectRouter);

        edRouter.setText("" + scanResult.SSID);

        btnConnectRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edPassword.getText().toString();
                if (!password.equalsIgnoreCase("")) {
                    try {
                        WiFiService.connectToWPAWiFi(SelectHomeRouterActivity.this, "\"" + scanResult.SSID + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    frequency = scanResult.frequency;

                    Variables.e.putString("ssid", scanResult.SSID);
                    Variables.e.putString("password", password);
                    Variables.e.apply();

                    if (isNewConfig) {


                    } else {
                        Constants.isChannelReboot = false;
                        startActivity(new Intent(getApplicationContext(), ReconfigActivity.class));
                    }

                } else {
                    edPassword.setError("Please Enter Password");
                }
            }
        });

        dialog.show();
    }

    private void showSettingDialog() {


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SelectHomeRouterActivity.this, R.style.AppCompatAlertDialogStyle);
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


    private class WifiScanReceiver extends BroadcastReceiver {
        private static final String TAG = "WiFiScanReceiver";

        public void onReceive(Context c, Intent intent) {
            List<ScanResult> allWifiList = wifi.getScanResults();


            Log.e(TAG, "Available networks " + allWifiList.toString());

            wifiScanList = new ArrayList<>();
            //  homeRouterAdapter.notifyDataSetChanged();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }


            for (ScanResult scanResult : allWifiList) {

                if (!scanResult.SSID.startsWith("Wizzo")) {
                    wifiScanList.add(scanResult);
                }

            }


            homeRouterAdapter = new HomeRouterAdapter(getApplicationContext(), wifiScanList);
            //   lvHomeRouters.setAdapter(homeRouterAdapter);

            RouterSpinnerAdapter routerSpinnerAdapter = new RouterSpinnerAdapter(getApplicationContext(), wifiScanList);
            spinner.setAdapter(routerSpinnerAdapter);

        }
    }


}
