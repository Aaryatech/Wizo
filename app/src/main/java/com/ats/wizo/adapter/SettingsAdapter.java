package com.ats.wizo.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.AddNewMoodActivity;
import com.ats.wizo.activity.AddNewRoomActivity;
import com.ats.wizo.activity.HelpWebViewActivity;
import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.activity.LoginActivity;
import com.ats.wizo.activity.ProfileActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.MoodDetailList;
import com.ats.wizo.model.MoodDeviceMapping;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.MoodsList;
import com.ats.wizo.model.RefreshList;
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

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
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

import static android.content.Context.WIFI_SERVICE;
import static com.ats.wizo.activity.HomeActivity.routerList;

/**
 * Created by MIRACLEINFOTAINMENT on 28/02/18.
 */

public class SettingsAdapter extends ArrayAdapter<String> {

    private List<String> settingsList;
    Context mContext;
    Activity activity;
    ProgressDialog progressDialog;


    private static class ViewHolder {
        TextView tvSetting;
        ImageView ivIcon;
    }

    public SettingsAdapter(@NonNull Context context, List<String> settingsList, Activity activity) {
        super(context, R.layout.settings_adapter, settingsList);
        this.mContext = context;
        this.settingsList = settingsList;
        this.activity = activity;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.settings_adapter, parent, false);

            viewHolder.tvSetting = convertView.findViewById(R.id.tvSetting);
            viewHolder.ivIcon = convertView.findViewById(R.id.ivIcon);

            viewHolder.tvSetting.setText(settingsList.get(position));


            if (position == 0) {

                viewHolder.ivIcon.setImageResource(R.mipmap.ic_new_room);

                //   viewHolder.ivIcon.setImageResource(R.mipmap.ic_upload);
            } else if (position == 1) {
               // viewHolder.ivIcon.setImageResource(R.mipmap.ic_download);


                viewHolder.ivIcon.setImageResource(R.mipmap.ic_mood);

            }else if (position == 2) {
               // viewHolder.ivIcon.setImageResource(R.mipmap.ic_mood);

                viewHolder.ivIcon.setImageResource(R.mipmap.ic_upload);
            } else if (position == 3) {
               // viewHolder.ivIcon.setImageResource(R.mipmap.ic_new_room);
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_download);
            }/* else if (position == 4) {
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_home_mode);

            }*/ else if (position == 4) {
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_refresh);

            } else if (position == 5) {
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_profile);

            } else if (position == 6) {
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_if_help_web);

            }else if (position == 7) {
                viewHolder.ivIcon.setImageResource(R.mipmap.ic_logout);

            }

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (position == 2) {

                    if (isOnline(mContext)) {
                        registerDeviceToServer();
                    }
                } else if (position == 3) {
                    // Download Data
                    if (isOnline(mContext)) {
                        getRoomDataFromServer();
                    }

                } else if (position == 1) {
                    // Add New Mood
                    if (isOnline(mContext)) {

                        activity.startActivity(new Intent(mContext, AddNewMoodActivity.class));

                    }

                }


                else if (position == 0) {

                    //Add New Room

                    if (isOnline(mContext)) {

                        activity.startActivity(new Intent(mContext, AddNewRoomActivity.class));

                    }

                } else if (position == 452) { //unused as home mode is removed

                    // Switch To Home Mode

                    try {

                        if (Variables.isManualHomeMode) {
                            Variables.isManualHomeMode = false;
                            Toast.makeText(mContext, "Switched To Internet Mode", Toast.LENGTH_SHORT).show();
                            activity.startActivity(new Intent(mContext, HomeActivity.class));
                            activity.finish();
                        } else {

                            String homeRouter="Home";
                            ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                            android.net.NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                            if (wifi.isAvailable() && wifi.isConnected()) {
                                Log.i("ConnectivityReceiver ", "Found WI-FI Network");

                                WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();


                                String ssid = wifiInfo.getSSID();

                                Log.e("Current WiFi", ".. " + ssid);
                                Log.e("Home Router List", ".. " + routerList.toString());


                                for (int j = 0; j < routerList.size(); j++) {
                                    if (ssid.equals("\"" + routerList.get(j) + "\"")) {

                                        Variables.isManualHomeMode = true;
                                        Variables.isAtHome = true;
                                        Variables.isInternetAvailable = false;
                                        Toast.makeText(mContext, "Switched To Home Mode", Toast.LENGTH_SHORT).show();
                                        homeRouter =routerList.get(j);
                                        activity.startActivity(new Intent(mContext, HomeActivity.class));
                                        activity.finish();
                                        Log.e("Manual Mode", " At Home ");
                                    }

                                }


                            }
                            if (!Variables.isAtHome) {
                                Toast.makeText(mContext, "Please Connect To \"" + homeRouter + "\" Network", Toast.LENGTH_SHORT).show();

                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (position == 4) {

                    //Synch With Router

                    try {

                        NewAsynchUpdateIP newAsynchUpdateIP = new NewAsynchUpdateIP();
                        newAsynchUpdateIP.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (position == 5) {
                    //My Profile

                    if (isOnline(mContext)) {
                        // start new profile act

                        activity.startActivity(new Intent(mContext, ProfileActivity.class));
                    }

                } else if (position == 6) {
                    //Manual Configuration
                    activity.startActivity(new Intent(mContext, HelpWebViewActivity.class));

                }
                else if (position == 7) {
                    //Logout
                    logout();

                }


            }
        });


        return convertView;
    }

    private void logout() {


        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Logout");
        builder.setMessage("Do you really want to Logout ?");

        String positiveText = mContext.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        SharedPreferences settings = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                        settings.edit().clear().commit();
                        HomeActivity.getInstance().finish();
                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.truncateAllTables();
                        activity.startActivity(new Intent(mContext, LoginActivity.class));
                        activity.finish();

                    }
                });

        String negativeText = mContext.getString(android.R.string.cancel);
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


    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Uploading Data");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        DBHandler dbHandler = new DBHandler(mContext);
        List<Device> allDeviceList = dbHandler.getAllDevices();
        List<DataUploadDevices> deviceList = new ArrayList<>();

        for (Device device : allDeviceList) {

            DataUploadDevices dataUploadDevices = new DataUploadDevices(Integer.parseInt(Constants.userId), device.getDevIp(), device.getDevMac(), device.getDevCaption(), device.getDevType(), device.getDevPosition(), device.getDevSsid(), device.getDevRoomId(), device.getDevIsUsed());

            deviceList.add(dataUploadDevices);
        }

        Log.e("Upload Device List"," "+deviceList.toString());

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
                        Toast.makeText(activity, "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Failed to upload data, Please try again...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();

            }
        });


    }


    public static boolean isOnline(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(context.getApplicationContext(), "No Internet connection ! ", Toast.LENGTH_LONG).show();
            Variables.isInternetAvailable = false;

            return false;
        }
        Variables.isInternetAvailable = true;
        return true;
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
                        Log.e("Device Data from "," server "+respDeviceData.toString());

                        DBHandler dbHandler = new DBHandler(mContext);
                        for (RespDevice respDevice : respDeviceData.getDeviceList()) {

                            Device device = new Device(respDevice.getDevCaption(), respDevice.getDevIp(), respDevice.getDevMac(), respDevice.getDevSsid(), respDevice.getDevType(), respDevice.getRoomId(), respDevice.getDevPosition(), respDevice.getDevIsUsed());
                            dbHandler.addNewDevice(device);

                        }

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

                try{

                    RespMoodList respMoodList=response.body();

                    Log.e("Moods Data ",""+respMoodList.toString());

                     if(!respMoodList.getError()){

                         List<MoodsList>moodsLists=respMoodList.getMoodsList();

                         for(MoodsList moodsList :moodsLists){

                             MoodMaster master= new MoodMaster();

                             master.setMoodName(moodsList.getMoodHeader().getMoodName());
                             master.setMoodId(moodsList.getMoodHeader().getMoodHeaderId());

                             DBHandler dbHandler=new DBHandler(mContext);
                             dbHandler.addNewMood(master);

                             List<MoodDetailList> moodDetailLists=moodsList.getMoodDetailList();

                             for (MoodDetailList detailList :moodDetailLists){

                                 MoodDeviceMapping moodDeviceMapping=new MoodDeviceMapping();
                                 
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

                }catch (Exception e){
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

                            DBHandler dbHandler = new DBHandler(mContext);
                            dbHandler.addNewMac(scanDevice.getDevMac());

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(activity, "Data Downloaded Successfully", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onFailure(Call<RespScanData> call, Throwable t) {

            }
        });


    }


    private void getRoomDataFromServer() {
        progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
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


                    if (!respRoomData.getRoomList().isEmpty()) {

                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.truncateAllTables();
                        for (Room room : respRoomData.getRoomList()) {
                            dbHandler.addNewRoom(room);
                        }
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


    private class NewAsynchUpdateIP extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog;
        Context context;
        DBHandler dbHandler;
        List<Device> allDeviceList;
        List<Device> updatedDeviceList;
        boolean isFinished;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context = mContext;

            dbHandler = new DBHandler(context);

            allDeviceList = dbHandler.getAllDevices();
            updatedDeviceList = new ArrayList<Device>();

            isFinished = false;
            progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
            progressDialog.setTitle("Syncing Your Devices");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {


                updatedDeviceList.clear();

                WifiManager wifiMgr = (WifiManager) context.getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                String addr = Formatter.formatIpAddress(ip);
                //String addr = allDeviceList.get(0).getDevIp();



                for (int i = 0; i <= 255; i++) {

                    if (!isFinished) {

                        addr = addr.substring(0, addr.lastIndexOf('.') + 1) + i;
                        Log.e("inside synch", "  method "+addr);

                        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://" + addr)
                                .addCallAdapterFactory(rxAdapter)
                                .addConverterFactory(GsonConverterFactory.create()).build();


                        MyInterface myInterface = retrofit.create(MyInterface.class);

                        Observable<JsonObject> call = myInterface.getSynch("");

                        call.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.e("Task completed", "..");
                                        if (allDeviceList.size() == updatedDeviceList.size()) {

                                            isFinished = true;
                                            Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e("New Json", "Error" + e.getMessage());

                                    }

                                    @Override
                                    public void onNext(JsonObject jsonObject) {
                                        Log.e("New Json", ".." + jsonObject.toString());

                                        JSONObject object = null;
                                        try {
                                            object = new JSONObject(jsonObject.toString());

                                            RefreshList macList = new RefreshList();
                                            macList.setStatus(object.getString("deviceId"));
                                            macList.setIp(object.getString("ip"));
                                            macList.setMac(object.getString("mac"));

                                            Device device = new Device();
                                            device.setDevId(Integer.parseInt(object.getString("deviceId")));
                                            device.setDevIp(object.getString("ip"));
                                            device.setDevMac(object.getString("mac"));
                                            updatedDeviceList.add(device);

                                            dbHandler.updateDeviceIP(macList.getIp(), macList.getMac());

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });


                    } else {

                        break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
//            context.startActivity(new Intent(context, HomeActivity.class));
//            activity.finish();

        }
    }


}
