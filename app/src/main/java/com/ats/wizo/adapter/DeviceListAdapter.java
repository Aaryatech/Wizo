package com.ats.wizo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.DeviceDetailsActivity;
import com.ats.wizo.activity.DeviceListActivity;
import com.ats.wizo.activity.DimmerSchedulerActivity;
import com.ats.wizo.activity.FanSchedulerActivity;
import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.CurrentStatus;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.IpMacList;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

import static android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static com.ats.wizo.mqtt.MqttConnection.publishMessage;

/**
 * Created by maxadmin on 9/1/18.
 */

public class DeviceListAdapter extends ArrayAdapter<Device> {

    private List<Device> list;

    public Activity activity;
    public static List<CurrentStatus> onList = new ArrayList<>();
    public static String ip;
    public static int op = 0;
    public Context context;

    private Boolean isTouched = false;


    // View lookup cache

    public static class ViewHolder {
        TextView tvCaption;
        ImageView ivBulb;
        Switch switch1;
        LinearLayout llCaption;
        RadioGroup rgFan;
        RadioButton rb0, rb25, rb50, rb75, rb100;
        SeekBar sbValue, sbDimmer1, sbDimmer2;

    }

    public DeviceListAdapter(Context context, List<Device> data, Activity activity) {
        super(context, R.layout.device_list_adapter, data);
        this.activity = activity;
        this.context = context;
        this.list = data;
    }


    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        final DeviceListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new DeviceListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.device_list_adapter, parent, false);

            viewHolder.tvCaption = convertView.findViewById(R.id.tvCaption);
            viewHolder.switch1 = convertView.findViewById(R.id.switch1);
            viewHolder.ivBulb = convertView.findViewById(R.id.ivBulb);
            viewHolder.llCaption = convertView.findViewById(R.id.llCaption);
            viewHolder.rgFan = convertView.findViewById(R.id.rgFan);

            viewHolder.rb0 = convertView.findViewById(R.id.rb0);
            viewHolder.rb25 = convertView.findViewById(R.id.rb25);
            viewHolder.rb50 = convertView.findViewById(R.id.rb50);
            viewHolder.rb75 = convertView.findViewById(R.id.rb75);
            viewHolder.rb100 = convertView.findViewById(R.id.rb100);
            viewHolder.sbValue = convertView.findViewById(R.id.sbValue);
            viewHolder.sbDimmer1 = convertView.findViewById(R.id.sbDimmer1);
            viewHolder.sbDimmer2 = convertView.findViewById(R.id.sbDimmer2);

            if (list.get(i).getDevType() == 678) {
                viewHolder.rgFan.setVisibility(View.GONE);
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.VISIBLE);
                viewHolder.sbDimmer1.setVisibility(View.GONE);
                viewHolder.sbDimmer2.setVisibility(View.GONE);
            }

            if (list.get(i).getDevType() == 12) {
                viewHolder.rgFan.setVisibility(View.GONE);
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.GONE);
                viewHolder.sbDimmer1.setVisibility(View.VISIBLE);
                viewHolder.sbDimmer2.setVisibility(View.GONE);
            }

            if (list.get(i).getDevType() == 13) {
                viewHolder.rgFan.setVisibility(View.GONE);
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.GONE);
                viewHolder.sbDimmer1.setVisibility(View.GONE);
                viewHolder.sbDimmer2.setVisibility(View.VISIBLE);
            }


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceListAdapter.ViewHolder) convertView.getTag();
        }

        Log.e("DEVICE LIST ADPT ", "****************************************************** MODEL : " + list.get(i));


        checkForAllOff();
        checkForAllOn();

        viewHolder.tvCaption.setText(list.get(i).getDevCaption());

        Log.e("Status for " + list.get(i).getDevCaption(), " is " + onList.get(i).getStatus());


        if (list.get(i).getDevType() == 678) {

            Log.e("Status ", "------------ " + onList.get(i).getStatus());
            /*if (onList.get(i).getStatus().equalsIgnoreCase("Off")) {
                viewHolder.sbValue.setProgress(0);

            } else {

                if (onList.get(i).getStatus().equalsIgnoreCase("On")) {
                    viewHolder.sbValue.setProgress(1);

                } else {
                    viewHolder.sbValue.setProgress(Integer.parseInt(onList.get(i).getStatus()));

                }


            }*/

            try {
                viewHolder.sbValue.setProgress(Integer.parseInt(onList.get(i).getStatus()));
            } catch (Exception e) {
                viewHolder.sbValue.setProgress(0);
                Log.e("DEV LIST ADPT : ", "-----------------EXCEPTION------------------------ " + e.getMessage());
                e.printStackTrace();
            }

          /*  if (onList.get(i).getStatus().equalsIgnoreCase("0")) {
                viewHolder.sbValue.setProgress(0);

            } else if (onList.get(i).getStatus().equalsIgnoreCase("1")) {
                viewHolder.sbValue.setProgress(1);

            } else if (onList.get(i).getStatus().equalsIgnoreCase("2")) {
                viewHolder.sbValue.setProgress(2);

            } else if (onList.get(i).getStatus().equalsIgnoreCase("3")) {
                viewHolder.sbValue.setProgress(3);

            } else if (onList.get(i).getStatus().equalsIgnoreCase("4")) {
                viewHolder.sbValue.setProgress(4);

            }*/


        } else if (list.get(i).getDevType() == 12) {

            Log.e("DEV LIST ADAPTER", "---------------------------------- 12 " + onList.get(i).getStatus() + "   POSITION : " + onList.get(i).getPosition());

            Log.e("Status ", "------------ " + onList.get(i).getStatus());

            try {
                viewHolder.sbDimmer1.setProgress(Integer.parseInt(onList.get(i).getStatus()));
            } catch (Exception e) {
                viewHolder.sbDimmer1.setProgress(0);
                Log.e("DEV LIST ADPT : ", "-----------------EXCEPTION------------------------ " + e.getMessage());
                e.printStackTrace();
            }

            /*if (onList.get(i).getStatus().equalsIgnoreCase("Off")) {
                viewHolder.sbDimmer1.setProgress(0);

            } else {

                if (onList.get(i).getStatus().equalsIgnoreCase("On")) {
                    viewHolder.sbDimmer1.setProgress(1);

                } else {
                    viewHolder.sbDimmer1.setProgress(Integer.parseInt(onList.get(i).getStatus()));

                }
            }*/

           /* if (onList.get(i).getStatus().equalsIgnoreCase("0")) {
                viewHolder.sbDimmer1.setProgress(0);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("1")) {
                viewHolder.sbDimmer1.setProgress(1);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("2")) {
                viewHolder.sbDimmer1.setProgress(2);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("3")) {
                viewHolder.sbDimmer1.setProgress(3);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("4")) {
                viewHolder.sbDimmer1.setProgress(4);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("5")) {
                viewHolder.sbDimmer1.setProgress(5);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("6")) {
                viewHolder.sbDimmer1.setProgress(6);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("7")) {
                viewHolder.sbDimmer1.setProgress(7);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("8")) {
                viewHolder.sbDimmer1.setProgress(8);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("9")) {
                viewHolder.sbDimmer1.setProgress(9);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("10")) {
                viewHolder.sbDimmer1.setProgress(10);
            }*/


        } else if (list.get(i).getDevType() == 13) {

            Log.e("DEV LIST ADAPTER", "---------------------------------- 13 " + onList.get(i).getStatus() + "   POSITION : " + onList.get(i).getPosition());

            Log.e("Status ", "------------ " + onList.get(i).getStatus());

            try {
                viewHolder.sbDimmer2.setProgress(Integer.parseInt(onList.get(i).getStatus()));
            } catch (Exception e) {
                viewHolder.sbDimmer2.setProgress(0);
                Log.e("DEV LIST ADPT : ", "-----------------EXCEPTION------------------------ " + e.getMessage());
                e.printStackTrace();
            }

            /*if (onList.get(i).getStatus().equalsIgnoreCase("Off")) {
                viewHolder.sbDimmer2.setProgress(0);

            } else {

                if (onList.get(i).getStatus().equalsIgnoreCase("On")) {
                    viewHolder.sbDimmer2.setProgress(1);

                } else {
                    viewHolder.sbDimmer2.setProgress(Integer.parseInt(onList.get(i).getStatus()));

                }
            }*/


          /*  if (onList.get(i).getStatus().equalsIgnoreCase("0")) {
                viewHolder.sbDimmer2.setProgress(0);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("1")) {
                viewHolder.sbDimmer2.setProgress(1);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("2")) {
                viewHolder.sbDimmer2.setProgress(2);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("3")) {
                viewHolder.sbDimmer2.setProgress(3);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("4")) {
                viewHolder.sbDimmer2.setProgress(4);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("5")) {
                viewHolder.sbDimmer2.setProgress(5);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("6")) {
                viewHolder.sbDimmer2.setProgress(6);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("7")) {
                viewHolder.sbDimmer2.setProgress(7);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("8")) {
                viewHolder.sbDimmer2.setProgress(8);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("9")) {
                viewHolder.sbDimmer2.setProgress(9);
            } else if (onList.get(i).getStatus().equalsIgnoreCase("10")) {
                viewHolder.sbDimmer2.setProgress(10);
            }
*/

        } else {

            Log.e("DEV LIST ADAPTER", "---------------------------------- SWITCH -  " + onList.get(i).getStatus());

            Log.e("Status ", "------------ " + onList.get(i).getStatus());


            if (onList.get(i).getStatus().equalsIgnoreCase("on")) {
                viewHolder.switch1.setChecked(true);
                isTouched = false;

            } else {

                viewHolder.switch1.setChecked(false);
                isTouched = false;
            }
        }


        viewHolder.sbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "Speed : " + progress, Toast.LENGTH_SHORT).show();


                publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.intensityOperation + "#" + progress);

            }
        });

        viewHolder.sbDimmer1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "Value : " + progress, Toast.LENGTH_SHORT).show();


                publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#" + progress);

            }
        });

        viewHolder.sbDimmer2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "Value : " + progress, Toast.LENGTH_SHORT).show();


                publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#" + progress);

            }
        });


        viewHolder.rb0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Variables.isAtHome && !Variables.isInternetAvailable) {

                    for (int j = 6; j <= 8; j++) {
                        localFanOFF(list.get(i), j);
                    }

                } else {
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#6");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#7");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#8");

                }
            }
        });

        viewHolder.rb25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.isAtHome && !Variables.isInternetAvailable) {


                    for (int j = 6; j <= 8; j++) {
                        localFanON25(list.get(i), j);
                    }
                } else {
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#6");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#7");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#8");


                }
            }
        });

        viewHolder.rb50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Variables.isAtHome && !Variables.isInternetAvailable) {


                    for (int j = 6; j <= 8; j++) {
                        localFanON50(list.get(i), j);
                    }
                } else {
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#6");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#7");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#8");


                }

            }
        });

        viewHolder.rb75.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Variables.isAtHome && !Variables.isInternetAvailable) {


                    for (int j = 6; j <= 8; j++) {
                        localFanON75(list.get(i), j);
                    }
                } else {
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#6");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#7");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#8");


                }

            }
        });

        viewHolder.rb100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Variables.isAtHome && !Variables.isInternetAvailable) {


                    for (int j = 6; j <= 8; j++) {
                        localFanON100(list.get(i), j);
                    }
                } else {
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#6");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#7");
                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#8");


                }

            }
        });

        viewHolder.switch1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });

        viewHolder.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (isTouched) {
                    isTouched = false;

                    if (b) {  // turn ON operation


                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local ON operation

                            onList.get(i).setStatus("On");

                            if (list.get(i).getDevType() == 0) {


                                deviceON(list.get(i).getDevIp());

                            } else {

                                switchON(list.get(i));

                            }
                            checkForAllOn();


                        } else {  // mqtt ON operation
                            onList.get(i).setStatus("On");

                            if (Variables.isInternetAvailable && Variables.isMQTTConnected) {

                                Log.e("Over Internet###", "..");

                                if (list.get(i).getDevType() == 0) {

                                    publishMessage(Constants.onOperation, list.get(i).getDevMac() + Constants.publishTopic);

                                } else {
                                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.onOperation + "#" + list.get(i).getDevType());
                                }
                                checkForAllOn();


                            } else {   // home wifi and internet not available for ON operation
                                Toast.makeText(context, "Please connect to \"Internet\" OR \"" + list.get(i).getDevSsid() + "\"" + " network", Toast.LENGTH_SHORT).show();
                                viewHolder.switch1.setChecked(false);
                            }

                        }


                    } else {  // turn OFF operation
                        if (Variables.isAtHome && !Variables.isInternetAvailable) {   // local OFF operation
                            onList.get(i).setStatus("Off");

                            if (list.get(i).getDevType() == 0) {
                                deviceOFF(list.get(i).getDevIp());

                            } else {

                                switchOFF(list.get(i));

                            }
                            checkForAllOff();


                        } else {   // mqtt OFF operation
                            onList.get(i).setStatus("Off");

                            if (Variables.isMQTTConnected && Variables.isInternetAvailable) {
                                Log.e("Over Internet###", "..");

                                if (list.get(i).getDevType() == 0) {
                                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation);

                                } else {

                                    publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.offOperation + "#" + list.get(i).getDevType());

                                }
                                checkForAllOff();


                            } else {   // home network and internet not available for OFF operation
                                Toast.makeText(context, "Please connect to \"Internet\" OR  \"" + Constants.homeSSID + "\"" + " network", Toast.LENGTH_SHORT).show();
                                viewHolder.switch1.setChecked(true);
                            }

                        }

                    }

                }
                isTouched = false;
            }


        });


        viewHolder.llCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // DeviceListAdapter.deviceModel = list.get(i);

                Intent intent;
                if (list.get(i).getDevType() == 678) {
                    FanSchedulerActivity.device = list.get(i);

                    intent = new Intent(context, FanSchedulerActivity.class);
                } else if (list.get(i).getDevType() == 12) {
                    DimmerSchedulerActivity.device = list.get(i);

                    intent = new Intent(context, DimmerSchedulerActivity.class);
                    intent.putExtra("devType", 12);
                } else if (list.get(i).getDevType() == 13) {
                    DimmerSchedulerActivity.device = list.get(i);

                    intent = new Intent(context, DimmerSchedulerActivity.class);
                    intent.putExtra("devType", 13);
                } else {
                    DeviceDetailsActivity.device = list.get(i);

                    intent = new Intent(context, DeviceDetailsActivity.class);

                }
                Pair<View, String> p1 = Pair.create((View) viewHolder.tvCaption, "caption");
                Pair<View, String> p2 = Pair.create((View) viewHolder.ivBulb, "image");

                ActivityOptionsCompat options = makeSceneTransitionAnimation(activity, p1, p2);
                //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent, options.toBundle());
                //  }
                //activity.finish();
                // activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });


        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {


               /* if (Variables.isAtHome) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Select Operation");
                    builder.setMessage("Please select one option for " + list.get(i).getDevCaption());

                    String positiveText = context.getString(R.string.Reboot);

                    builder.setPositiveButton(positiveText,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    rebootDeviceDialog(list.get(i).getDevIp(), list.get(i).getDevCaption());
                                    dialog.dismiss();

                                }
                            });

                    String negativeText = context.getString(R.string.Reset);
                    builder.setNegativeButton(negativeText,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    resetDialog(list.get(i).getDevIp(), list.get(i).getDevCaption());
                                    dialog.dismiss();
                                }
                            });


                    AlertDialog dialog = builder.create();

                    dialog.show();
                }*/

                return false;
            }
        });

        return convertView;
    }

    private void checkForAllOff() {

        boolean isAllOff = true;

        for (CurrentStatus status : onList) {

            if (status.getStatus().equalsIgnoreCase("On")) {

                isAllOff = false;
            }

        }
        if (isAllOff) {
            try {

                ((DeviceListActivity) activity).setRoomAllOff();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void checkForAllOn() {

        boolean isAllOn = true;


        for (CurrentStatus status : onList) {

            if (status.getStatus().equalsIgnoreCase("Off")) {

                isAllOn = false;
            }

        }
        if (isAllOn) {

            try {

                ((DeviceListActivity) activity).setRoomAllOn();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void localFanON100(Device device, int index) {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        if (index == 6) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);
        } else if (index == 7) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        } else if (index == 8) {
            call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);

        }
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });

    }

    private void localFanON75(Device device, int index) {


        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        if (index == 6) {
            call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);
        } else if (index == 7) {
            call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);

        } else if (index == 8) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        }
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });

    }

    private void localFanON50(Device device, int index) {
        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        if (index == 6) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);
        } else if (index == 7) {
            call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);

        } else if (index == 8) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        }
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });
    }

    private void localFanON25(Device device, int index) {


        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        if (index == 6) {
            call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);
        } else if (index == 7) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        } else if (index == 8) {
            call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        }
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });

    }

    private void localFanOFF(Device device, int index) {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "off", Constants.authKey);

        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });


    }

    private void localFanOn(Device device, int index) {

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + device.getDevIp())
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);

        Observable<JsonObject> call = myInterface.fanOperation(String.valueOf(index), "on", Constants.authKey);

        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                        // deviceAdapter.notifyDataSetChanged();

                        Log.e("Task completed", " onList " + onList.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("New Json", "Error" + e.getMessage());

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {


                    }
                });


    }


    private void resetDialog(final String ip, String caption) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reset Device ?");
        builder.setMessage("Do you really want to reset " + caption + " ?");

        String positiveText = context.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetDevice(ip);

                    }
                });

        String negativeText = context.getString(android.R.string.cancel);
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

    private void resetDevice(final String ip) {

        DBHandler dbHandler = new DBHandler(context);
        //  dbOperation.UpdateDeviceStatus("0", String.valueOf(ip));


        String urlString = "http://" + ip;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.resetDevice();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.e("Device reset", " .." + response.body().toString());


                //dbOperation.UpdateDeviceStatus("0", String.valueOf(ip));

                //  int i = list.indexOf(ip);
                // list.remove(i);
                Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                activity.finish();

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device reset", " failed" + t.getMessage());
            }
        });

    }


    private void rebootDeviceDialog(final String ip, String caption) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reboot Device ?");
        builder.setMessage("Do you really want to reboot " + caption + " ?");

        String positiveText = context.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rebootDevice(ip);

                    }
                });

        String negativeText = context.getString(android.R.string.cancel);
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


    private void rebootDevice(String ip) {

        String urlString = "http://" + ip;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.softReset();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.e("Device Reboot", " .." + response.body().toString());

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device Reboot", " failed" + t.getMessage());
            }
        });

    }

    private void deviceOFF(String ip) {

        String urlString = "http://" + ip;
        Log.e("URL ", ".. " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.deviceOFF();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {
                    Log.e("Device OFF", " .." + response.body().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device OFF", " failed" + t.getMessage());
            }
        });

    }

    private void switchOFF(Device device) {

        String urlString = "http://" + device.getDevIp();
        Log.e("URL ", ".. " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process(String.valueOf(device.getDevType()), "off", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {
                    Log.e("Device OFF", " .." + response.body().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device OFF", " failed" + t.getMessage());
                Toast.makeText(context, "Try Sync With Router Option", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void deviceON(String ip) {

        String urlString = "http://" + ip;
        Log.e("URL ", ".. " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.deviceON();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    Log.e("Device ON", " .." + response.body().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device ON", " failed" + t.getMessage());
            }
        });

    }

    private void switchON(Device device) {

        String urlString = "http://" + device.getDevIp();
        Log.e("device no " + device.getDevType(), "url " + urlString);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlString)
                .addConverterFactory(GsonConverterFactory.create()).build();

        MyInterface myInterface = retrofit.create(MyInterface.class);
        Call<JsonObject> call = myInterface.process(String.valueOf(device.getDevType()), "on", Constants.authKey);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    Log.e("Device ON", " .." + response.body().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Device ON", " failed" + t.getMessage());
                Toast.makeText(context, "Try Sync With Router Option", Toast.LENGTH_SHORT).show();

            }
        });

    }


}


