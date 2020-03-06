package com.ats.wizo.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by maxadmin on 10/1/18.
 */

public class WiFiService {

    public static final String TAG = "WiFiService";

    public static void connectToWPAWiFi(Activity activity, String ssid, String pass) {

        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
           // wm.disconnect();
      /*  if(isConnectedTo(activity,ssid)){ //see if we are already connected to the given ssid
           // Toast.makeText(activity.getApplicationContext(),"Already Connected To - "+ssid,Toast.LENGTH_SHORT).show();
            return;
        }*/
        WifiConfiguration wifiConfig=getWifiConfig(activity,ssid);

        if(wifiConfig == null){
            createWPAProfile(activity,ssid,pass);
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

    private static void createWPAProfile(Activity activity, String ssid, String pass) {

        WifiConfiguration newConfig=new WifiConfiguration();
        newConfig.SSID=ssid;
        if(pass!="" || pass!=null) {
            newConfig.preSharedKey = pass;
        }
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wm.addNetwork(newConfig);
        Log.e(TAG,"saved SSID to WiFiManger");

    }

    private static WifiConfiguration getWifiConfig(Activity activity, String ssid) {
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworkList= wm.getConfiguredNetworks();
        for (WifiConfiguration config : configuredNetworkList){
            if(config.SSID != null && config.SSID.equals(ssid)){
                return  config;
            }
        }
        return null;
    }

    public static boolean isConnectedTo(Activity activity, String ssid) {
        WifiManager wm=(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Log.e(TAG," Connetced "+wm.getConnectionInfo().getSSID());

        if(wm.getConnectionInfo().getSSID().equals(ssid)){
            Log.e(TAG," Already Connetced");
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

                WifiInfo wifiInfo=intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String ssid=wifiInfo.getSSID();
                Log.e(TAG,"Connected To SSID "+ssid);
           //     Toast.makeText(context,"Conncted To "+ssid,Toast.LENGTH_SHORT);
            }

        }
    }
}
