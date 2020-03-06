package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.NewMoodDeviceAdapter;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddNewDeviceToMoodActivity extends AppCompatActivity {

    Button btnAddNewDevice;
    ListView lvMoodDevice;

    List<Device> deviceList;

    int moodId;
    private ProgressDialog progressDialog;

    public static List<MoodDevice> staticNewSelectedDeviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_device_to_mood);

        lvMoodDevice=findViewById(R.id.lvMoodDevice);

        btnAddNewDevice=findViewById(R.id.btnAddNewDevice);

        moodId=getIntent().getIntExtra("moodId",0);

        getDeviceList(moodId);

        btnAddNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addNewDevice();

            }
        });

    }

    private void addNewDevice() {

        progressDialog=new ProgressDialog(AddNewDeviceToMoodActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();



        DBHandler dbHandler=new DBHandler(getApplicationContext());

       /* Log.e("ADD NEW MOOD TO ACT","--------------------***********************--------------- "+staticNewSelectedDeviceList);

        for(MoodDevice moodDevice :staticNewSelectedDeviceList) {

            MoodDeviceMapping mapping = new MoodDeviceMapping();

            mapping.setMoodId(moodId);
            mapping.setMoodDevMac(moodDevice.getDevMac());
            mapping.setMoodDevType(moodDevice.getDevType());
            mapping.setMoodOperation(moodDevice.getOperation());
            mapping.setMoodDevType(moodDevice.getDevType());

            dbHandler.addNewDeviceToMood(mapping);

        }*/


        // Uploading Device To Server

        MoodMaster moodMaster=dbHandler.getMoodById(String.valueOf(moodId));

        MoodHeader moodHeader=new MoodHeader();

        moodHeader.setUserId(Integer.parseInt(Constants.userId));
        moodHeader.setMoodName(moodMaster.getMoodName());
        moodHeader.setMoodHeaderId(moodId);


        List<MoodDetail>moodDetailList=new ArrayList<>();

        for(MoodDevice moodDevice :staticNewSelectedDeviceList){

            MoodDetail moodDetail=new MoodDetail();
            moodDetail.setMoodHeaderId(moodId);
            moodDetail.setDevMac(moodDevice.getDevMac());
            moodDetail.setDevType(moodDevice.getDevType());
            moodDetail.setLocalDevId(moodDevice.getDevId());

            moodDetailList.add(moodDetail);
        }

        final PostNewMood postNewMood=new PostNewMood();
        postNewMood.setMoodHeader(moodHeader);
        postNewMood.setMoodDetailList(moodDetailList);

        Log.e("AddNewDevToMood ACT ","------------NEW MOOD DEVICE : ----"+postNewMood.toString());

        Call<RespAddNewMood> call = Constants.myInterface.addNewDeviceToMood(postNewMood);
        call.enqueue(new Callback<RespAddNewMood>() {
            @Override
            public void onResponse(Call<RespAddNewMood> call, Response<RespAddNewMood> response) {


                Log.e("AddNewDevToMood ACT -", " resp " + response.body());

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespAddNewMood respAddNewMood = response.body();

                    if (!respAddNewMood.isError()) {


                        DBHandler dbHandler = new DBHandler(AddNewDeviceToMoodActivity.this);

                        List<Device> devList = dbHandler.getDevicesByMoodId(moodId);



                        try {

                            if (respAddNewMood.getPostNewMood().getMoodDetailList().size() > 0) {
                                for (int i = 0; i < respAddNewMood.getPostNewMood().getMoodDetailList().size(); i++) {

                                    MoodDetail moodDetail = respAddNewMood.getPostNewMood().getMoodDetailList().get(i);

                                    boolean flag=false;

                                    if (devList!=null){
                                        for (int j=0;j<devList.size();j++){

                                            if (devList.get(j).getDevType()==moodDetail.getDevType() && devList.get(j).getDevMac().equalsIgnoreCase(moodDetail.getDevMac())){
                                                flag=true;
                                            }

                                        }

                                        if (!flag){

                                            MoodDeviceMapping mapping = new MoodDeviceMapping();
                                            mapping.setMoodId(moodId);
                                            mapping.setMoodDevMac(moodDetail.getDevMac());
                                            mapping.setMoodDevType(moodDetail.getDevType());
                                            mapping.setMoodOperation(moodDetail.getOperation());
                                            mapping.setMoodDetailId(moodDetail.getMoodDetailId());

                                            dbHandler.addNewDeviceToMood(mapping);


                                        }

                                    }else{

                                        MoodDeviceMapping mapping = new MoodDeviceMapping();
                                        mapping.setMoodId(moodId);
                                        mapping.setMoodDevMac(moodDetail.getDevMac());
                                        mapping.setMoodDevType(moodDetail.getDevType());
                                        mapping.setMoodOperation(moodDetail.getOperation());
                                        mapping.setMoodDetailId(moodDetail.getMoodDetailId());

                                        dbHandler.addNewDeviceToMood(mapping);


                                    }


                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                       /* for(MoodDevice moodDevice :staticNewSelectedDeviceList) {

                            MoodDeviceMapping mapping = new MoodDeviceMapping();

                            mapping.setMoodId(moodId);
                            mapping.setMoodDevMac(moodDevice.getDevMac());
                            mapping.setMoodDevType(moodDevice.getDevType());
                            mapping.setMoodOperation(moodDevice.getOperation());
                            mapping.setMoodDevType(moodDevice.getDevType());

                            dbHandler.addNewDeviceToMood(mapping);

                        }*/


                        Toast.makeText(AddNewDeviceToMoodActivity.this, "Device added Successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    } else {
                        Toast.makeText(AddNewDeviceToMoodActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


               /* try {
                    if (progressDialog.isShowing()) {

                        progressDialog.dismiss();

                    }

                    Log.e("Delete Mood ", " respo " + response.body().toString());

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {

                        Toast.makeText(AddNewDeviceToMoodActivity.this, "Mood Created Successfully", Toast.LENGTH_SHORT).show();

                        onBackPressed();
                    } else {

                        Toast.makeText(AddNewDeviceToMoodActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

                    }
                }catch (Exception e){

                    e.printStackTrace();
                }*/

            }

            @Override
            public void onFailure(Call<RespAddNewMood> call, Throwable t) {
                Toast.makeText(AddNewDeviceToMoodActivity.this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void getDeviceList(int moodId) {

        DBHandler dbHandler= new DBHandler(getApplicationContext());
        deviceList= dbHandler.getAllDevicesByMoodId(moodId);

        Log.e("Device List "," "+deviceList.toString());

        List<Room> roomList=dbHandler.getAllRooms();

        Log.e("Room List "," "+roomList.toString());

        List<MoodDevice> allDevicesList=new ArrayList<>();
        for(Room room:roomList){

            MoodDevice moodDevice=new MoodDevice();
            moodDevice.setSelected(false);
            moodDevice.setRoomName(room.getRoomName());
            moodDevice.setHeader(true);


            List<Device> list=dbHandler.getAllDevicesByRoomId(room.getRoomId());
            if(!list.isEmpty()){
                allDevicesList.add(moodDevice);
            }

            Log.e("Device List for "+room.getRoomName()," is "+list.toString());

            for(Device device :list){

                boolean isPrev=false;
                for(Device device1 :deviceList){

                    if(device.getDevId()==device1.getDevId()){
                        isPrev=true;
                    }

                }

                if(!isPrev) {

                    MoodDevice moodDevice2 = new MoodDevice();
                    moodDevice2.setSelected(false);
                    moodDevice2.setHeader(false);
                    moodDevice2.setDevId(device.getDevId());
                    moodDevice2.setDevMac(device.getDevMac());
                    moodDevice2.setDevCaption(device.getDevCaption());
                    moodDevice2.setDevType(device.getDevType());
                    moodDevice2.setOperation(device.getOperation());
                    moodDevice2.setDetailId(device.getDetailId());

                    allDevicesList.add(moodDevice2);
                }
            }

        }

        Log.e(" \n\n Adapter List "," \n\n "+allDevicesList.toString());


        staticNewSelectedDeviceList=new ArrayList<>();
        NewMoodDeviceAdapter moodDeviceAdapter=new NewMoodDeviceAdapter(getApplicationContext(),allDevicesList,AddNewDeviceToMoodActivity.this);
        lvMoodDevice.setAdapter(moodDeviceAdapter);


//        MoodDeviceListAdapter moodDeviceListAdapter=new MoodDeviceListAdapter(getApplicationContext(),deviceList,MoodDeviceListActivity.this);
//        lvDeviceList.setAdapter(moodDeviceListAdapter);

    }
}
