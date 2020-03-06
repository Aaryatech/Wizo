package com.ats.wizo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.MoodDetailList;
import com.ats.wizo.model.MoodDeviceMapping;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.MoodsList;
import com.ats.wizo.model.RespDevice;
import com.ats.wizo.model.RespDeviceData;
import com.ats.wizo.model.RespMoodList;
import com.ats.wizo.model.RespRoomData;
import com.ats.wizo.model.RespScanData;
import com.ats.wizo.model.Room;
import com.ats.wizo.model.ScanDevice;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText edOtp, edUserName;
    private ProgressDialog progressDialog;
    private int otp = 0;
    private TextInputLayout iLOTP;
    private TextView tvDemoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        edUserName = findViewById(R.id.edUserName);
        edOtp = findViewById(R.id.edOtp);
        iLOTP = findViewById(R.id.iLOTP);
        tvDemoLogin = findViewById(R.id.tvDemoLogin);

        edOtp.setVisibility(View.INVISIBLE);
        iLOTP.setVisibility(View.INVISIBLE);


        tvDemoLogin.setPaintFlags(tvDemoLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnLogin.setText("Send OTP");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1001);

        }

        tvDemoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startActivity(new Intent(getApplicationContext(), DemoLoginActivity.class));

            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnLogin.getText().toString().equalsIgnoreCase("Send OTP")) {

                    String userMob = edUserName.getText().toString();

                    if (userMob.length() == 10) {

                        if (isOnline()) {

                            progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
                            progressDialog.setMessage("Please wait ...");
                            progressDialog.show();

                            Random rnd = new Random();

                            otp = 100000 + rnd.nextInt(900000);

                            Log.e("LoginAct ", "OTP " + otp);

                            RequestBody bodyFormMode = RequestBody.create(MediaType.parse("text/plain"), "otp");

                            RequestBody bodyMobile = RequestBody.create(MediaType.parse("text/plain"), userMob);
                            RequestBody bodyOtp = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(otp));

                            Call<JsonObject> call = Constants.myInterface.login(bodyMobile, bodyOtp);

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
                                            Toast.makeText(LoginActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
                                        } else {

                                            edOtp.setVisibility(View.VISIBLE);
                                            iLOTP.setVisibility(View.VISIBLE);
                                            edUserName.setFocusable(false);
                                            iLOTP.setFocusable(false);

                                            btnLogin.setText("Login");

                                            JSONObject userObj = jsonObject.getJSONObject("user");

                                            Constants.userId = userObj.getString("userId");
                                            Constants.authKey = userObj.getString("authKey");

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {

                                    if (progressDialog.isShowing()) {

                                        progressDialog.dismiss();
                                    }
                                    Log.e("LoginAct ", " failed " + t.getMessage());
                                    Toast.makeText(LoginActivity.this, "Something Went Wrong, Please Try Again", Toast.LENGTH_SHORT).show();

                                }
                            });


                        }
                    } else {


                        edUserName.setError("Please Enter Valid Mobile No");

                    }
                } else {
                    if (edOtp.getText().toString().equalsIgnoreCase("")) {
                        edOtp.setError("Please Enter OTP");
                        edOtp.requestFocus();
                    } else if (edOtp.getText().toString().equalsIgnoreCase("" + otp)) {

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

                    } else {


                        edOtp.setError("Incorrect OTP");
                        edOtp.requestFocus();
                    }


                }

            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

    }

    private void setUpOperationsMsg(String authKey) {

        Constants.onOperation = authKey + "piMjVtYV";
        Constants.allOnOperation = authKey + "piMjVtYV#nolla";
        Constants.offOperation = authKey + "JhTVo1V1";
        Constants.allOffOperation = authKey + "JhTVo1V1#ffolla";
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


                    // getScanDeviceData();
                    getMoodsData();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<RespDeviceData> call, Throwable t) {

            }
        });


    }


    private void getMoodsData() {


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        Call<RespMoodList> call = Constants.myInterface.getMoodsByUserId(bodyUserId);

        call.enqueue(new Callback<RespMoodList>() {
            @Override
            public void onResponse(Call<RespMoodList> call, Response<RespMoodList> response) {

                try {

                    RespMoodList respMoodList = response.body();

                    Log.e("Moods Data ", "" + respMoodList.toString());

                    if (!respMoodList.getError()) {

                        List<MoodsList> moodsLists = respMoodList.getMoodsList();

                        for (MoodsList moodsList : moodsLists) {

                            MoodMaster master = new MoodMaster();

                            master.setMoodName(moodsList.getMoodHeader().getMoodName());
                            master.setMoodId(moodsList.getMoodHeader().getMoodHeaderId());

                            DBHandler dbHandler = new DBHandler(getApplicationContext());
                            dbHandler.addNewMood(master);

                            List<MoodDetailList> moodDetailLists = moodsList.getMoodDetailList();

                            for (MoodDetailList detailList : moodDetailLists) {

                                MoodDeviceMapping moodDeviceMapping = new MoodDeviceMapping();

                                moodDeviceMapping.setMoodId(moodsList.getMoodHeader().getMoodHeaderId());
                                moodDeviceMapping.setMoodDevMac(detailList.getDevMac());
                                moodDeviceMapping.setMoodDevType(detailList.getDevType());
                                moodDeviceMapping.setMoodOperation(detailList.getOperation());
                                moodDeviceMapping.setMoodDetailId(detailList.getMoodDetailId());

                                dbHandler.addNewDeviceToMood(moodDeviceMapping);

                            }


                        }
                        getScanDeviceData();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<RespMoodList> call, Throwable t) {

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

    private void getRoomDataFromServer() {
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.MyAlertDialogStyle);
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


    /*private void userLogin(String mobile, String pwd) {

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Authenticating User");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyFormMode = RequestBody.create(MediaType.parse("text/plain"), "get_login");
        RequestBody bodyMobile = RequestBody.create(MediaType.parse("text/plain"), mobile);
        RequestBody bodyPwd = RequestBody.create(MediaType.parse("text/plain"), pwd);

        Call<JsonObject> call = Constants.myInterface.userLogin(bodyFormMode, bodyMobile, bodyPwd);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                try {
                    Log.e("User Login Respo.", " .. " + response.body().toString());

                    JSONObject object = new JSONObject(response.body().toString());
                    boolean error = object.getBoolean("error");
                    if (error) {

                        Toast.makeText(LoginActivity.this, "Invalid login details", Toast.LENGTH_SHORT).show();

                    } else {
                        String user_id = object.getString("user_id");
                        String auth_key = object.getString("auth_key");
                        Constants.authKey = auth_key;
                        Variables.e.putString("user_id", user_id);
                        Variables.e.putString("auth_key", auth_key);
                        Variables.e.putBoolean("isUserLogin", true);
                        Variables.e.commit();

                        if (isFirstUse.equalsIgnoreCase("y")) {
                            HomeActivity.needToFetch = true;
                            Log.e("First User", "..");
                            startActivity(new Intent(getApplicationContext(), HelperTabActivity.class));
                            finish();
                        } else {
                            HomeActivity.needToFetch = false;
                            Log.e("Old User", "..");
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Excpetion ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                Log.e("User Login Fail", " .. " + t.getMessage());

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }
        });
    }*/

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Exit ?");
        builder.setMessage("Do you really want to Exit ?");

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.show();

    }


    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet Connection ! ", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


}

