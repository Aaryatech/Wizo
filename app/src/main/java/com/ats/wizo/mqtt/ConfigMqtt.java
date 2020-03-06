package com.ats.wizo.mqtt;

import android.content.Context;
import android.util.Log;

import com.ats.wizo.activity.DeviceConfigActivity;
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

import static com.ats.wizo.common.Variables.isStatusReceived;
import static com.ats.wizo.common.Variables.subscribedTopics;
import static com.ats.wizo.constant.Constants.clientID;
import static com.ats.wizo.constant.Constants.mqttAndroidClient;
import static com.ats.wizo.constant.Constants.serverUri;

public class ConfigMqtt {


    public static String ip;
    public static String mac;
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

            mqttAndroidClient = ConfigMqtt.CreateInstance(mqttAndroidClient, clientID + ts, serverUri, context);

            try {
                boolean isConnected = mqttAndroidClient.isConnected();
                Log.e("Is Connected ", ".. " + isConnected);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception ", ".. " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MQTT Constant", "excep " + e.getMessage());
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

                            if (!Variables.subscribedTopics.contains(topicList.get(i))) { // to avoid duplicate subscription
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
                public void messageArrived(String topic, MqttMessage message) throws Exception { // all incoming messages from subscribed topics

                    Log.e("New Config Data ", "ConfigMqtt Class  " + message.toString());

                    try {

                        JSONObject object = null;
                        try {
                            object = new JSONObject(message.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        mac = object.getString("mac");
                        ip = object.getString("ip");

                        isStatusReceived = true;


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
