package com.ats.wizo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Device;
import com.ats.wizo.mqtt.MqttConnection;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.ConnectivityChangeReceiver;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ats.wizo.common.Variables.isInternetAvailable;
import static com.ats.wizo.common.Variables.needToShowConnectivity;

public class SplashActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private String TAG = "SplashActivity";
    private Button btnPublish;
    boolean islogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);


        }

        Variables.needToShowConnectivity=false;
        Variables.sh = getSharedPreferences("MyPref", MODE_PRIVATE);
        Variables.e = Variables.sh.edit();

        Constants.homeSSID = Variables.sh.getString("ssid", "");

        Constants.authKey = Variables.sh.getString("authKey", "");

        int uid = Variables.sh.getInt("userId", 0);
        Constants.userId = String.valueOf(uid);

        setUpOperationsMsg(Constants.authKey);


        Log.e("Home ","SSID "+Constants.homeSSID);

//        if (!Variables.isMQTTConnected) {
//            MqttConnection.initializeMQTT(getApplicationContext(), topicList);
//
//        }


    }

    private void setUpOperationsMsg(String authKey) {

        Constants.onOperation = authKey + "piMjVtYV";
        Constants.allOnOperation = authKey + "piMjVtYV#nolla";
        Constants.offOperation = authKey + "JhTVo1V1";
        Constants.allOffOperation = authKey + "JhTVo1V1#ffolla";
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "In Resume method");


        islogin = Variables.sh.getBoolean("isUserLogin", false);
        Log.e("Is User Login", ".. "+islogin);


        final Handler handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (islogin) {

                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {


                            startActivity(new Intent(getApplicationContext(), HelperTabActivity.class));
                        }
                    }

                }, 2000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "In Pause method");
    }

//
//    public boolean isInternetAvailable() {
//        try {
//            InetAddress ipAddr = InetAddress.getByName("www.google.com");
//
//            Log.e(TAG, "In internet check method " + ipAddr);
//
//
//            return !ipAddr.equals("");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }




}
