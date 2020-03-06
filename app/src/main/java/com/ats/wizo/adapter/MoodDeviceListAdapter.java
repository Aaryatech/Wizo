package com.ats.wizo.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.activity.MoodDeviceListActivity;
import com.ats.wizo.activity.ProfileActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.interfaces.MyInterface;
import com.ats.wizo.model.CurrentStatus;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.ErrorMessage;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static com.ats.wizo.mqtt.MqttConnection.publishMessage;

public class MoodDeviceListAdapter extends ArrayAdapter<Device> {

    private List<Device> list;

    public Activity activity;
    public static List<CurrentStatus> onList = new ArrayList<>();
    public static String ip;
    public static int op = 0;
    public Context context;
    public int changeFlag;

    private Boolean isTouched = false;


    // View lookup cache

    public static class ViewHolder {
        TextView tvCaption;
        ImageView ivBulb;
        Switch switch1;
        LinearLayout llCaption;
        SeekBar sbValue, sbDimmer1, sbDimmer2;
    }

   /* public MoodDeviceListAdapter(Context context, List<Device> data, Activity activity) {
        super(context, R.layout.device_list_adapter, data);
        this.activity = activity;
        this.context = context;
        this.list = data;
    }*/

    public MoodDeviceListAdapter(Context context, List<Device> data, Activity activity, int changeFlag) {
        super(context, R.layout.device_list_adapter, data);
        this.activity = activity;
        this.context = context;
        this.list = data;
        this.changeFlag = changeFlag;
    }


    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView = inflater.inflate(R.layout.device_list_adapter, parent, false);

            viewHolder.tvCaption = convertView.findViewById(R.id.tvCaption);
            viewHolder.switch1 = convertView.findViewById(R.id.switch1);
            viewHolder.ivBulb = convertView.findViewById(R.id.ivBulb);
            viewHolder.llCaption = convertView.findViewById(R.id.llCaption);
            viewHolder.sbValue = convertView.findViewById(R.id.sbValue);
            viewHolder.sbDimmer1 = convertView.findViewById(R.id.sbDimmer1);
            viewHolder.sbDimmer2 = convertView.findViewById(R.id.sbDimmer2);

            if (changeFlag == 1) {

                viewHolder.sbValue.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

                viewHolder.sbDimmer1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

                viewHolder.sbDimmer2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });

            } else if (changeFlag == 0) {

                viewHolder.sbValue.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });

                viewHolder.sbDimmer1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });

                viewHolder.sbDimmer2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });
            }

            if (list.get(i).getDevType() == 678) {
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.VISIBLE);
                viewHolder.sbDimmer1.setVisibility(View.GONE);
                viewHolder.sbDimmer2.setVisibility(View.GONE);

                viewHolder.sbValue.setProgress(list.get(i).getOperation());

            }

            if (list.get(i).getDevType() == 12) {
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.GONE);
                viewHolder.sbDimmer1.setVisibility(View.VISIBLE);
                viewHolder.sbDimmer2.setVisibility(View.GONE);

                viewHolder.sbDimmer1.setProgress(list.get(i).getOperation());

            }

            if (list.get(i).getDevType() == 13) {
                viewHolder.switch1.setVisibility(View.GONE);
                viewHolder.sbValue.setVisibility(View.GONE);
                viewHolder.sbDimmer1.setVisibility(View.GONE);
                viewHolder.sbDimmer2.setVisibility(View.VISIBLE);

                viewHolder.sbDimmer2.setProgress(list.get(i).getOperation());

            }

            viewHolder.switch1.setVisibility(View.INVISIBLE);


            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {


                    showAlert(list.get(i));

                    return false;
                }
            });


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.tvCaption.setText(list.get(i).getDevCaption());

        Log.e("ON LIST STATUS ", "----------------------------------------" + onList);


        viewHolder.sbDimmer1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                Toast.makeText(getContext(), "Value : " + progress, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                try {
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.updateMoodOperation(list.get(i).getDetailId(), progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateMoodOperationToServer(list.get(i).getDetailId(), progress);
                //publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer1Operation + "#" + progress);


            }
        });

        viewHolder.sbDimmer2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                Toast.makeText(getContext(), "Value : " + progress, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                try {
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.updateMoodOperation(list.get(i).getDetailId(), progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateMoodOperationToServer(list.get(i).getDetailId(), progress);
                // publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#" + progress);

            }
        });

        viewHolder.sbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                Toast.makeText(getContext(), "Speed : " + progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                try {
                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.updateMoodOperation(list.get(i).getDetailId(), progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateMoodOperationToServer(list.get(i).getDetailId(), progress);
                // publishMessage(list.get(i).getDevMac() + Constants.publishTopic, Constants.dimmer2Operation + "#" + progress);

            }
        });


        return convertView;
    }


    private void showAlert(final Device device) {


        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to remove this device ?");

        String positiveText = context.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        deleteDeviceFromMood(device);
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

    private void deleteDeviceFromMood(final Device device) {

        final ProgressDialog progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading ");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyLocalMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(MoodDeviceListActivity.moodId));
        RequestBody bodyLocalDevMac = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(device.getDevMac()));
        RequestBody bodyLocalDevType = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(device.getDevType()));

        Call<JsonObject> call = Constants.myInterface.deleteDeviceFromMood(bodyUserId, bodyLocalMoodId, bodyLocalDevMac, bodyLocalDevType);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    Log.e("Delete Mood ", " respo " + response.body().toString());

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {

                        DBHandler dbHandler = new DBHandler(context);
                        dbHandler.deleteRow(MoodDeviceListActivity.moodId, device.getDevMac(), device.getDevType());

                        Toast.makeText(activity, "Device Deleted Successfully", Toast.LENGTH_SHORT).show();
                        list.remove(device);
                        notifyDataSetChanged();

                    } else {

                        Toast.makeText(activity, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

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

                Toast.makeText(activity, "Something went wrong,Please try again", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void updateMoodOperationToServer(int moodDetailId, int operation) {

        final ProgressDialog progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading ");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyMoodDetailId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodDetailId));
        RequestBody bodyOperation = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(operation));

        Call<ErrorMessage> call = Constants.myInterface.updateMoodOperation(bodyMoodDetailId, bodyOperation);

        call.enqueue(new Callback<ErrorMessage>() {
            @Override
            public void onResponse(Call<ErrorMessage> call, Response<ErrorMessage> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Log.e("Mood DevList Adpt ", "---------------------" + response.body());


            }

            @Override
            public void onFailure(Call<ErrorMessage> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Log.e("Mood DevList Adpt ", "----------- ON FAILURE -------------" + t.getMessage());
                t.printStackTrace();
            }
        });


    }


}


