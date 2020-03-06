package com.ats.wizo.common;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by maxadmin on 8/1/18.
 */

public class Variables {

    public static boolean isMQTTConnected=false;
    public static ArrayList<String> subscribedTopics=new ArrayList<String>();
    public static SharedPreferences sh;
    public static SharedPreferences.Editor e;

    public static boolean isInternetAvailable;
    public static boolean isAtHome;
    public static boolean isManualHomeMode=false;

    public static boolean needToShowConnectivity;

    public static boolean isStatusReceived =false;

}
