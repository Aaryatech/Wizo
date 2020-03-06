package com.ats.wizo.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by maxadmin on 12/1/18.
 */

public class WizoDeviceConnection {

    public static final String TAG = "WizoWifiService";

    public static void connectToWPAWiFi(Activity activity, String ssid) {
        if(isConnectedTo(activity,ssid)){ //see if we are already connected to the given ssid
       //     Toast.makeText(activity.getApplicationContext(),"Already Connected To - "+ssid,Toast.LENGTH_SHORT).show();
            return;
        }
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //wifiConfig=wm.getWiFiConfig(ssid);
        WifiConfiguration wifiConfig=getWifiConfig(activity,ssid);

        if(wifiConfig == null){
            createWPAProfile(activity,ssid);
            wifiConfig=getWifiConfig(activity,ssid);
        }
        wm.disconnect();
        try {
            wm.enableNetwork(wifiConfig.networkId, true);
            wm.reconnect();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private static void createWPAProfile(Activity activity, String ssid) {

        //  WifiConfiguration newConfig=new WifiConfiguration();
        // newConfig.SSID=ssid;
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID=ssid;

        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int id = wm.addNetwork(wc);


        Log.e(TAG,"saved SSID to WiFiManger");

    }

    private static WifiConfiguration getWifiConfig(Activity activity, String ssid) {
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworkList= wm.getConfiguredNetworks();

        for (WifiConfiguration config : configuredNetworkList){
            if(config.SSID != null && config.SSID.equals(ssid)){
                Log.e("Previous "," Wifi "+configuredNetworkList.toString());

                return  config;
            }
        }
        return null;
    }

    private static boolean isConnectedTo(Activity activity, String ssid) {
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Log.e("with qoutes "+ssid, "SSID Substring "+ssid.substring(1,ssid.length()-1));
        Log.e(TAG,"current ssid "+wm.getConnectionInfo().getSSID());

        if(wm.getConnectionInfo().getSSID().equals(ssid)   ){
            Log.e(TAG," Already Connected");
            return true;
        }
        return false;
    }


    public static class WiFiChangeBrdRcr extends BroadcastReceiver {

        private static final String TAG = "WiFiChangeBrdRcr";

        public WiFiChangeBrdRcr() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getState()==NetworkInfo.State.CONNECTED){
                String bssid=intent.getStringExtra(WifiManager.EXTRA_BSSID);
                Log.e(TAG,"Connected To BSSID "+bssid);

                WifiInfo wifiInfo=(WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                String ssid=wifiInfo.getSSID();

                Log.e(TAG,"Connected To SSID "+ssid);
             //   Toast.makeText(context,"Conncted To "+ssid,Toast.LENGTH_SHORT);


            }

        }
    }
}
