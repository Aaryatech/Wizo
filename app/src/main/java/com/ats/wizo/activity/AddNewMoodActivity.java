package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeTransform;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.MoodDeviceAdapter;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.MoodDetail;
import com.ats.wizo.model.MoodDevice;
import com.ats.wizo.model.MoodDeviceMapping;
import com.ats.wizo.model.MoodHeader;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.PostNewMood;
import com.ats.wizo.model.RespAddNewMood;
import com.ats.wizo.model.Room;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewMoodActivity extends AppCompatActivity {

    // RecyclerView recyclerView;
    //  MoodDeviceAdapter adapter;

    Context mContext;
    ListView lvMoodDevice;
    public static List<MoodDevice> selectedDeviceList;

    EditText etMoodName;
    Button btnCreate;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_add_new_mood);
        //  recyclerView =  findViewById(R.id.rvMoodDevice);
        lvMoodDevice = findViewById(R.id.lvMoodDevice);

        etMoodName = findViewById(R.id.etMoodName);
        btnCreate = findViewById(R.id.btnCreate);


        final DBHandler dbHandler = new DBHandler(getApplicationContext());

        List<Room> roomList = dbHandler.getAllRooms();

        Log.e("Room List ", " " + roomList.toString());
        List<MoodDevice> allDevicesList = new ArrayList<>();
        for (Room room : roomList) {

            MoodDevice moodDevice = new MoodDevice();
            moodDevice.setSelected(false);
            moodDevice.setRoomName(room.getRoomName());
            moodDevice.setHeader(true);

            List<Device> deviceList = dbHandler.getAllDevicesByRoomId(room.getRoomId());
            if (!deviceList.isEmpty()) {
                allDevicesList.add(moodDevice);
            }

            Log.e("Device List for " + room.getRoomName(), " is " + deviceList.toString());

            for (Device device : deviceList) {

                MoodDevice moodDevice2 = new MoodDevice();
                moodDevice2.setSelected(false);
                moodDevice2.setHeader(false);
                moodDevice2.setDevId(device.getDevId());
                moodDevice2.setDevMac(device.getDevMac());
                moodDevice2.setDevType(device.getDevType());
                moodDevice2.setDevCaption(device.getDevCaption());
                moodDevice2.setOperation(device.getOperation());
                moodDevice2.setDetailId(device.getDetailId());

                allDevicesList.add(moodDevice2);
            }


        }
        Log.e(" \n\n Adapter List ", " \n\n " + allDevicesList.toString());


        selectedDeviceList = new ArrayList<>();
        MoodDeviceAdapter moodDeviceAdapter = new MoodDeviceAdapter(getApplicationContext(), allDevicesList, AddNewMoodActivity.this);
        lvMoodDevice.setAdapter(moodDeviceAdapter);

//
//        adapter = new MoodDeviceAdapter(allDevicesList);
//        LinearLayoutManager manager = new LinearLayoutManager(AddNewMoodActivity.this);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(manager);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
//                manager.getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
//        recyclerView.setAdapter(adapter);


        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String moodName = etMoodName.getText().toString();

                if (moodName.equalsIgnoreCase("")) {

                    etMoodName.setError("Please enter mood name");
                    etMoodName.requestFocus();

                } else if (selectedDeviceList.size() < 2) {

                    Toast.makeText(AddNewMoodActivity.this, "Please select at least two switches", Toast.LENGTH_SHORT).show();

                } else {

                    Log.e("Selected switches ", " " + selectedDeviceList.toString());


                    // Uploading Mood To Server

                    MoodHeader moodHeader = new MoodHeader();
                    moodHeader.setMoodName(etMoodName.getText().toString());
                    moodHeader.setUserId(Integer.parseInt(Constants.userId));


                    List<MoodDetail> moodDetailList = new ArrayList<>();

                    for (MoodDevice moodDevice : selectedDeviceList) {

                        MoodDetail moodDetail = new MoodDetail();
                        moodDetail.setDevMac(moodDevice.getDevMac());
                        moodDetail.setDevType(moodDevice.getDevType());
                        moodDetail.setLocalDevId(moodDevice.getDevId());

                        moodDetailList.add(moodDetail);
                    }

                    PostNewMood postNewMood = new PostNewMood();
                    postNewMood.setMoodHeader(moodHeader);
                    postNewMood.setMoodDetailList(moodDetailList);

                    uploadMoodToServer(postNewMood);

                }


            }
        });


    }

    private void uploadMoodToServer(PostNewMood postNewMood) {

        progressDialog = new ProgressDialog(AddNewMoodActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage("Please wait ...");
        progressDialog.show();


        Call<RespAddNewMood> call = Constants.myInterface.addNewMood(postNewMood);

        call.enqueue(new Callback<RespAddNewMood>() {
            @Override
            public void onResponse(Call<RespAddNewMood> call, Response<RespAddNewMood> response) {

                Log.e("Add New Mood ", " resp " + response.body());

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespAddNewMood respAddNewMood = response.body();

                    if (!respAddNewMood.isError()) {

                        MoodMaster master = new MoodMaster();
                        master.setMoodName(etMoodName.getText().toString());
                        master.setMoodId(respAddNewMood.getPostNewMood().getMoodHeader().getMoodHeaderId());

                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.addNewMood(master);

                        int id = respAddNewMood.getPostNewMood().getMoodHeader().getMoodHeaderId();

                        Log.e("Mood Id ", " " + id);

                        try {

                            if (respAddNewMood.getPostNewMood().getMoodDetailList().size() > 0) {
                                for (int i = 0; i < respAddNewMood.getPostNewMood().getMoodDetailList().size(); i++) {

                                    MoodDetail moodDetail = respAddNewMood.getPostNewMood().getMoodDetailList().get(i);

                                    MoodDeviceMapping mapping = new MoodDeviceMapping();
                                    mapping.setMoodId(id);
                                    mapping.setMoodDevMac(moodDetail.getDevMac());
                                    mapping.setMoodDevType(moodDetail.getDevType());
                                    mapping.setMoodOperation(moodDetail.getOperation());
                                    mapping.setMoodDetailId(moodDetail.getMoodDetailId());

                                    dbHandler.addNewDeviceToMood(mapping);

                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                      /*  for (MoodDevice moodDevice : selectedDeviceList) {
                            MoodDeviceMapping mapping = new MoodDeviceMapping();
                            mapping.setMoodId(id);
                            mapping.setMoodDevMac(moodDevice.getDevMac());
                            mapping.setMoodDevType(moodDevice.getDevType());
                            mapping.setMoodOperation(moodDevice.getOperation());
                            mapping.setMoodDetailId(moodDevice.getDetailId());

                            dbHandler.addNewDeviceToMood(mapping);

                        }*/


                        Toast.makeText(AddNewMoodActivity.this, "Mood Created Successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    } else {
                        Toast.makeText(AddNewMoodActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<RespAddNewMood> call, Throwable t) {

                progressDialog.dismiss();

                Log.e("Add New Mood ", " failed " + t.getMessage());
            }
        });

    }


}
