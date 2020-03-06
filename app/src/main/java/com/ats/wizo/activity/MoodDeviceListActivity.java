package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.MoodDeviceListAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.ErrorMessage;
import com.ats.wizo.model.MoodDetailList;
import com.ats.wizo.model.MoodDeviceMapping;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.MoodsList;
import com.ats.wizo.model.RespMoodList;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ats.wizo.mqtt.MqttConnection.publishMessage;
import static java.security.AccessController.getContext;

public class MoodDeviceListActivity extends AppCompatActivity {

    public static int moodId;
    public static String moodName;
    List<Device> deviceList;
    TextView tvRoomName;

    ListView lvDeviceList;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton fab;

    Switch swMood;
    private boolean isTouched = false;

    ImageView ivMoodSch;

    DBHandler dbHandler;

    public static int changeFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_device_list);

        lvDeviceList = findViewById(R.id.lvDeviceList);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        tvRoomName = findViewById(R.id.tvRoomName);

        swMood = findViewById(R.id.swMood);

        fab = findViewById(R.id.fab);

        ivMoodSch = findViewById(R.id.ivMoodSch);

        tvRoomName.setText(getIntent().getStringExtra("moodName"));

        moodId = getIntent().getIntExtra("moodId", 0);
        moodName = getIntent().getStringExtra("moodName");

        dbHandler = new DBHandler(getApplicationContext());

        // getAllDevicesByMood(moodId);


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


        ivMoodSch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MoodSchedulerActivity.class);
                intent.putExtra("moodId", moodId);
                intent.putExtra("moodName", moodName);
                startActivity(intent);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent deviceListIntent = new Intent(getApplicationContext(), AddNewDeviceToMoodActivity.class);
                deviceListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                deviceListIntent.putExtra("moodId", moodId);
                startActivity(deviceListIntent);


            }
        });

        swMood.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isTouched = true;
                return false;
            }
        });


        swMood.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isTouched) {

                    //getDeviceList(moodId);


                    if (isChecked) {

                        changeFlag = 1;


                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        //  deviceList = dbHandler.getAllDevicesByMoodId(moodId);
                        deviceList = dbHandler.getDevicesByMoodId(moodId);


                        MoodDeviceListAdapter moodDeviceListAdapter = new MoodDeviceListAdapter(getApplicationContext(), deviceList, MoodDeviceListActivity.this, changeFlag);
                        lvDeviceList.setAdapter(moodDeviceListAdapter);

                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local All ON operation

                            Log.e("MOOD DEV LIST ACT", "################----------------- isAtHome");
                            for (int i = 0; i < deviceList.size(); i++) {
                                switchON(deviceList.get(i));
                            }


                        } else {  // mqtt ALL ON operation

                            Log.e("MOOD DEV LIST ACT", "################----------------- ELSE");

                            for (int i = 0; i < deviceList.size(); i++) {

                               /* if (deviceList.get(i).getDevType() == 0) {
                                    publishMessage(Constants.allOnOperation, deviceList.get(i).getDevMac() + Constants.publishTopic);
                                } else {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#" + deviceList.get(i).getDevType());


                                }*/

                                Log.e("DEVICE : " + i, "----------------  " + deviceList.get(i).getDevCaption() + "            DEV Type -- " + deviceList.get(i).getDevType() + "               Operation -- " + deviceList.get(i).getOperation() + "             Detail id -- " + deviceList.get(i).getDetailId());


                                if (deviceList.get(i).getDevType() == 0) {
                                    publishMessage(Constants.allOnOperation, deviceList.get(i).getDevMac() + Constants.publishTopic);
                                } else if (deviceList.get(i).getDevType() == 678) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.intensityOperation + "#" + deviceList.get(i).getOperation());

                                } else if (deviceList.get(i).getDevType() == 12) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#" + deviceList.get(i).getOperation());

                                } else if (deviceList.get(i).getDevType() == 13) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#" + deviceList.get(i).getOperation());

                                } else {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#" + deviceList.get(i).getDevType());


                                }


                            }

                            updateMoodStatusToServer(moodId, 1);

                        }

                        dbHandler.updateMoodStatus(moodId, 1);

                    } else {

                        changeFlag = 0;

                        MoodDeviceListAdapter moodDeviceListAdapter = new MoodDeviceListAdapter(getApplicationContext(), deviceList, MoodDeviceListActivity.this, changeFlag);
                        lvDeviceList.setAdapter(moodDeviceListAdapter);


                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local All OFF operation

                            for (int i = 0; i < deviceList.size(); i++) {
                                switchOFF(deviceList.get(i));
                            }


                        } else {  // mqtt ALL OFF operation


                            for (int i = 0; i < deviceList.size(); i++) {
                               /* if (deviceList.get(i).getDevType() == 0) {
                                    publishMessage(Constants.allOnOperation, deviceList.get(i).getDevMac() + Constants.publishTopic);
                                } else {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#" + deviceList.get(i).getDevType());


                                }*/


                                if (deviceList.get(i).getDevType() == 0) {
                                    publishMessage(Constants.allOffOperation, deviceList.get(i).getDevMac() + Constants.publishTopic);
                                } else if (deviceList.get(i).getDevType() == 678) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.intensityOperation + "#0");

                                } else if (deviceList.get(i).getDevType() == 12) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#0");

                                } else if (deviceList.get(i).getDevType() == 13) {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#0");

                                } else {
                                    publishMessage(deviceList.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#" + deviceList.get(i).getDevType());

                                }


                            }


                            updateMoodStatusToServer(moodId, 0);

                        }

                        dbHandler.updateMoodStatus(moodId, 0);


                    }

                    isTouched = false;
                }


            }
        });


    }

    private void updateMoodStatusToServer(int moodId, int status) {


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodId));
        RequestBody bodyStatus = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(status));

        Call<ErrorMessage> call = Constants.myInterface.updateMoodStatus(bodyUserId, bodyMoodId, bodyStatus);

        call.enqueue(new Callback<ErrorMessage>() {
            @Override
            public void onResponse(Call<ErrorMessage> call, Response<ErrorMessage> response) {


            }

            @Override
            public void onFailure(Call<ErrorMessage> call, Throwable t) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        //getDeviceList(moodId);


        if (Variables.isInternetAvailable)
            getMoodStatus();


    }

    private void getMoodStatus() {

        swipeRefreshLayout.setRefreshing(true);


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodId));

        Call<JsonObject> call = Constants.myInterface.getMoodStatus(bodyUserId, bodyMoodId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.e("MOOD DEV LIST ACT ", "----------------------getMoodStatus---------------- " + response.body());

                try {


                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }


                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    int status = jsonObject.getInt("status");

                    changeFlag = status;

                    DBHandler dbHandler = new DBHandler(getApplicationContext());

                    if (status == 0) {
                        swMood.setChecked(false);
                        dbHandler.updateMoodStatus(moodId, 0);

                    } else if (status == 1) {

                        swMood.setChecked(true);
                        dbHandler.updateMoodStatus(moodId, 1);

                    }


                } catch (Exception e) {

                    e.printStackTrace();
                }

                getAllDevicesByMood(moodId);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                getAllDevicesByMood(moodId);


                Log.e("mood status", " Failed " + t.getMessage());

            }
        });


    }

    private void switchON(Device device) {

        String urlString = "http://" + device.getDevIp();
        Log.e("device no " + device.getDevType(), "url " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process(String.valueOf(device.getDevType()), "on", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    Log.e("Device ON", " .." + response.body().toString());
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

    private void switchOFF(Device device) {

        String urlString = "http://" + device.getDevIp();
        Log.e("URL ", ".. " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process(String.valueOf(device.getDevType()), "off", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {
                    Log.e("Device OFF", " .." + response.body().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device OFF", " failed" + t.getMessage());
                Toast.makeText(getApplicationContext(), "Try Synch With Router Option", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void getDeviceList(int moodId) {

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        //  deviceList = dbHandler.getAllDevicesByMoodId(moodId);
        deviceList = dbHandler.getDevicesByMoodId(moodId);

        Log.e("Device List for " + moodId, " --@@@@@@@@@@@@--- " + deviceList.toString());

        MoodDeviceListAdapter moodDeviceListAdapter = new MoodDeviceListAdapter(getApplicationContext(), deviceList, MoodDeviceListActivity.this, changeFlag);
        lvDeviceList.setAdapter(moodDeviceListAdapter);

    }


    public void getAllDevicesByMood(final int moodId) {

        Log.e("MOOD DEV LIST ACT ", "----------------------------- IN getAllDevicesByMood");

        final ProgressDialog progressDialog = new ProgressDialog(MoodDeviceListActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading ");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        Call<ArrayList<Device>> listCall = Constants.myInterface.getMoodDeviceListByMoodId(moodId);
        listCall.enqueue(new Callback<ArrayList<Device>>() {
            @Override
            public void onResponse(Call<ArrayList<Device>> call, Response<ArrayList<Device>> response) {
                try {
                    if (response.body() != null) {

                        Log.e("mood Data : ", "------------" + response.body());

                        ArrayList<Device> data = response.body();
                        if (data == null) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            //Toast.makeText(SelectCityActivity.this, "No Cities Found !", Toast.LENGTH_SHORT).show();
                        } else {

                            DBHandler dbHandler = new DBHandler(getApplicationContext());

                            if (data.size() > 0) {
                                for (int i = 0; i < data.size(); i++) {

                                    //  MoodDeviceMapping moodDev = dbHandler.getMoodDeviceById("" + data.get(i).getDetailId());
                                    if (dbHandler.checkIsDeviceExist(data.get(i).getDetailId())) {
                                        Log.e("MOOD DEV LIST ACT ", "--------------------******************************************* ------------------- FOUND");
                                        dbHandler.updateMoodOperation(data.get(i).getDetailId(), data.get(i).getOperation());
                                    }
                                }
                            }


                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }


                        }
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        Log.e("Data Null : ", "-----------");
                    }

                    getDeviceList(moodId);

                } catch (Exception e) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    Log.e("Exception : ", "-----------" + e.getMessage());
                    e.printStackTrace();

                    getDeviceList(moodId);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Device>> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Log.e("onFailure : ", "-----------" + t.getMessage());
                t.printStackTrace();

                getDeviceList(moodId);
            }
        });

    }


}
