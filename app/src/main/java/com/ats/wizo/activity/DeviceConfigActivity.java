package com.ats.wizo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.Room;
import com.ats.wizo.mqtt.ConfigMqtt;
import com.ats.wizo.mqtt.MqttConnection;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.ConnectivityChangeReceiver;
import com.ats.wizo.util.WiFiService;
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

import static com.ats.wizo.activity.HomeActivity.topicList;
import static com.ats.wizo.activity.ReconfigActivity.ssid;
import static com.ats.wizo.activity.SelectExistingWizoActivity.mac;
import static com.ats.wizo.activity.SelectHomeRouterActivity.frequency;
import static com.ats.wizo.common.Variables.isMQTTConnected;
import static com.ats.wizo.common.Variables.isStatusReceived;
import static com.ats.wizo.common.Variables.subscribedTopics;
import static com.ats.wizo.util.Utils.getChannelFromFrequency;

public class DeviceConfigActivity extends AppCompatActivity {


    LinearLayout llMqttHandler;

    Button btnConfig, btnReHandle;
    Spinner spinnerRoom;
    ProgressDialog progressDialog;
    List<String> roomadapterList;
    List<Room> roomList;

    String roomName = "";
    String macAddress = null;
    private String cap1 = "Switch 1";
    private String cap2 = "Switch 2";
    private String cap3 = "Switch 3";
    private String cap4 = "Switch 4";
    private String cap5 = "Switch 5";
    private String cap6 = "Switch 6";
    private String cap7 = "Switch 7";
    private String cap8 = "Switch 8";

    int roomId = 0;
    int totalAttempts = 0;
    int deviceType = 0;


    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_device_config);


        llMqttHandler = findViewById(R.id.llMqttHandler);


        btnConfig = findViewById(R.id.btnConfig);

        btnReHandle = findViewById(R.id.btnReHandle);

        spinnerRoom = findViewById(R.id.spRoom);

        roomList = getRoomData();

        roomadapterList = new ArrayList<>();
        roomadapterList.add("Please Select Room");

        for (int i = 0; i < roomList.size(); i++) {

            roomadapterList.add(roomList.get(i).getRoomName());

        }

        dialog = new Dialog(DeviceConfigActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_for_config);
        dialog.setCancelable(false);


        // getting device type from intent

        deviceType = getIntent().getIntExtra("deviceId", 1);

        macAddress = getIntent().getStringExtra("macAddress");


        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item, roomadapterList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(spinnerAdapter);

        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (deviceType) {

                    case 1:
                        handleWizzo4s();
                        break;

                    case 2:
                        handleWizzoPowerSwitch();
                        break;

                    case 3:
                        handleWizzo5s();
                        break;

                    case 4:
                        handleWizzo8s();
                        break;

                    case 5:
                        handleWizzo4s2d();
                        break;

                }
            }
        });


        btnReHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (totalAttempts <= 3) {

                    tryToHandleFromMqtt();


                } else {
                    Intent intent = new Intent(getApplicationContext(), SelectHomeRouterActivity.class);
                    intent.putExtra("isForConfig", true);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }


    private void handleWizzo4s() {


        boolean isValid = true;


        if (spinnerRoom.getSelectedItemPosition() == 0) {

            Toast.makeText(DeviceConfigActivity.this, "Please Select Room", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {

            int selectedPos = spinnerRoom.getSelectedItemPosition();

            roomName = roomadapterList.get(selectedPos);

            roomId = roomList.get(0).getRoomId();

            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                    roomId = roomList.get(i).getRoomId();
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wizzoSsid = wifiInfo.getSSID();

                if (getIntent().getStringExtra("wizzoSsid").equalsIgnoreCase(wizzoSsid)) {
                    configNewSwitchDevice();
                } else {
                    showSettingDialog();
                }

            } else {
                configNewSwitchDevice();
            }
        }

    }

    private void handleWizzoPowerSwitch() {

        boolean isValid = true;

        if (spinnerRoom.getSelectedItemPosition() == 0) {

            Toast.makeText(DeviceConfigActivity.this, "Please Select Room", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {

            int selectedPos = spinnerRoom.getSelectedItemPosition();

            roomName = roomadapterList.get(selectedPos);

            int roomId = roomList.get(0).getRoomId();

            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                    roomId = roomList.get(i).getRoomId();
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wizzoSsid = wifiInfo.getSSID();

                if (getIntent().getStringExtra("wizzoSsid").equalsIgnoreCase(wizzoSsid)) {
                    configNewPS(roomId);

                } else {
                    showSettingDialog();
                }

            } else {
                configNewPS(roomId);
            }
        }


    }

    private void configNewPS(final int roomId) {

        try {

            dialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            String channel = String.valueOf(getChannelFromFrequency(frequency));


            final String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);


            Log.e("Now Sending Info ", " .. ");


            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    Log.e("OnResponse ", " .. " + response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String ip = jsonObject.getString("ip");
                        String mac = jsonObject.getString("mac");

                        Device device1 = new Device(cap1, ip, mac, ssid, 1, roomId, 1, 1);

                        DBHandler dbHandler = new DBHandler(getApplicationContext());

                        dbHandler.addNewDevice(device1);

                        try {
                            String ssid = Variables.sh.getString("ssid", "");
                            String password = Variables.sh.getString("password", "");

                            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                        registerDeviceToServer();

                                    }

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());
                    btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            totalAttempts = 0;
                            Toast.makeText(DeviceConfigActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();
                            llMqttHandler.setVisibility(View.VISIBLE);


                        }
                    }, (3000));

                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void handleWizzo5s() {


        boolean isValid = true;

        if (spinnerRoom.getSelectedItemPosition() == 0) {

            Toast.makeText(DeviceConfigActivity.this, "Please Select Room", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {

            int selectedPos = spinnerRoom.getSelectedItemPosition();

            roomName = roomadapterList.get(selectedPos);

            int roomId = roomList.get(0).getRoomId();

            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                    roomId = roomList.get(i).getRoomId();
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wizzoSsid = wifiInfo.getSSID();

                if (getIntent().getStringExtra("wizzoSsid").equalsIgnoreCase(wizzoSsid)) {
                    configNew5S(roomId);

                } else {
                    showSettingDialog();
                }

            } else {
                configNew5S(roomId);
            }
        }


    }

    private void configNew5S(final int roomId) {

        try {

            dialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            String channel = String.valueOf(getChannelFromFrequency(frequency));


            final String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);


            Log.e("Now Sending Info ", " .. ");


            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    Log.e("OnResponse ", " .. " + response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String ip = jsonObject.getString("ip");
                        String mac = jsonObject.getString("mac");

                        Device device1 = new Device(cap1, ip, mac, ssid, 1, roomId, 1, 1);
                        Device device2 = new Device(cap2, ip, mac, ssid, 2, roomId, 2, 1);
                        Device device3 = new Device(cap3, ip, mac, ssid, 3, roomId, 3, 1);
                        Device device4 = new Device(cap4, ip, mac, ssid, 4, roomId, 4, 1);
                        // Device device5 = new Device(cap5, ip, mac, ssid, 5, roomId, 5, 1);
                        Device device6 = new Device("Fan", ip, mac, ssid, 678, roomId, 6, 1);

                        DBHandler dbHandler = new DBHandler(getApplicationContext());

                        dbHandler.addNewDevice(device1);
                        dbHandler.addNewDevice(device2);
                        dbHandler.addNewDevice(device3);
                        dbHandler.addNewDevice(device4);
                        // dbHandler.addNewDevice(device5);
                        dbHandler.addNewDevice(device6);

                        try {
                            String ssid = Variables.sh.getString("ssid", "");
                            String password = Variables.sh.getString("password", "");

                            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                        registerDeviceToServer();

                                    }

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());
                    btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            totalAttempts = 0;
                            Toast.makeText(DeviceConfigActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();
                            llMqttHandler.setVisibility(View.VISIBLE);


                        }
                    }, (3000));
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    private void handleWizzo8s() {


        boolean isValid = true;


        if (spinnerRoom.getSelectedItemPosition() == 0) {

            Toast.makeText(DeviceConfigActivity.this, "Please Select Room", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {

            int selectedPos = spinnerRoom.getSelectedItemPosition();

            roomName = roomadapterList.get(selectedPos);

            int roomId = roomList.get(0).getRoomId();

            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                    roomId = roomList.get(i).getRoomId();
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wizzoSsid = wifiInfo.getSSID();

                if (getIntent().getStringExtra("wizzoSsid").equalsIgnoreCase(wizzoSsid)) {
                    configNew8S(roomId);

                } else {
                    showSettingDialog();
                }

            } else {
                configNew8S(roomId);
            }
        }


    }

    private void configNew8S(final int roomId) {


        try {
            dialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            String channel = String.valueOf(getChannelFromFrequency(frequency));


            final String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);


            Log.e("Now Sending Info ", " .. ");


            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    Log.e("OnResponse ", " .. " + response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String ip = jsonObject.getString("ip");
                        String mac = jsonObject.getString("mac");

                        Device device1 = new Device(cap1, ip, mac, ssid, 1, roomId, 1, 1);
                        Device device2 = new Device(cap2, ip, mac, ssid, 2, roomId, 2, 1);
                        Device device3 = new Device(cap3, ip, mac, ssid, 3, roomId, 3, 1);
                        Device device4 = new Device(cap4, ip, mac, ssid, 4, roomId, 4, 1);
                        Device device5 = new Device(cap5, ip, mac, ssid, 5, roomId, 5, 1);
                        Device device6 = new Device(cap6, ip, mac, ssid, 6, roomId, 6, 1);
                        Device device7 = new Device(cap7, ip, mac, ssid, 7, roomId, 7, 1);
                        Device device8 = new Device(cap8, ip, mac, ssid, 8, roomId, 8, 1);

                        DBHandler dbHandler = new DBHandler(getApplicationContext());

                        dbHandler.addNewDevice(device1);
                        dbHandler.addNewDevice(device2);
                        dbHandler.addNewDevice(device3);
                        dbHandler.addNewDevice(device4);
                        dbHandler.addNewDevice(device5);
                        dbHandler.addNewDevice(device6);
                        dbHandler.addNewDevice(device7);
                        dbHandler.addNewDevice(device8);

                        try {
                            String ssid = Variables.sh.getString("ssid", "");
                            String password = Variables.sh.getString("password", "");

                            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                        registerDeviceToServer();

                                    }

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());
                    btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            totalAttempts = 0;
                            Toast.makeText(DeviceConfigActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();
                            llMqttHandler.setVisibility(View.VISIBLE);


                        }
                    }, (3000));

                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    private void handleWizzo4s2d() {


        boolean isValid = true;

        if (spinnerRoom.getSelectedItemPosition() == 0) {

            Toast.makeText(DeviceConfigActivity.this, "Please Select Room", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {

            int selectedPos = spinnerRoom.getSelectedItemPosition();

            roomName = roomadapterList.get(selectedPos);

            int roomId = roomList.get(0).getRoomId();

            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                    roomId = roomList.get(i).getRoomId();
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wizzoSsid = wifiInfo.getSSID();

                if (getIntent().getStringExtra("wizzoSsid").equalsIgnoreCase(wizzoSsid)) {
                    configNew4s2d(roomId);

                } else {
                    showSettingDialog();
                }

            } else {
                configNew4s2d(roomId);
            }
        }


    }

    private void configNew4s2d(final int roomId) {

        try {

            dialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            String channel = String.valueOf(getChannelFromFrequency(frequency));


            final String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);


            Log.e("Now Sending Info ", " .. ");


            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    Log.e("OnResponse ", " .. " + response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String ip = jsonObject.getString("ip");
                        String mac = jsonObject.getString("mac");

                        Device device1 = new Device(cap1, ip, mac, ssid, 1, roomId, 1, 1);
                        Device device2 = new Device(cap2, ip, mac, ssid, 2, roomId, 2, 1);
                        Device device3 = new Device(cap3, ip, mac, ssid, 3, roomId, 3, 1);
                        Device device4 = new Device(cap4, ip, mac, ssid, 4, roomId, 4, 1);
                        // Device device5 = new Device(cap5, ip, mac, ssid, 5, roomId, 5, 1);
                        Device device6 = new Device("Dimmer 1", ip, mac, ssid, 12, roomId, 6, 1);
                        Device device7 = new Device("Dimmer 2", ip, mac, ssid, 13, roomId, 7, 1);

                        DBHandler dbHandler = new DBHandler(getApplicationContext());

                        dbHandler.addNewDevice(device1);
                        dbHandler.addNewDevice(device2);
                        dbHandler.addNewDevice(device3);
                        dbHandler.addNewDevice(device4);
                        // dbHandler.addNewDevice(device5);
                        dbHandler.addNewDevice(device6);
                        dbHandler.addNewDevice(device7);

                        try {
                            String ssid = Variables.sh.getString("ssid", "");
                            String password = Variables.sh.getString("password", "");

                            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                        registerDeviceToServer();

                                    }

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());
                    btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            totalAttempts = 0;
                            Toast.makeText(DeviceConfigActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();
                            llMqttHandler.setVisibility(View.VISIBLE);


                        }
                    }, (3000));
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    private void showSettingDialog() {


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(DeviceConfigActivity.this, R.style.AppCompatAlertDialogStyle);
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

    private void configNewSwitchDevice() {

        try {
            dialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

            String channel = String.valueOf(getChannelFromFrequency(frequency));


            final String ssid = Variables.sh.getString("ssid", "");
            String password = Variables.sh.getString("password", "");


            Call<JsonObject> call = myInterface.getConfig(ssid, password, Constants.authKey);


            Log.e("Now Sending Info ", " .. ");


            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    Log.e("OnResponse ", " .. " + response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String ip = jsonObject.getString("ip");
                        String mac = jsonObject.getString("mac");

                        Device device1 = new Device(cap1, ip, mac, ssid, 1, roomId, 1, 1);
                        Device device2 = new Device(cap2, ip, mac, ssid, 2, roomId, 2, 1);
                        Device device3 = new Device(cap3, ip, mac, ssid, 3, roomId, 3, 1);
                        Device device4 = new Device(cap4, ip, mac, ssid, 4, roomId, 4, 1);

                        DBHandler dbHandler = new DBHandler(getApplicationContext());

                        dbHandler.addNewDevice(device1);
                        dbHandler.addNewDevice(device2);
                        dbHandler.addNewDevice(device3);
                        dbHandler.addNewDevice(device4);

                        try {
                            String ssid = Variables.sh.getString("ssid", "");
                            String password = Variables.sh.getString("password", "");

                            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }

                                        registerDeviceToServer();

                                    }

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                  /*  btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            totalAttempts = 0;

                            llMqttHandler.setVisibility(View.VISIBLE);

                        }
                    }, (3000));
*/

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());
                    btnConfig.setVisibility(View.GONE);


                    try {
                        String ssid = Variables.sh.getString("ssid", "");
                        String password = Variables.sh.getString("password", "");

                        WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            totalAttempts = 0;
                            Toast.makeText(DeviceConfigActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();
                            llMqttHandler.setVisibility(View.VISIBLE);


                        }
                    }, (3000));
                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    private void tryToHandleFromMqtt() {

        Log.e("DEV CONFIG ACT","-----------------------------------------------------------------tryToHandleFromMqtt");

        topicList = new ArrayList<>();
        subscribedTopics = new ArrayList<>();
        topicList.add(macAddress);
        isStatusReceived = false;

        if (!isMQTTConnected || subscribedTopics.size() < topicList.size()) {
            ConfigMqtt.initializeMQTT(getApplicationContext(), topicList);
        }

        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Please Restart Wizzo Device");
        progressDialog.show();


        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isStatusReceived) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();


                    switch (deviceType) {

                        case 1:
                            dumpData4s();
                            break;

                        case 2:

                            dumpDataPS();
                            break;

                        case 3:

                            dumpData5s();
                            break;

                        case 4:

                            dumpData8s();
                            break;

                        case 5:

                            dumpData4s2d();
                            break;

                    }


                    Toast.makeText(DeviceConfigActivity.this, "Configured successfully", Toast.LENGTH_SHORT).show();
                } else {
                    isStatusReceived = false;
                    btnReHandle.setText("Retry");

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    Toast.makeText(DeviceConfigActivity.this, "Retry again...", Toast.LENGTH_SHORT).show();
                    totalAttempts = totalAttempts + 1;

                }
            }
        }, (25000));


    }

    public void dumpData4s() {


        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        final String ssid = Variables.sh.getString("ssid", "");


        Device device1 = new Device(cap1, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 1, roomId, 1, 1);
        Device device2 = new Device(cap2, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 2, roomId, 2, 1);
        Device device3 = new Device(cap3, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 3, roomId, 3, 1);
        Device device4 = new Device(cap4, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 4, roomId, 4, 1);

        DBHandler dbHandler = new DBHandler(getApplicationContext());


        List<Device> deviceList = dbHandler.getAllDevicesByDevMac(macAddress);

        Log.e(" ## Prev. DeviceList ", "  " + deviceList.toString());


        if (deviceList.isEmpty()) {
            dbHandler.addNewDevice(device1);
            dbHandler.addNewDevice(device2);
            dbHandler.addNewDevice(device3);
            dbHandler.addNewDevice(device4);
        } else {

            for (Device device : deviceList) {

                dbHandler.updateDeviceData(device.getDevIp(), device.getDevRoomId(), device.getDevId());
            }

        }
        try {

            String password = Variables.sh.getString("password", "");

            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        registerDeviceToServer();

                    }

                }, 10000);

    }


    public void dumpDataPS() {


        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        final String ssid = Variables.sh.getString("ssid", "");


        Device device1 = new Device(cap1, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 1, roomId, 1, 1);


        DBHandler dbHandler = new DBHandler(getApplicationContext());


        List<Device> deviceList = dbHandler.getAllDevicesByDevMac(macAddress);

        Log.e(" ## Prev. DeviceList ", "  " + deviceList.toString());


        if (deviceList.isEmpty()) {
            dbHandler.addNewDevice(device1);


        } else {

            for (Device device : deviceList) {

                dbHandler.updateDeviceData(device.getDevIp(), device.getDevRoomId(), device.getDevId());
            }

        }
        try {

            String password = Variables.sh.getString("password", "");

            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        registerDeviceToServer();

                    }

                }, 10000);

    }


    public void dumpData5s() {


        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        final String ssid = Variables.sh.getString("ssid", "");


        Device device1 = new Device(cap1, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 1, roomId, 1, 1);
        Device device2 = new Device(cap2, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 2, roomId, 2, 1);
        Device device3 = new Device(cap3, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 3, roomId, 3, 1);
        Device device4 = new Device(cap4, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 4, roomId, 4, 1);
        Device device5 = new Device(cap5, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 5, roomId, 5, 1);

        DBHandler dbHandler = new DBHandler(getApplicationContext());


        List<Device> deviceList = dbHandler.getAllDevicesByDevMac(macAddress);

        Log.e(" ## Prev. DeviceList ", "  " + deviceList.toString());


        if (deviceList.isEmpty()) {
            dbHandler.addNewDevice(device1);
            dbHandler.addNewDevice(device2);
            dbHandler.addNewDevice(device3);
            dbHandler.addNewDevice(device4);
            dbHandler.addNewDevice(device5);
        } else {

            for (Device device : deviceList) {

                dbHandler.updateDeviceData(device.getDevIp(), device.getDevRoomId(), device.getDevId());
            }

        }
        try {

            String password = Variables.sh.getString("password", "");

            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        registerDeviceToServer();

                    }

                }, 10000);

    }


    public void dumpData8s() {


        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        final String ssid = Variables.sh.getString("ssid", "");


        Device device1 = new Device(cap1, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 1, roomId, 1, 1);
        Device device2 = new Device(cap2, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 2, roomId, 2, 1);
        Device device3 = new Device(cap3, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 3, roomId, 3, 1);
        Device device4 = new Device(cap4, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 4, roomId, 4, 1);
        Device device5 = new Device(cap5, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 5, roomId, 5, 1);
        Device device6 = new Device(cap6, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 6, roomId, 6, 1);
        Device device7 = new Device(cap7, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 7, roomId, 7, 1);
        Device device8 = new Device(cap8, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 8, roomId, 8, 1);

        DBHandler dbHandler = new DBHandler(getApplicationContext());


        List<Device> deviceList = dbHandler.getAllDevicesByDevMac(macAddress);

        Log.e(" ## Prev. DeviceList ", "  " + deviceList.toString());


        if (deviceList.isEmpty()) {
            dbHandler.addNewDevice(device1);
            dbHandler.addNewDevice(device2);
            dbHandler.addNewDevice(device3);
            dbHandler.addNewDevice(device4);
            dbHandler.addNewDevice(device5);
            dbHandler.addNewDevice(device6);
            dbHandler.addNewDevice(device7);
            dbHandler.addNewDevice(device8);
        } else {

            for (Device device : deviceList) {

                dbHandler.updateDeviceData(device.getDevIp(), device.getDevRoomId(), device.getDevId());
            }

        }
        try {

            String password = Variables.sh.getString("password", "");

            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        registerDeviceToServer();

                    }

                }, 10000);

    }

    public void dumpData4s2d() {


        progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        final String ssid = Variables.sh.getString("ssid", "");


        Device device1 = new Device(cap1, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 1, roomId, 1, 1);
        Device device2 = new Device(cap2, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 2, roomId, 2, 1);
        Device device3 = new Device(cap3, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 3, roomId, 3, 1);
        Device device4 = new Device(cap4, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 4, roomId, 4, 1);
        Device device5 = new Device(cap5, ConfigMqtt.ip, ConfigMqtt.mac, ssid, 5, roomId, 5, 1);

        DBHandler dbHandler = new DBHandler(getApplicationContext());


        List<Device> deviceList = dbHandler.getAllDevicesByDevMac(macAddress);

        Log.e(" ## Prev. DeviceList ", "  " + deviceList.toString());


        if (deviceList.isEmpty()) {
            dbHandler.addNewDevice(device1);
            dbHandler.addNewDevice(device2);
            dbHandler.addNewDevice(device3);
            dbHandler.addNewDevice(device4);
            dbHandler.addNewDevice(device5);
        } else {

            for (Device device : deviceList) {

                dbHandler.updateDeviceData(device.getDevIp(), device.getDevRoomId(), device.getDevId());
            }

        }
        try {

            String password = Variables.sh.getString("password", "");

            WiFiService.connectToWPAWiFi(DeviceConfigActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        registerDeviceToServer();

                    }

                }, 10000);

    }




    private List<Room> getRoomData() {

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Room> roomList = dbHandler.getAllRooms();

        return roomList;
    }


    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(DeviceConfigActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Device> allDeviceList = dbHandler.getAllDevices();
        List<DataUploadDevices> deviceList = new ArrayList<>();

        for (Device device : allDeviceList) {

            DataUploadDevices dataUploadDevices = new DataUploadDevices(Integer.parseInt(Constants.userId), device.getDevIp(), device.getDevMac(), device.getDevCaption(), device.getDevType(), device.getDevPosition(), device.getDevSsid(), device.getDevRoomId(), device.getDevIsUsed());

            deviceList.add(dataUploadDevices);
        }


        Log.e(" ## DeviceList ", " upload " + deviceList.toString());

        Call<JsonObject> call = Constants.myInterface.uploadDeviceData(deviceList);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    Log.e("Config Response", " " + jsonObject.toString());

                    if (!jsonObject.getBoolean("error")) {

                        Toast.makeText(DeviceConfigActivity.this, "Device Added Successfully Under " + roomName, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(DeviceConfigActivity.this, "Device Added But Failed To Upload, Please Try Uploading Data From Setting Menu", Toast.LENGTH_SHORT).show();
                    }


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
                Toast.makeText(DeviceConfigActivity.this, "Device Added But Failed To Upload, Please Try Uploading Data From Setting Menu", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);

                intent.putExtra("isFromConfig", true);
                startActivity(intent);
                finish();
            }
        });


    }


}
