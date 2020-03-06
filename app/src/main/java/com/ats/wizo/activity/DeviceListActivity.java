package com.ats.wizo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.DeviceListAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.CurrentStatus;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.FanStatus;
import com.ats.wizo.model.IpMacList;
import com.ats.wizo.mqtt.MqttConnection;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.ConnectivityChangeReceiver;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.ats.wizo.activity.HomeActivity.routerList;
import static com.ats.wizo.activity.HomeActivity.topicList;
import static com.ats.wizo.adapter.DeviceListAdapter.onList;
import static com.ats.wizo.common.Variables.isInternetAvailable;
import static com.ats.wizo.common.Variables.isMQTTConnected;
import static com.ats.wizo.common.Variables.isStatusReceived;
import static com.ats.wizo.common.Variables.subscribedTopics;
import static com.ats.wizo.constant.Constants.mqttAndroidClient;
import static com.ats.wizo.mqtt.MqttConnection.publishMessage;

public class DeviceListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static IpMacList deviceModel;
    private ListView lvDeviceList;

    TextView tvRoomName;
    Switch allSwitch;
    List<Device> parentDevices;
    List<Device> deviceList;
    public static List<FanStatus> fanStatusList;

    private boolean taskCompleted = false;
    public static DeviceListAdapter deviceListAdapter;
    int roomId;

    private BroadcastReceiver connectivityChangeReceiver = null;

    private static boolean isRegister;

    private String TAG = DeviceListActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    Snackbar negSnackbar;

    ImageView ivRoomIcon;
    private boolean isTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        boolean displayed = Variables.sh.getBoolean("isDeviceHelpDisplayed", false);
        if (!displayed) {
            roomId = getIntent().getIntExtra("roomId", 0);
            deviceList = getDeviceListByRoomId(roomId);
            if (!deviceList.isEmpty()) {
                showDeviceHelp();
            }
        }
        lvDeviceList = findViewById(R.id.lvDeviceList);

        ivRoomIcon = findViewById(R.id.ivRoomIcon);

        lvDeviceList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lvDeviceList.getChildAt(0) != null) {
                    swipeRefreshLayout.setEnabled(lvDeviceList.getFirstVisiblePosition() == 0 && lvDeviceList.getChildAt(0).getTop() == 0);
                }
            }
        });


        allSwitch = findViewById(R.id.allSwitch);

        tvRoomName = findViewById(R.id.tvRoomName);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(DeviceListActivity.this);

        fanStatusList = new ArrayList<>();


        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
//        swipeRefreshLayout.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        swipeRefreshLayout.setRefreshing(true);
//
//                                        //startActivity(getIntent());
//
//                                        onRefresh();
//                                    }
//                                }
//        );

        if (connectivityChangeReceiver == null) {
            connectivityChangeReceiver = new ConnectivityChangeReceiver(findViewById(R.id.llRoomTitle));
        }

        allSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });

        allSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (isTouched) {
                    isTouched = false;
                    if (b) {  // turn ON operation


                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local All ON operation

                            Log.e("DEV LIST ACT","--------------------- isAtHome");
                            for (int i = 0; i < parentDevices.size(); i++) {
                                localAllOn(parentDevices.get(i));
                            }


                        } else {  // mqtt ALL ON operation

                            Log.e("DEV LIST ACT","--------------------- Mqtt ALL");

                            for (int i = 0; i < parentDevices.size(); i++) {

                                if (parentDevices.get(i).getDevType() == 0) {
                                    publishMessage(Constants.allOnOperation, parentDevices.get(i).getDevMac() + Constants.publishTopic);
                                } else if (parentDevices.get(i).getDevType() == 678) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.intensityOperation + "#1");

                                }else if (parentDevices.get(i).getDevType() == 12) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#1");

                                }else if (parentDevices.get(i).getDevType() == 13) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#1");

                                } else {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.allOnOperation);
                                }

                            }
                        }

                    } else {  // turn OFF operation
                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local ALL OFF operation
                            for (int i = 0; i < parentDevices.size(); i++) {
                                localAllOff(parentDevices.get(i));
                            }
                        } else {   // mqtt ALL OFF operation

                            for (int i = 0; i < parentDevices.size(); i++) {

                                if (parentDevices.get(i).getDevType() == 0) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.allOffOperation);
                                } else if (parentDevices.get(i).getDevType() == 678) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.intensityOperation + "#0");

                                } else if (parentDevices.get(i).getDevType() == 12) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#0");

                                } else if (parentDevices.get(i).getDevType() == 13) {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#0");

                                } else {
                                    publishMessage(parentDevices.get(i).getDevMac() + Constants.publishTopic, Constants.allOffOperation);
                                }
                            }


                        }
                    }

                }
            }
        });


        //updateStatus();


    }

    private void localAllOff(Device device) {

        String urlString = "http://" + device.getDevIp();
        Log.e("URL ", ".. " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process("0", "off", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {
                    Log.e("All Device OFF", " .." + response.body().toString());


                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    String status = jsonObject.getString("status");


                    if (status.equalsIgnoreCase("allOff")) {

                        for (int j = 0; j < onList.size(); j++) {
                            onList.get(j).setStatus("Off");

                        }
                        deviceListAdapter.notifyDataSetChanged();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device OFF", " failed" + t.getMessage());
            }
        });


    }

    private void localAllOn(Device device) {


        String urlString = "http://" + device.getDevIp();
        Log.e("device no " + device.getDevType(), "url " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process("0", "on", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    Log.e("Device ON", " .." + response.body().toString());


                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    String status = jsonObject.getString("status");


                    if (status.equalsIgnoreCase("allOn")) {

                        for (int j = 0; j < onList.size(); j++) {
                            onList.get(j).setStatus("On");

                        }
                        deviceListAdapter.notifyDataSetChanged();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device ON", " failed" + t.getMessage());
            }
        });

    }

    private List<Device> getParentdeviceByRoomId(int roomId) {

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Device> deviceList = dbHandler.getParentDevicesByRoomId(roomId);

        Log.e("DeviceList Act 123", "ParentDevices " + deviceList.toString());

        return deviceList;

    }

    private List<Device> getDeviceListByRoomId(int roomId) {

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Device> deviceList = dbHandler.getAllDevicesByRoomId(roomId);
        Log.e("returning list", "  from db " + deviceList.toString());

        return deviceList;
    }


    public void updateStatus() {
        Log.e("local Refresh", "  method");

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                taskCompleted = true;
            }
        }, (deviceList.size() * 3000));

        List<String> ipPublished = new ArrayList<>();

        for (int i = 0; i < deviceList.size(); i++) {

            final Device device = deviceList.get(i);

            if (i == deviceList.size() - 1) {
                taskCompleted = true;
            }

            if (!ipPublished.contains(device.getDevIp())) {

                RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://" + device.getDevIp())
                        .addCallAdapterFactory(rxAdapter)
                        .addConverterFactory(GsonConverterFactory.create()).build();

                ipPublished.add(device.getDevIp());
                MyInterface myInterface = retrofit.create(MyInterface.class);

                Observable<JsonObject> call = myInterface.getSynch("");

                call.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JsonObject>() {
                            @Override
                            public void onCompleted() {
                                // deviceAdapter.notifyDataSetChanged();

                                if (taskCompleted) {
                                    Log.e("Task completed", " onList " + onList.toString());

                                    // TilesDetailsActivity.deviceAdapter.notifyDataSetChanged();
                                    deviceListAdapter.notifyDataSetChanged();

                                    try {

                                        if (swipeRefreshLayout.isRefreshing()) {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("New Json", "Error" + e.getMessage());
                                try {

                                    if (swipeRefreshLayout.isRefreshing()) {
                                        swipeRefreshLayout.setRefreshing(false);
                                    }

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {

                                try {
                                    Log.e("New Json", ".." + jsonObject.toString());

                                    JSONObject object;
                                    object = new JSONObject(jsonObject.toString());

                                    String mac = object.getString("mac");
                                    String deviceId = object.getString("deviceId");

                                    if (deviceId.equalsIgnoreCase("1")) {

                                        String device1 = object.getString("device1");
                                        String device2 = object.getString("device2");
                                        String device3 = object.getString("device3");
                                        String device4 = object.getString("device4");
                                        Log.e("On list is", " .. " + onList.toString());


                                        //1
                                        if (device1.equalsIgnoreCase("on")) {
                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("On");
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("Off");
                                                    Log.e("setting status", " Off1");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 2
                                        if (device2.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("On");
                                                    Log.e("setting status", " On2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("Off");
                                                    Log.e("setting status", " Off2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 3
                                        if (device3.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("On");
                                                    Log.e("setting status", " On3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("Off");
                                                    Log.e("setting status", " Off3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 4
                                        if (device4.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("On");
                                                    Log.e("setting status", " On4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("Off");
                                                    Log.e("setting status", " Off4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }


                                    } else if (deviceId.equalsIgnoreCase("2")) {
                                        String deviceState = object.getString("DeviceState");
                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus(deviceState);
                                                Log.e("setting status", " 1 " + deviceState);
                                                // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                            }
                                        }

                                    } else if (deviceId.equalsIgnoreCase("3")) {

                                        Log.e("DEVICE ","----------**********************************------------------"+object.getString("Fan"));

                                        String device1 = object.getString("device1");
                                        String device2 = object.getString("device2");
                                        String device3 = object.getString("device3");
                                        String device4 = object.getString("device4");
                                        //  String device5 = object.getString("device5");
                                        String device6 = object.getString("Fan");
                                        //  String device7 = object.getString("device7");
                                        // String device8 = object.getString("device8");
                                        Log.e("On list is", " .. " + onList.toString());


                                        //1
                                        if (device1.equalsIgnoreCase("on")) {
                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("On");
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("Off");
                                                    Log.e("setting status", " Off1");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 2
                                        if (device2.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("On");
                                                    Log.e("setting status", " On2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("Off");
                                                    Log.e("setting status", " Off2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 3
                                        if (device3.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("On");
                                                    Log.e("setting status", " On3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("Off");
                                                    Log.e("setting status", " Off3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 4
                                        if (device4.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("On");
                                                    Log.e("setting status", " On4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("Off");
                                                    Log.e("setting status", " Off4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 5
                                      /*  if (device5.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(4).setStatus("On");
                                                    Log.e("setting status", " On5");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(4).setStatus("Off");
                                                    Log.e("setting status", " Off5");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }*/


                                        // 678


                                        if (device6.equalsIgnoreCase("0")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("0");
                                                    Log.e("setting status", " 0 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("1")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("1");
                                                    Log.e("setting status", " 1 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("2")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("2");
                                                    Log.e("setting status", " 2 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("3")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("3");
                                                    Log.e("setting status", " 3 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("4")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("4");
                                                    Log.e("setting status", " 4 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }
                                        /*else if (device6.equalsIgnoreCase("on")) {


                                            if (device8.equalsIgnoreCase("off")) {

                                                if (device7.equalsIgnoreCase("off")) {

                                                    for (int j = 0; j < onList.size(); j++) {
                                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                            onList.get(5).setStatus("25");
                                                            Log.e("setting status", " On 25% ");
                                                            // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                        }
                                                    }
                                                } else if (device7.equalsIgnoreCase("on")) {

                                                    for (int j = 0; j < onList.size(); j++) {
                                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                            onList.get(5).setStatus("75");
                                                            Log.e("setting status", " On 75%");
                                                            // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                        }
                                                    }

                                                }

                                            }

                                        }*/


                                    } else if (deviceId.equalsIgnoreCase("4")) {


                                        String device1 = object.getString("device1");
                                        String device2 = object.getString("device2");
                                        String device3 = object.getString("device3");
                                        String device4 = object.getString("device4");
                                        Log.e("On list is", " .. " + onList.toString());


                                        //1
                                        if (device1.equalsIgnoreCase("on")) {
                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("On");
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("Off");
                                                    Log.e("setting status", " Off1");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 2
                                        if (device2.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("On");
                                                    Log.e("setting status", " On2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("Off");
                                                    Log.e("setting status", " Off2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 3
                                        if (device3.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("On");
                                                    Log.e("setting status", " On3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("Off");
                                                    Log.e("setting status", " Off3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 4
                                        if (device4.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("On");
                                                    Log.e("setting status", " On4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("Off");
                                                    Log.e("setting status", " Off4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                    }else if (deviceId.equalsIgnoreCase("5")) {

                                        Log.e("DEVICE ","----------**********************************------------------"+object.getString("Fan"));

                                        String device1 = object.getString("device1");
                                        String device2 = object.getString("device2");
                                        String device3 = object.getString("device3");
                                        String device4 = object.getString("device4");
                                        //  String device5 = object.getString("device5");
                                        String device6 = object.getString("Dimmer 1");
                                          String device7 = object.getString("Dimmer 2");
                                        // String device8 = object.getString("device8");
                                        Log.e("On list is", " .. " + onList.toString());


                                        //1
                                        if (device1.equalsIgnoreCase("on")) {
                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("On");
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(0).setStatus("Off");
                                                    Log.e("setting status", " Off1");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 2
                                        if (device2.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("On");
                                                    Log.e("setting status", " On2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(1).setStatus("Off");
                                                    Log.e("setting status", " Off2");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 3
                                        if (device3.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("On");
                                                    Log.e("setting status", " On3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(2).setStatus("Off");
                                                    Log.e("setting status", " Off3");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }

                                        // 4
                                        if (device4.equalsIgnoreCase("on")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("On");
                                                    Log.e("setting status", " On4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        } else {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(3).setStatus("Off");
                                                    Log.e("setting status", " Off4");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }



                                        // 12


                                        if (device6.equalsIgnoreCase("A0")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("0");
                                                    Log.e("setting status", " 0 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A1")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("1");
                                                    Log.e("setting status", " 1 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A2")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("2");
                                                    Log.e("setting status", " 2 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A3")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("3");
                                                    Log.e("setting status", " 3 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A4")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("4");
                                                    Log.e("setting status", " 4 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A5")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("5");
                                                    Log.e("setting status", " 5 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A6")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("6");
                                                    Log.e("setting status", " 6 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A7")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("7");
                                                    Log.e("setting status", " 7 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A8")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("8");
                                                    Log.e("setting status", " 8 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A9")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("9");
                                                    Log.e("setting status", " 9 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device6.equalsIgnoreCase("A10")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(5).setStatus("10");
                                                    Log.e("setting status", " 10 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }


                                        // 13


                                        if (device7.equalsIgnoreCase("B0")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("0");
                                                    Log.e("setting status", " 0 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B1")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("1");
                                                    Log.e("setting status", " 1 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B2")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("2");
                                                    Log.e("setting status", " 2 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B3")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("3");
                                                    Log.e("setting status", " 3 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B4")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("4");
                                                    Log.e("setting status", " 4 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B5")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("5");
                                                    Log.e("setting status", " 5 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B6")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("6");
                                                    Log.e("setting status", " 6 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B7")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("7");
                                                    Log.e("setting status", " 7 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B8")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("8");
                                                    Log.e("setting status", " 8 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B9")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("9");
                                                    Log.e("setting status", " 9 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }else  if (device7.equalsIgnoreCase("B10")) {

                                            for (int j = 0; j < onList.size(); j++) {
                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                    onList.get(6).setStatus("10");
                                                    Log.e("setting status", " 10 ");
                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                }
                                            }

                                        }


                                    }


                                } catch (
                                        JSONException e) {
                                    e.printStackTrace();
                                    Log.e("Exception ", " .. " + e.getMessage());
                                }

                            }
                        });
            }
        }

    }

    private void refreshDevices() {

        Log.e("inside refresh ", " .. ");

        swipeRefreshLayout.setRefreshing(true);
        try {

            Log.e("Topic list ", " .. " + parentDevices.size());
            String msg = "status";
            isStatusReceived = false;

            for (int k = 0; k < parentDevices.size(); k++) {

                //   if (Variables.isInternetAvailable && Variables.isMQTTConnected) {
                Log.e("Publishing to topic ", " .. " + k);
                MqttConnection.publishMessage(mqttAndroidClient, Constants.authKey + msg, parentDevices.get(k).getDevMac() + Constants.publishTopic);
                //}
            }

            final Handler handler = new Handler();
            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Now checking", " for status flag");

                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            if (!isStatusReceived) {

                                String message = "Connection Failed !! Verify Devices Are Powered ON & Connected To Internet";
                                int color = Color.parseColor("#FFEA5E5B");


                                negSnackbar = Snackbar.make(findViewById(R.id.llRoomTitle), message, Snackbar.LENGTH_INDEFINITE);
                                View sbView = negSnackbar.getView();


                                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) sbView.getLayoutParams();
                                params.gravity = Gravity.TOP;
                                sbView.setLayoutParams(params);

                                TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(color);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                } else {
                                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                                }
                                negSnackbar.show();

                            } else {

                                if (negSnackbar != null) {
                                    if (negSnackbar.isShown()) {
                                        negSnackbar.dismiss();
                                    }
                                }
                            }

                        }

                    }, 4000);


            try {

                final Handler handler2 = new Handler();
                handler2.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {

                                Log.e("Now checking for", "2nd time ");

                                if (isStatusReceived) {

                                    if (negSnackbar != null) {
                                        if (negSnackbar.isShown()) {
                                            negSnackbar.dismiss();
                                        }
                                    }

                                }

                            }

                        }, 4000);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        onRefresh();
    }

    private void showAlert(String roomName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);

        builder.setTitle("No Device")
                .setMessage("You don't have any device configured under " + roomName)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                })
                .setCancelable(false)
                .show();

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e("inside ", " pause method ");

        try {
            if (isRegister) {
                unregisterReceiver(connectivityChangeReceiver);
                isRegister = false;
                Log.e("receiver ", " unregistered ");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable(Context context) {
        boolean isConnected = false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            Log.v("DeviceListAct", " Now you are connected to Internet!");

                            isConnected = isOnline();

                            if (isConnected) {
                                isInternetAvailable = true;
                                Variables.isAtHome = false;

                                if (!isMQTTConnected || subscribedTopics.size() < topicList.size()) {

                                    MqttConnection.initializeMQTT(context, topicList);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(
                                            new Runnable() {
                                                @Override
                                                public void run() {

                                                    refreshDevices();
                                                }

                                            }, 3000);


                                }

                            } else {
                                isInternetAvailable = false;
                                Variables.isAtHome = false;
                                NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                if (wifi.isAvailable() && wifi.isConnected()) {
                                    Log.i("ConnectivityReceiver ", "Found WI-FI Network");

                                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();


                                    String ssid = wifiInfo.getSSID();

                                    Log.e("Current WiFi", ".. " + ssid);

                                    for (int j = 0; j < routerList.size(); j++) {
                                        if (ssid.equals("\"" + routerList.get(j) + "\"")) {

                                            Variables.isAtHome = true;

                                            Log.e("No Internet ", " But at home ");
                                        }

                                    }
                                    if (Variables.isAtHome && !Variables.isInternetAvailable) {
                                        updateStatus();
                                    }


                                }

                            }

                        }
                        return true;
                    }
                }
            }
        }
        Log.v("DeviceListAct", "You are not connected to Internet!");
        //  Toast.makeText(context, "Internet NOT available via Broadcast receiver", Toast.LENGTH_SHORT).show();
        isConnected = false;
        return false;
    }

    public Boolean isOnline() {
        try {
            InetAddress ipAddr = InetAddress.getByName("www.google.com");

            Log.e("receiver ", "In internet check method " + ipAddr);

            return !ipAddr.equals("");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void onRefresh() {


        roomId = getIntent().getIntExtra("roomId", 0);

        String roomName = getIntent().getStringExtra("roomName");
        String roomIcon = getIntent().getStringExtra("roomIcon");

        Log.e("Room Adapter ", " Room ID " + roomId);

        tvRoomName.setText(roomName);

        switch (roomIcon) {

            case "LR":
                ivRoomIcon.setImageResource(R.mipmap.living_room_icon);
                break;
            case "MB":
                ivRoomIcon.setImageResource(R.mipmap.master_bedroom_icon);
                break;
            case "K":
                ivRoomIcon.setImageResource(R.mipmap.kitchen_icon);
                break;
            case "B":
                ivRoomIcon.setImageResource(R.mipmap.bedroom_icon);
                break;
            default:
                ivRoomIcon.setImageResource(R.mipmap.living_room_icon);
                break;

        }


        deviceList = new ArrayList<>();
        parentDevices = new ArrayList<>();
        deviceList = getDeviceListByRoomId(roomId);
        parentDevices = getParentdeviceByRoomId(roomId);

        if (deviceList.isEmpty()) {

            showAlert(roomName);

        } else {

            Log.e("DeviceList Act 456 ", " Device List " + deviceList.toString());

            onList.clear();

            int switch5Pos = 0;
            for (int i = 0; i < deviceList.size(); i++) {

                CurrentStatus currentStatus = new CurrentStatus();
                currentStatus.setMac(deviceList.get(i).getDevMac());
                currentStatus.setStatus("OFF");
                int pos = deviceList.get(i).getDevType();

                currentStatus.setPosition(pos);
                onList.add(currentStatus);

                if (deviceList.get(i).getDevId() == 277) {
                    switch5Pos = i;
                }

                if (deviceList.get(i).getDevId() == 678) {
                    deviceList.get(i).setDevCaption("Fan");
                }

            }

            if (switch5Pos > 0) {
                deviceList.remove(switch5Pos);
            }


           // Collections.sort(deviceList, Collections.reverseOrder());

            deviceListAdapter = new DeviceListAdapter(getApplicationContext(), deviceList, DeviceListActivity.this);

            lvDeviceList.setAdapter(deviceListAdapter);

        }


        if (!deviceList.isEmpty()) {

            if (Variables.isInternetAvailable && Variables.isMQTTConnected) {
                refreshDevices();
            } else if (!Variables.isInternetAvailable && Variables.isAtHome) {
                updateStatus();
            }

            Log.e("is MQTT ", " connected ? " + Variables.isMQTTConnected);
            Log.e("is at ", " Home ? " + Variables.isAtHome);


            if (!Variables.isMQTTConnected && !Variables.isAtHome) {

                boolean isNetworkAvailable = isNetworkAvailable(getApplicationContext());

                Log.e("isNetwork ", " available " + isNetworkAvailable);

                if (swipeRefreshLayout.isRefreshing()) {

                    swipeRefreshLayout.setRefreshing(false);
                }

            } else {

                if (!isRegister) {
                    Log.e("registering ", " receiver ");

                    registerReceiver(connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                    isRegister = true;
                }


            }


        }

    }

    public void showDeviceHelp() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final int trans = Color.parseColor("#49000000");

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(trans));
        dialog.setContentView(R.layout.help_device_list);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Variables.e.putBoolean("isDeviceHelpDisplayed", true);
                Variables.e.apply();
                Variables.e.commit();

            }
        });
        dialog.show();
    }


    public void setRoomAllOn() {

        allSwitch.setChecked(true);
        isTouched = false;
    }

    public void setRoomAllOff() {
        allSwitch.setChecked(false);
        isTouched = false;
    }


}
