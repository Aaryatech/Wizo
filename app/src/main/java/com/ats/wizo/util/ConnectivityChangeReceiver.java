package com.ats.wizo.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.activity.LoginActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.mqtt.MqttConnection;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ats.wizo.activity.HomeActivity.routerList;
import static com.ats.wizo.activity.HomeActivity.topicList;
import static com.ats.wizo.common.Variables.isInternetAvailable;
import static com.ats.wizo.common.Variables.isMQTTConnected;
import static com.ats.wizo.common.Variables.needToShowConnectivity;
import static com.ats.wizo.common.Variables.subscribedTopics;

/**
 * Created by MIRACLEINFOTAINMENT on 26/02/18.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NetworkChangeReceiver";
    private boolean isConnected = false;
    private View view;
    Snackbar posSnackbar;
    Snackbar negSnackbar;

    public ConnectivityChangeReceiver(View view) {
        this.view = view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Received notification about network status");
        isNetworkAvailable(context);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            Log.v(LOG_TAG, "is connected to Internet!" +isOnline());

                           if(!Variables.isManualHomeMode) {
                                isConnected = isOnline();
                                showSnack(isConnected);
                           }else{
                               return true;

                           }
                            if (isConnected) {
                                isInternetAvailable = true;
                                Variables.isAtHome = false;

                                if (!isMQTTConnected || subscribedTopics.size() < topicList.size()) {

                                    MqttConnection.initializeMQTT(context, topicList);
                                }

                            } else {
                                isInternetAvailable = false;
                                Variables.isAtHome = false;
                                needToShowConnectivity=true;
                                android.net.NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                try {
                                    Log.e("Connectivity Changed","Wifi Info "+wifi.toString());
                                }catch (Exception e){
                                    e.printStackTrace();

                                }
                                if (wifi.isAvailable() && wifi.isConnected()) {
                                    Log.i("ConnectivityReceiver ", "Found WI-FI Network");

                                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();


                                    String ssid = wifiInfo.getSSID();

                                    Log.e("Current WiFi", ".. " + ssid);

                                    for (int j = 0; j <routerList.size(); j++) {
                                        if (ssid.equals("\"" + routerList.get(j) + "\"")) {

                                            Variables.isAtHome = true;

                                            Log.e("No Internet ", " But at home ");
                                        }

                                    }


                                }

                            }


                        }
                        return true;
                    }
                }
            }
        }
        Log.v(LOG_TAG, "You are not connected to Internet!");
        //  Toast.makeText(context, "Internet NOT available via Broadcast receiver", Toast.LENGTH_SHORT).show();
        isConnected = false;
        needToShowConnectivity=true;
        isInternetAvailable= false;
        Variables.isAtHome = false;

        showSnack(false);
        return false;
    }


    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {

            if(negSnackbar != null && negSnackbar.isShown()) {
                message = "Connected To Internet";
                color = Color.WHITE;


                    negSnackbar.dismiss();


                 posSnackbar = Snackbar
                        .make(view, message, Snackbar.LENGTH_LONG);


                View sbView = posSnackbar.getView();

                CoordinatorLayout.LayoutParams params=(CoordinatorLayout.LayoutParams)sbView.getLayoutParams();
                params.gravity = Gravity.TOP;
                sbView.setLayoutParams(params);
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                } else {
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                posSnackbar.show();
            }
        } else {

            if(negSnackbar== null || !negSnackbar.isShown()) {
                message = "No Internet Connection";



               // color = Color.RED;
                color = Color.parseColor("#FFEA5E5B");;
                negSnackbar = Snackbar
                        .make(view, message, Snackbar.LENGTH_LONG);
                negSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                View sbView = negSnackbar.getView();

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) sbView.getLayoutParams();
                params.gravity = Gravity.TOP;
                sbView.setLayoutParams(params);

                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                } else {
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                negSnackbar.show();
            }
        }


    }

    public Boolean isOnline() {


        InetAddress inetAddress=null;

        try {

            Future<InetAddress>future= Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() throws Exception {
                    try{
                        return  InetAddress.getByName("google.com");

                    }catch (UnknownHostException e){
                        return null;
                    }
                }
            });
            inetAddress=future.get(5000, TimeUnit.MILLISECONDS);
            future.cancel(true);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return inetAddress!=null && !inetAddress.equals("");


    }
}