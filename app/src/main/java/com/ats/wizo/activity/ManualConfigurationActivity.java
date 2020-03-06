package com.ats.wizo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.Room;
import com.ats.wizo.sqlite.DBHandler;
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

import static com.ats.wizo.activity.SelectHomeRouterActivity.frequency;
import static com.ats.wizo.util.Utils.getChannelFromFrequency;

public class ManualConfigurationActivity extends AppCompatActivity {

    private EditText edSSID,edHomeRouterPassword,edChannel,edIpAddress;
    private Button btnConfigure;

    private boolean isChannelSend;
    private String ssid,password,channel,ipAddress;
    List<Room> roomList;
    List<String> roomadapterList;
    ProgressDialog progressDialog;
    Spinner spinnerRoom;
    String roomName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_configuration);

        edSSID =findViewById(R.id.edSSID);
        edHomeRouterPassword =findViewById(R.id.edHomeRouterPassword);
        edChannel =findViewById(R.id.edChannel);
      //  edIpAddress =findViewById(R.id.edIpAddress);

        btnConfigure =findViewById(R.id.btnConfigure);

        spinnerRoom = findViewById(R.id.spRoom);

        roomList = getRoomData();

        isChannelSend=false;

        roomadapterList = new ArrayList<>();
        roomadapterList.add("Please Select Room");

        for (int i = 0; i < roomList.size(); i++) {

            roomadapterList.add(roomList.get(i).getRoomName());

        }


        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item, roomadapterList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoom.setAdapter(spinnerAdapter);

        btnConfigure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(isValid()){

                    if(isChannelSend){
                        sendConfigurationData();
                    }else{
                        sendChannelData();
                    }

                }

            }
        });

    }

    private List<Room> getRoomData() {

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        List<Room> roomList = dbHandler.getAllRooms();

        return roomList;
    }





    private void sendConfigurationData() {


        int selectedPos = spinnerRoom.getSelectedItemPosition();

        roomName = roomadapterList.get(selectedPos);

        int roomId = roomList.get(0).getRoomId();

        for (int i = 0; i < roomList.size(); i++) {

            if (roomList.get(i).getRoomName().equalsIgnoreCase(roomName)) {

                roomId = roomList.get(i).getRoomId();
            }

        }

        configNewSwitchDevice("Switch 1","Switch 2","Switch 3","Switch 4",roomId);

    }

    private boolean isValid() {

        ssid=edSSID.getText().toString();
        password=edHomeRouterPassword.getText().toString();
        channel=edChannel.getText().toString();

        if(ssid.equalsIgnoreCase("")){
            edSSID.setError("Please Enter Home Router Name");
            edSSID.requestFocus();
            return false;

        } else if(password.equalsIgnoreCase("")){
            edHomeRouterPassword.setError("Please Enter Home Router Password");
            edHomeRouterPassword.requestFocus();
            return false;

        } else if(channel.equalsIgnoreCase("")){
            edChannel.setError("Please Enter Channel No");
            edChannel.requestFocus();
            return false;

        } else{
            return true;

        }

    }
    private void sendChannelData() {

        progressDialog = new ProgressDialog(ManualConfigurationActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Configuring");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();



        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.137.29")
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Call<JsonObject> call = myInterface.sendChannel(channel);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {
                    Log.e("onResponse", " : " + response.body());

                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }

                    

                    JSONObject object = new JSONObject(response.body().toString());

                    String status = object.getString("status");
                    if (status.equalsIgnoreCase("success")) {
                        isChannelSend = true;
                        btnConfigure.setText("Configure");

                    } else {

                        isChannelSend = false;
                       Toast.makeText(ManualConfigurationActivity.this, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();
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

                isChannelSend = false;
                Toast.makeText(ManualConfigurationActivity.this, "Failed to connect, Please try again", Toast.LENGTH_SHORT).show();


            }
        });

    }







    private void configNewSwitchDevice(final String cap1, final String cap2, final String cap3, final String cap4, final int roomId) {

        try {
            progressDialog = new ProgressDialog(ManualConfigurationActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setTitle("Configuring");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.137.29")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            MyInterface myInterface = retrofit.create(MyInterface.class);

//            String channel = String.valueOf(getChannelFromFrequency(frequency));
//
//
//            final String ssid = Variables.sh.getString("ssid", "");
//            String password = Variables.sh.getString("password", "");


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

                            WiFiService.connectToWPAWiFi(ManualConfigurationActivity.this, "\"" + ssid + "\"", "\"" + password + "\"");

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

                                }, 18000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                    Log.e("OnFailure ", " .. " + t.getMessage());

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    Toast.makeText(ManualConfigurationActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(ManualConfigurationActivity.this, R.style.MyAlertDialogStyle);
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


        Log.e(" ## DeviceList "," upload "+deviceList.toString());

        Call<JsonObject> call = Constants.myInterface.uploadDeviceData(deviceList);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    Log.e("Config Response"," "+jsonObject.toString());

                    if (!jsonObject.getBoolean("error")) {

                        Toast.makeText(ManualConfigurationActivity.this, "Device Added Successfully Under " + roomName, Toast.LENGTH_SHORT).show();


                    } else {
                        Toast.makeText(ManualConfigurationActivity.this, "Device Added But Failed To Upload, Please Try Uploading Data From Setting Menu", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(ManualConfigurationActivity.this, "Device Added But Failed To Upload, Please Try Uploading Data From Setting Menu", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);

                intent.putExtra("isFromConfig", true);
                startActivity(intent);
                finish();
            }
        });


    }

}
