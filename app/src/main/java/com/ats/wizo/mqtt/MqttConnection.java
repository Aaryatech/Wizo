package com.ats.wizo.mqtt;

import android.content.Context;
import android.util.Log;

import com.ats.wizo.activity.DeviceListActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.CurrentStatus;
import com.ats.wizo.model.FanStatus;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ats.wizo.activity.DeviceListActivity.deviceListAdapter;
import static com.ats.wizo.activity.DeviceListActivity.fanStatusList;
import static com.ats.wizo.adapter.DeviceListAdapter.onList;
import static com.ats.wizo.common.Variables.isStatusReceived;
import static com.ats.wizo.common.Variables.subscribedTopics;
import static com.ats.wizo.constant.Constants.clientID;
import static com.ats.wizo.constant.Constants.mqttAndroidClient;
import static com.ats.wizo.constant.Constants.serverUri;

/**
 * Created by maxadmin on 9/1/18.
 */

public class MqttConnection {


    static boolean connection;

    private static MqttAndroidClient CreateInstance(MqttAndroidClient mqttAndroidClient, String clientId, String host, Context context) {
        //generate the client handle from its hash code
        try {

            mqttAndroidClient = new MqttAndroidClient(context, host, clientId);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MQTT Conn Class", "excep " + e.getMessage());

        }
        return mqttAndroidClient;
    }

    public static void initializeMQTT(Context context, final List<String> topicList) {
        try {

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientID + ts);

            mqttAndroidClient = MqttConnection.CreateInstance(mqttAndroidClient, clientID + ts, serverUri, context);

            try {
                boolean isConnected = mqttAndroidClient.isConnected();
                Log.e("Is Connected ", ".. " + isConnected);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception ", ".. " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MQTT Constant ", "excep " + e.getMessage());
        }

        try {

            final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);

          /*
            String willMsg="I'm going offline";
            MqttMessage message = new MqttMessage();
            message.setPayload(willMsg.getBytes());

            mqttConnectOptions.setWill("test",message.getPayload(),1,true);
         */

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.e("Connected", " To MQTT Constant Class");
                    String msg = "status";

                    Variables.isMQTTConnected = true;

                    MqttMessage message = new MqttMessage();
                    message.setPayload(msg.getBytes());

                    try {


                        for (int i = 0; i < topicList.size(); i++) {

                            if (!Variables.subscribedTopics.contains(topicList.get(i))) {
                                subscribeToTopic(mqttAndroidClient, topicList.get(i) + Constants.subscriptionTopic);
                                Variables.subscribedTopics.add(topicList.get(i));
                            }
                        }

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Log.e("Exception Constants ", "MQTT");
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Log.e("Fail to connect", " mqtt " + exception.getMessage());
                    Variables.isMQTTConnected = false;

                }
            });


            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                    Log.e("Connection lost", " ..");
                    Variables.isMQTTConnected = false;
                    subscribedTopics = new ArrayList<>();

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    Log.e("New json from ", "Constants Class  " + message.toString());

                    try {

                        JSONObject object = null;
                        try {
                            object = new JSONObject(message.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                       /* RefreshList macList = new RefreshList();
                        macList.setStatus(object.getString("MAC"));
                        macList.setMac(object.getString("MAC")); */

                        String mac = object.getString("mac");
                        String status = object.getString("status");

                        boolean isRequest = false;
                        CurrentStatus currentStatus = new CurrentStatus();
                        currentStatus.setMac(mac);


                        if (object.has("onRequest")) {
                            isRequest = true;
                            isStatusReceived = true;
                        }

                        if (object.has("devId")) {

                            String deviceId = object.getString("devId");

                            Log.e("MQTT CONN ", "---------- 175 ------------------------/////////////////////////////------------ DEV ID : " + deviceId);


                            if (deviceId.equalsIgnoreCase("3")) {

                                Log.e("\n\nFan Regulator", " setting status ");
//                                if(status.equalsIgnoreCase("")) {
//                                    RefreshList macList = new RefreshList();
//                                    macList.setStatus(object.getString("MAC"));
//                                    macList.setMac(object.getString("MAC"));
//
//
//                                }


                                switch (status) {
                                    case "on":

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }

                                        break;

                                    case "off":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }

                                        break;

                                    case "on1":

                                        for (int j = 0; j < onList.size(); j++) {

                                            Log.e("MQTT CONN - ", "------------------POSITION----------------- " + onList.get(j).getPosition());

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("On");
                                            }

                                        }
                                        break;
                                    case "on2":


                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("On");
                                            }

                                        }
                                        break;
                                    case "on3":


                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;

                                    case "0":
                                        Log.e("MQTT CONN", "****************************************************  0");

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                                onList.get(j).setStatus("0");

                                            }
                                        }
                                        break;

                                    case "1":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                                onList.get(j).setStatus("1");

                                            }
                                        }
                                        break;

                                    case "2":
                                        Log.e("MQTT CONN", "****************************************************  2");
                                        Log.e("ONLIST", "****************************************************  " + onList);

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                                onList.get(j).setStatus("2");

                                            }
                                        }
                                        break;

                                    case "3":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                                onList.get(j).setStatus("3");

                                            }
                                        }
                                        break;

                                    case "4":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                                onList.get(j).setStatus("4");

                                            }
                                        }
                                        break;

                                    case "on6":


                                        boolean isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay6("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        break;

                                    case "on7":

                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay7("on");

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay7("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        break;

                                    case "on8":


                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay8("on");

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay8("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                String device6 = statusOld.getRelay6();
                                                String device7 = statusOld.getRelay7();
                                                String device8 = statusOld.getRelay8();


                                            }

                                        }

                                        deviceListAdapter.notifyDataSetChanged();

                                        break;

                                    case "off1":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off2":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off3":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;


                                  /*  case "off6":


                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay6("off");
                                            fanStatusList.add(fanStatus);

                                        }

                                        break;

                                    case "off7":


                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay7("off");


                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay7("off");
                                            fanStatusList.add(fanStatus);

                                        }
                                        break;

                                    case "off8":

                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay8("off");

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay8("off");
                                            fanStatusList.add(fanStatus);

                                        }


                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                String device6 = statusOld.getRelay6();
                                                String device7 = statusOld.getRelay7();
                                                String device8 = statusOld.getRelay8();


                                                Log.e("relay status", "  " + statusOld.toString());

                                                if (device6.equalsIgnoreCase("off")) {

                                                    if (device7.equalsIgnoreCase("off")) {

                                                        if (device8.equalsIgnoreCase("off")) {

                                                            for (int j = 0; j < onList.size(); j++) {
                                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                                    onList.get(5).setStatus("0");
                                                                    Log.e("setting status", " On 0% ");
                                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                                }
                                                            }

                                                        } else if (device8.equalsIgnoreCase("on")) {

                                                            for (int j = 0; j < onList.size(); j++) {
                                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                                    onList.get(5).setStatus("100");
                                                                    Log.e("setting status", " On 100% ");
                                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                                }
                                                            }

                                                        }


                                                    } else if (device7.equalsIgnoreCase("on")) {

                                                        if (device8.equalsIgnoreCase("off")) {

                                                            for (int j = 0; j < onList.size(); j++) {
                                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                                    onList.get(5).setStatus("50");
                                                                    Log.e("setting status", " On 50% ");
                                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                                }
                                                            }

                                                        }

                                                    }

                                                } else if (device6.equalsIgnoreCase("on")) {


                                                    if (device8.equalsIgnoreCase("off")) {

                                                        if (device7.equalsIgnoreCase("off")) {

                                                            for (int j = 0; j < onList.size(); j++) {
                                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                                    onList.get(5).setStatus("25");
                                                                    Log.e("setting status", " On 25% ");
                                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                                }
                                                            }
                                                        } else if (device7.equalsIgnoreCase("on")) {

                                                            for (int j = 0; j < onList.size(); j++) {
                                                                if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                                    onList.get(5).setStatus("75");
                                                                    Log.e("setting status", " On 75%");
                                                                    // dbOperation.updateStatus(String.valueOf(j), "1", mac);
                                                                }
                                                            }

                                                        }

                                                    }

                                                }


                                            }

                                        }

                                        deviceListAdapter.notifyDataSetChanged();
                                        break;*/


                                    default:


                                }


                            } else if (deviceId.equalsIgnoreCase("5")) {

                                Log.e("MQTT CONN - LINE 657", "********************************************************* DEVICE ID : 5");

                                Log.e("\n\nFan Regulator", " setting status ");
//                                if(status.equalsIgnoreCase("")) {
//                                    RefreshList macList = new RefreshList();
//                                    macList.setStatus(object.getString("MAC"));
//                                    macList.setMac(object.getString("MAC"));
//
//
//                                }


                                if (status.equalsIgnoreCase("on")) {

                                    Log.e("MQTT CONN 5 - ", " on ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }
                                } else if (status.equalsIgnoreCase("off")) {

                                    Log.e("MQTT CONN 5 - ", " off ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }
                                } else if (status.equalsIgnoreCase("on1")) {

                                    Log.e("MQTT CONN 5 - ", " on1 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        Log.e("MQTT CONN - ", "------------------POSITION----------------- " + onList.get(j).getPosition());

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                            onList.get(j).setStatus("On");
                                        }

                                    }

                                } else if (status.equalsIgnoreCase("on2")) {

                                    Log.e("MQTT CONN 5 - ", " on2 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                            onList.get(j).setStatus("On");
                                        }

                                    }

                                } else if (status.equalsIgnoreCase("on3")) {

                                    Log.e("MQTT CONN 5 - ", " on3 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("on4")) {

                                    Log.e("MQTT CONN 5 - ", " on4 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("on5")) {

                                    Log.e("MQTT CONN 5 - ", " on5 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off1")) {

                                    Log.e("MQTT CONN 5 - ", " off1 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off2")) {

                                    Log.e("MQTT CONN 5 - ", " off2 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off3")) {

                                    Log.e("MQTT CONN 5 - ", " off3 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off4")) {

                                    Log.e("MQTT CONN 5 - ", " off4 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off5")) {

                                    Log.e("MQTT CONN 5 - ", " off5 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else {

                                    Log.e("MQTT CONN 5 - ", " ELSE  --LINE : 807-- " + status);

                                    for (int i = 0; i < Constants.dimmer1Speed; i++) {

                                        if (status.equalsIgnoreCase("A" + i)) {


                                            for (int j = 0; j < onList.size(); j++) {

                                                Log.e("MQTT CONN 5 - ", " A" + i);

                                                if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                    onList.get(j).setStatus("" + i);
                                                }
                                            }
                                        }


                                    }

                                    for (int i = 0; i < Constants.dimmer2Speed; i++) {

/*
                                    if (status.equalsIgnoreCase("B0")) {

                                        Log.e("MQTT CONN 5 - ", " B0");

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("50");
                                            }
                                        }
                                         }
*/


                                        if (status.equalsIgnoreCase("B" + i)) {

                                            Log.e("MQTT CONN 5 - ", " B" + i);


                                            for (int j = 0; j < onList.size(); j++) {

                                                if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                    onList.get(j).setStatus("" + i);
                                                }
                                            }
                                        }

                                    }

                                }


                               /* switch (status) {
                                    case "on":

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }

                                        break;

                                    case "off":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }

                                        break;

                                    case "on1":

                                        for (int j = 0; j < onList.size(); j++) {

                                            Log.e("MQTT CONN - ", "------------------POSITION----------------- " + onList.get(j).getPosition());

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("On");
                                            }

                                        }
                                        break;
                                    case "on2":


                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("On");
                                            }

                                        }
                                        break;
                                    case "on3":


                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;


                                    //Dimmer

                                    case "A0":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("0");
                                            }
                                        }
                                        break;

                                    case "A1":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("1");
                                            }
                                        }
                                        break;

                                    case "A2":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("2");
                                            }
                                        }
                                        break;

                                    case "A3":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("3");
                                            }
                                        }
                                        break;

                                    case "A4":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("4");
                                            }
                                        }
                                        break;

                                    case "A5":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("5");
                                            }
                                        }
                                        break;

                                    case "A6":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("6");
                                            }
                                        }
                                        break;

                                    case "A7":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("7");
                                            }
                                        }
                                        break;

                                    case "A8":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("8");
                                            }
                                        }
                                        break;

                                    case "A9":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("9");
                                            }
                                        }
                                        break;

                                    case "A10":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                                onList.get(j).setStatus("10");
                                            }
                                        }
                                        break;

                                    case "B0":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("0");
                                            }
                                        }
                                        break;

                                    case "B1":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("1");
                                            }
                                        }
                                        break;

                                    case "B2":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("2");
                                            }
                                        }
                                        break;

                                    case "B3":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("3");
                                            }
                                        }
                                        break;

                                    case "B4":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("4");
                                            }
                                        }
                                        break;

                                    case "B5":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("5");
                                            }
                                        }
                                        break;

                                    case "B6":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("6");
                                            }
                                        }
                                        break;

                                    case "B7":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("7");
                                            }
                                        }
                                        break;

                                    case "B8":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("8");
                                            }
                                        }
                                        break;

                                    case "B9":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("9");
                                            }
                                        }
                                        break;

                                    case "B10":

                                        for (int j = 0; j < onList.size(); j++) {

                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                                onList.get(j).setStatus("10");
                                            }
                                        }
                                        break;

                                    case "on6":


                                        boolean isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay6("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        break;

                                    case "on7":

                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay7("on");

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay7("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        break;

                                    case "on8":


                                        isPrev = false;
                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                isPrev = true;
                                                int index = fanStatusList.indexOf(statusOld);

                                                fanStatusList.get(index).setRelay8("on");

                                            }

                                        }

                                        if (!isPrev) {
                                            FanStatus fanStatus = new FanStatus();

                                            fanStatus.setMac(mac);
                                            fanStatus.setRelay8("on");
                                            fanStatusList.add(fanStatus);

                                        }


                                        for (FanStatus statusOld : fanStatusList) {

                                            if (statusOld.getMac().equalsIgnoreCase(mac)) {

                                                String device6 = statusOld.getRelay6();
                                                String device7 = statusOld.getRelay7();
                                                String device8 = statusOld.getRelay8();


                                            }

                                        }

                                        deviceListAdapter.notifyDataSetChanged();

                                        break;

                                    case "off1":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off2":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off3":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;

                                    default:


                                }*/


                            }else if(deviceId.equalsIgnoreCase("1")){


                                if (status.equalsIgnoreCase("on")) {

                                    Log.e("MQTT CONN 1 - ", " on ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }
                                } else if (status.equalsIgnoreCase("off")) {

                                    Log.e("MQTT CONN 1 - ", " off ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }
                                } else if (status.equalsIgnoreCase("on1")) {

                                    Log.e("MQTT CONN 1 - ", " on1 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        Log.e("MQTT CONN - ", "------------------POSITION----------------- " + onList.get(j).getPosition());

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                            onList.get(j).setStatus("On");
                                        }

                                    }

                                } else if (status.equalsIgnoreCase("on2")) {

                                    Log.e("MQTT CONN 1 - ", " on2 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                            onList.get(j).setStatus("On");
                                        }

                                    }

                                } else if (status.equalsIgnoreCase("on3")) {

                                    Log.e("MQTT CONN 1 - ", " on3 ");

                                    for (int j = 0; j < onList.size(); j++) {

                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("on4")) {

                                    Log.e("MQTT CONN 1 - ", " on4 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }

                                }  else if (status.equalsIgnoreCase("off1")) {

                                    Log.e("MQTT CONN 1 - ", " off1 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off2")) {

                                    Log.e("MQTT CONN 1 - ", " off2 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off3")) {

                                    Log.e("MQTT CONN 1 - ", " off3 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                } else if (status.equalsIgnoreCase("off4")) {

                                    Log.e("MQTT CONN 5 - ", " off4 ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                }



                            }else if(deviceId.equalsIgnoreCase("2")){


                                if (status.equalsIgnoreCase("on")) {

                                    Log.e("MQTT CONN 2 - ", " on ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                            onList.get(j).setStatus("On");

                                        }
                                    }
                                }   else if (status.equalsIgnoreCase("off")) {

                                    Log.e("MQTT CONN 2 - ", " off ");

                                    for (int j = 0; j < onList.size(); j++) {
                                        if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                            onList.get(j).setStatus("Off");

                                        }
                                    }

                                }



                            }


                        } else {

                            if (status.equalsIgnoreCase("allOn")) {

                                Log.e("MQTT CONN", "************************************* AllOn");

                                isRequest = true;
                                for (int j = 0; j < onList.size(); j++) {
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                        onList.get(j).setStatus("On");

                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                        onList.get(j).setStatus("0");
                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                        onList.get(j).setStatus("0");
                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                        onList.get(j).setStatus("0");
                                    }
                                }
                                deviceListAdapter.notifyDataSetChanged();

                            } else if (status.equalsIgnoreCase("allOff")) {

                                Log.e("MQTT CONN", "************************************* AllOff");


                                isRequest = true;
                                for (int j = 0; j < onList.size(); j++) {
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                        onList.get(j).setStatus("Off");

                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 678) {
                                        onList.get(j).setStatus("0");
                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 12) {
                                        onList.get(j).setStatus("0");
                                    }
                                    if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 13) {
                                        onList.get(j).setStatus("0");
                                    }
                                }
                                deviceListAdapter.notifyDataSetChanged();

                            } else if (status.startsWith("o")) {

                                switch (status) {
                                    case "on":

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }

                                        break;

                                    case "off":

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac)) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }

                                        break;

                                    case "on1":

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on2":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on3":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;
                                    case "on5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;

                                    case "on6":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 6) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;

                                    case "on7":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 7) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;

                                    case "on8":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 8) {
                                                onList.get(j).setStatus("On");

                                            }
                                        }
                                        break;

                                    case "off1":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 1) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off2":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 2) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off3":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 3) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off4":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;
                                    case "off5":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 5) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;


                                    case "off6":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 6) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;

                                    case "off7":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 7) {
                                                onList.get(j).setStatus("Off");

                                            }
                                        }
                                        break;

                                    case "off8":


                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 8) {
                                                onList.get(j).setStatus("Off");
                                            }
                                        }
                                        break;


                                    default:

                                        for (int j = 0; j < onList.size(); j++) {
                                            if (onList.get(j).getMac().equalsIgnoreCase(mac) && onList.get(j).getPosition() == 4) {
                                                onList.get(j).setStatus(status);

                                            }
                                        }

                                }


                            }

                        }

                        deviceListAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Exception", "json" + e.getMessage());

                    }


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });


        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }

    }


    public static void publishMessage(String topic, String msg) {

        try {

            MqttMessage message = new MqttMessage();

            message.setPayload(msg.getBytes());

            if (mqttAndroidClient.isConnected()) {
                mqttAndroidClient.publish(topic, message);
                Log.e("Msg published", ".." + message.toString());
            }

        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }

    }


    public static void publishSwitch(String onOperation, String s) {

        try {

            Log.e("operation " + onOperation, " topic " + s);
            MqttMessage message = new MqttMessage();

            message.setPayload(onOperation.getBytes());

            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.publish(s, message);
                Log.e("Msg published", ".." + message.toString());
            }

        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static void subscribeToTopic(MqttAndroidClient mqttAndroidClient, final String subscriptionTopic) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("subscribed to topic ", " .. " + subscriptionTopic);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    try {
                        System.err.println("Error subscribing: " + exception.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (MqttException ex) {
            System.err.println("Exception while subscribing");
            ex.printStackTrace();
        }
    }

    public static void publishMessage(MqttAndroidClient mqttAndroidClient, String msg, String publishTopic) {

        try {

            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Log.e("Mqttt con Class", "Msg published" + message.toString());

        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
