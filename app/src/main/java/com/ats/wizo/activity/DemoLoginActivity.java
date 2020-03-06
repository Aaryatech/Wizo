package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.RespDevice;
import com.ats.wizo.model.RespDeviceData;
import com.ats.wizo.model.RespRoomData;
import com.ats.wizo.model.RespScanData;
import com.ats.wizo.model.Room;
import com.ats.wizo.model.ScanDevice;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoLoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edUserName,edUserMob;

    private TextInputEditText edPwd;
    private Button btnDemoLogin,btnBackToLogin;
    private String userName,userMob,userPwd;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_login);

        initViews();


    }


    @Override
    public void onClick(View v) {

    switch (v.getId()){
        case R.id.btnDemoLogin: demoLogin();
                                break;


        case R.id.btnBackToLogin: startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                    finish();
                                    break;

    }

    }

    private void demoLogin() {

        if(isValid()){

            progressDialog = new ProgressDialog(DemoLoginActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Please wait ...");
            progressDialog.show();

            RequestBody bodyMobile = RequestBody.create(MediaType.parse("text/plain"), userMob);
            RequestBody bodyName = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userName));
            RequestBody bodyPassword = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userPwd));

            Call<JsonObject> call = Constants.myInterface.demoLogin(bodyMobile, bodyPassword,bodyName);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    try {

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        boolean error = jsonObject.getBoolean("error");
                        Log.e("JSON RESPONSE ", " .. " + jsonObject);

                        if (error) {

                            String msg = jsonObject.getString("message");
                            Toast.makeText(DemoLoginActivity.this, "" + msg, Toast.LENGTH_SHORT).show();

                        } else {
                            JSONObject userObj = jsonObject.getJSONObject("user");

                            Constants.userId = userObj.getString("userId");
                            Constants.authKey = userObj.getString("authKey");

                            Variables.e.putInt("user_id", Integer.parseInt(Constants.userId));
                            Variables.e.putString("auth_key", Constants.authKey);
                            Variables.e.putBoolean("isUserLogin", true);
                            Variables.e.commit();

                            Variables.e.putInt("userId", Integer.parseInt(Constants.userId));
                            Variables.e.putString("authKey", Constants.authKey);
                            Variables.e.putBoolean("isUserLogin", true);
                            Variables.e.putBoolean("isHomeHelpDisplayed", false);
                            Variables.e.putBoolean("isDeviceHelpDisplayed", false);
                            Variables.e.putBoolean("isScheHelpDisplayed", false);

                            Variables.e.apply();
                            Variables.e.commit();

                            setUpOperationsMsg(Constants.authKey);

                            getRoomDataFromServer();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });


        }

    }

    private void getRoomDataFromServer() {


        progressDialog = new ProgressDialog(DemoLoginActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Your Data");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        Call<RespRoomData> call = Constants.myInterface.getRoomsDataByUserId(bodyUserId);

        call.enqueue(new Callback<RespRoomData>() {
            @Override
            public void onResponse(Call<RespRoomData> call, Response<RespRoomData> response) {

                try {

                    RespRoomData respRoomData = response.body();
                    Log.e("User Room Respo.", " .. " + respRoomData.toString());

                    for (Room room : respRoomData.getRoomList()) {

                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        dbHandler.addNewRoom(room);

                    }

                    getDeviceDataFromServer();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<RespRoomData> call, Throwable t) {

            }
        });


    }

    private void getDeviceDataFromServer() {

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        Call<RespDeviceData> call = Constants.myInterface.getDeviceDataByUserId(bodyUserId);
        call.enqueue(new Callback<RespDeviceData>() {
            @Override
            public void onResponse(Call<RespDeviceData> call, Response<RespDeviceData> response) {

                try {

                    RespDeviceData respDeviceData = response.body();

                    if (!respDeviceData.getError()) {

                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        for (RespDevice respDevice : respDeviceData.getDeviceList()) {

                            Device device = new Device(respDevice.getDevCaption(), respDevice.getDevIp(), respDevice.getDevMac(), respDevice.getDevSsid(), respDevice.getDevType(), respDevice.getRoomId(), respDevice.getDevPosition(), respDevice.getDevIsUsed());
                            dbHandler.addNewDevice(device);

                        }

                    }
                    try {
                        Variables.e.putString("ssid", respDeviceData.getDeviceList().get(0).getDevSsid());
                        Variables.e.apply();
                        Constants.homeSSID = respDeviceData.getDeviceList().get(0).getDevSsid();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    getScanDeviceData();


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<RespDeviceData> call, Throwable t) {

            }
        });

    }

    private void getScanDeviceData() {


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        Call<RespScanData> call = Constants.myInterface.getScanDevices(bodyUserId);


        call.enqueue(new Callback<RespScanData>() {
            @Override
            public void onResponse(Call<RespScanData> call, Response<RespScanData> response) {

                try {
                    RespScanData respScanData = response.body();

                    if (!respScanData.getError()) {

                        for (ScanDevice scanDevice : respScanData.getScanList()) {

                            DBHandler dbHandler = new DBHandler(getApplicationContext());
                            dbHandler.addNewMac(scanDevice.getDevMac());

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();

            }

            @Override
            public void onFailure(Call<RespScanData> call, Throwable t) {

            }
        });


    }
    private void setUpOperationsMsg(String authKey) {

        Constants.onOperation = authKey + "piMjVtYV";
        Constants.allOnOperation = authKey + "piMjVtYV#nolla";
        Constants.offOperation = authKey + "JhTVo1V1";
        Constants.allOffOperation = authKey + "JhTVo1V1#ffolla";

    }

    private boolean isValid() {

        userName=edUserName.getText().toString();
        userMob=edUserMob.getText().toString();
        userPwd=edPwd.getText().toString();

        if(userName.equalsIgnoreCase("")){
            edUserName.setError("Please enter name");
            edUserName.requestFocus();
            return false;
        }else if(userMob.equalsIgnoreCase("")){
            edUserMob.setError("Please enter mobile no");
            edUserMob.requestFocus();
            return false;
        }else if(userMob.toString().length()!=10){
            edUserMob.setError("Please enter valid mobile no");
            edUserMob.requestFocus();
            return false;
        }else if(userPwd.equalsIgnoreCase("")){
            edPwd.setError("Please enter password");
            edPwd.requestFocus();
            return false;
        }

        return true;

    }


    private void initViews() {
        edUserName=findViewById(R.id.edUserName);

        edUserMob=findViewById(R.id.edUserMob);

        edPwd=findViewById(R.id.edPwd);

        btnDemoLogin=findViewById(R.id.btnDemoLogin);
        btnDemoLogin.setOnClickListener(this);

        btnBackToLogin=findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(this);

    }


}
