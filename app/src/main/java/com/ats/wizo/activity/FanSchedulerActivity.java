package com.ats.wizo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.SchedulerAdapter;
import com.ats.wizo.adapter.SchedulerFanAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.fragment.OffScheduleFragment;
import com.ats.wizo.fragment.OnScheduleFragment;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.RespScheduler;
import com.ats.wizo.model.RespSchedulerData;
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

import static com.ats.wizo.activity.DeviceDetailsActivity.fanSchList;
import static com.ats.wizo.activity.DeviceDetailsActivity.offSchList;

public class FanSchedulerActivity extends AppCompatActivity {


    // lvFanSch
    // fabFanScheduler

    private ListView lvFanSch;
    private FloatingActionButton fabFanScheduler;

    public SchedulerFanAdapter schedulerFanAdapter;

    public static Device device;

    public ArrayList<RespScheduler> fanSchedulerList = new ArrayList<>();

    ProgressDialog progressDialog;
    public static String type = "";

    private TextView tvCaption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_scheduler);
        lvFanSch = findViewById(R.id.lvFanSch);
        fabFanScheduler = findViewById(R.id.fabFanScheduler);


//        if (Variables.isInternetAvailable) {
//            getDeviceSchData();
//        } else {
//            Toast.makeText(this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
//        }

//        boolean displayed = Variables.sh.getBoolean("isScheHelpDisplayed", false);

        tvCaption = findViewById(R.id.tvCaption);


        LinearLayout myLayout = findViewById(R.id.myLayout);
        ImageView ivEditCaption = findViewById(R.id.ivEditCaption);

        fabFanScheduler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Variables.isInternetAvailable) {

//                    Log.e("DEV TYPE : ","*********** ----------- "+DeviceDetailsActivity.device.getDevType());

                    Intent intent = new Intent(FanSchedulerActivity.this, TimePickerActivity.class);
                    intent.putExtra("op", "ON");
                    intent.putExtra("moodId", -1);
                    intent.putExtra("devType", 678);
                    intent.putExtra("type", 100);//temp value


                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(FanSchedulerActivity.this, "Please Connect To Internet First", Toast.LENGTH_SHORT).show();
                }

            }
        });


        ivEditCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Variables.isInternetAvailable) {

                    final Dialog dialog = new Dialog(FanSchedulerActivity.this, R.style.MyAlertDialogStyle);
                    dialog.setContentView(R.layout.dialog_edit_caption);

                    ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                    // set the custom dialog components - text, image and button
                    final EditText editText = dialog.findViewById(R.id.edCaption);
                    Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                    btnSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            String caption = editText.getText().toString();

                            if (caption.equalsIgnoreCase("")) {
                                editText.setError("Please enter valid name");
                            } else {

                                updateCaption(caption, device);
                                dialog.dismiss();
                            }


                        }
                    });


                    dialog.show();

                } else {
                    Toast.makeText(FanSchedulerActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();

                }

            }
//
        });


        try {
            tvCaption.setText(device.getDevCaption());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Excption", ".." + e.getMessage());

        }


    }


    private void updateCaption(String caption, Device device) {

        tvCaption.setText(caption);

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        dbHandler.updateDeviceCaption(caption, device);

        registerDeviceToServer();


    }

    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(FanSchedulerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Updating");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        DBHandler dbHandler = new DBHandler(FanSchedulerActivity.this);
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

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    if (!jsonObject.getBoolean("error")) {
                        Toast.makeText(FanSchedulerActivity.this, "Caption Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FanSchedulerActivity.this, "Failed To Update, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }


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
                Toast.makeText(FanSchedulerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void getDeviceSchData() {

        progressDialog = new ProgressDialog(FanSchedulerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyDevMac = RequestBody.create(MediaType.parse("text/plain"), device.getDevMac());
        RequestBody bodyDevType = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(device.getDevType()));

        Log.e("Parameter : ", "--------- User Id : " + bodyUserId + "              MAC : " + bodyDevMac + "             TYPE : " + bodyDevType);

        Call<RespSchedulerData> call = Constants.myInterface.getSchedulerList(bodyUserId, bodyDevMac, bodyDevType);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();

                    Log.e("## Scheduler List ", " is \n\n " + response.body().toString());

                    if (!respSchedulerData.isError()) {

                        fanSchedulerList = new ArrayList<RespScheduler>();


                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {


                            fanSchedulerList.add(respScheduler);

                        }

                        schedulerFanAdapter = new SchedulerFanAdapter(getApplicationContext(), fanSchedulerList, FanSchedulerActivity.this);
                        lvFanSch.setAdapter(schedulerFanAdapter);

                    }


                } catch (Exception e) {
                    Log.e("Exception ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Exception ", " .. " + t.getMessage());

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Variables.isInternetAvailable) {
            getDeviceSchData();
        } else {
            Toast.makeText(this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
        }
    }
}
