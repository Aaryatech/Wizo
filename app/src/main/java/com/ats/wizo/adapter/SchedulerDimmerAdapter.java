package com.ats.wizo.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.DimmerSchedulerActivity;
import com.ats.wizo.activity.FanSchedulerActivity;
import com.ats.wizo.activity.TimePickerActivity;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.RespScheduler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SchedulerDimmerAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<RespScheduler> list;

    Context context;
    Activity activity;
    Boolean isTouched = false;
    ProgressDialog progressDialog;

    public SchedulerDimmerAdapter(Context context, List<RespScheduler> list, Activity activity) {
        this.inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity = activity;
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class Holder {
        TextView tvTime, tvDay, tvSpeed;
        LinearLayout linearLayout;
        Switch btnSwitch;
    }


    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.scheduler_fan_list, null);
        holder.tvTime = rowView.findViewById(R.id.tvTime);
        holder.tvDay = rowView.findViewById(R.id.tvDay);
        holder.tvSpeed = rowView.findViewById(R.id.tvSpeed);
        holder.linearLayout = rowView.findViewById(R.id.linearLayout);
        holder.btnSwitch = rowView.findViewById(R.id.btnSwitch);

        final RespScheduler schPojo = list.get(i);
        Log.e("BEAN ", "----------------- " + schPojo);

        String time = schPojo.getTime();
        Log.e("Time", " is " + time);

        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            Date _24HourDt = _24HourSDF.parse(time);
            String _12HrsTime = _12HourSDF.format(_24HourDt);
            holder.tvTime.setText(_12HrsTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.tvSpeed.setText("V-" + schPojo.getOperation());


       /* if (schPojo.getDay() == 0) {
            holder.tvDay.setVisibility(View.INVISIBLE);
        }*/


        int days = schPojo.getDay();
        ArrayList<Integer> daysArray = new ArrayList<>();
        try {

            while (days > 0) {
                daysArray.add(days % 10);
                days = days / 10;
            }

            String daysStr = "";
            if (daysArray.size() > 0) {
                if (daysArray.contains(1)) {
                    daysStr = daysStr + "Sun";
                }
                if (daysArray.contains(2)) {
                    daysStr = daysStr + ", " + "Mon";
                }
                if (daysArray.contains(3)) {
                    daysStr = daysStr + ", " + "Tue";
                }
                if (daysArray.contains(4)) {
                    daysStr = daysStr + ", " + "Wed";
                }
                if (daysArray.contains(5)) {
                    daysStr = daysStr + ", " + "Thu";
                }
                if (daysArray.contains(6)) {
                    daysStr = daysStr + ", " + "Fri";
                }
                if (daysArray.contains(7)) {
                    daysStr = daysStr + ", " + "Sat";
                }

                if (daysStr.substring(0, 1).equalsIgnoreCase(",")) {
                    daysStr = daysStr.substring(2);
                }

                Log.e("Days Print", "------------------------------------------- " + daysStr);
                holder.tvDay.setText("" + daysStr);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.btnSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                isTouched = true;

                return false;
            }
        });

        if (schPojo.getSchStatus() == 1) {
            holder.btnSwitch.setChecked(true);
        } else {
            holder.btnSwitch.setChecked(false);
        }

        holder.btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (isTouched) {

                    if (b) {

                        updateScheStatus(list.get(i), 1);

                    } else {
                        updateScheStatus(list.get(i), 0);

                    }

                }
                isTouched = false;

            }
        });


        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Log.e("DIMMER SCH ACT", "------------------------------------- long click");
                try {

                    //DimmerSchedulerActivity activity=(DimmerSchedulerActivity)context;

                    final Dialog openDialog = new Dialog(activity);
                    openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    openDialog.setContentView(R.layout.custom_scheduler_menu);
                    openDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    Window window = openDialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.CENTER;
                    wlp.dimAmount = 0.75f;
                    wlp.x = 100;
                    wlp.y = 100;
                    wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    window.setAttributes(wlp);

                    final Button btnUpdate = openDialog.findViewById(R.id.btnUpdate);
                    final Button btnDelete = openDialog.findViewById(R.id.btnDelete);

                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            openDialog.dismiss();

                            String status;
                            if (schPojo.getSchStatus() == 1) {
                                status = "ON";
                            } else {
                                status = "OFF";
                            }

                            Gson gson = new Gson();
                            String json = gson.toJson(schPojo);

                            Intent intent = new Intent(context, TimePickerActivity.class);
                            intent.putExtra("op", status);
                            intent.putExtra("moodId", -1);
                            intent.putExtra("dimmerDevType", schPojo.getDevType());
                            intent.putExtra("model", json);
                            intent.putExtra("type", 100);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        }
                    });

                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            openDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
                            builder.setTitle("Delete Scheduler ?");

                            String positiveText = "Delete";

                            builder.setPositiveButton(positiveText,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            deleteScheduler(schPojo);
                                            dialog.dismiss();
                                        }
                                    });

                            String negativeText = "Cancel";
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
                    });


                    openDialog.show();

                } catch (Exception e) {
                    Log.e("DIMMER SCH ACT","--------------------EXCEPTION : "+e.getMessage());
                    e.printStackTrace();
                }


                return false;
            }
        });



      /*  View v;

        final RespScheduler schPojo = list.get(i);

        v = inflater.inflate(R.layout.scheduler_fan_list, viewGroup, false);


        tvTime = (TextView) v.findViewById(R.id.tvTime);
        TextView tvDay = (TextView) v.findViewById(R.id.tvDay);

        TextView tvSpeed = (TextView) v.findViewById(R.id.tvSpeed);

        String time =schPojo.getTime();
        Log.e("Time"," is "+time);

        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a");
            Date _24HourDt = _24HourSDF.parse(time);
            String _12HrsTime=_12HourSDF.format(_24HourDt);
            tvTime.setText(_12HrsTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvSpeed.setText(schPojo.getOperation());




        if (schPojo.getDay()==0) {
            tvDay.setVisibility(View.INVISIBLE);
        }




        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.MyAlertDialogStyle);
                builder.setTitle("Delete Scheduler ?");

                String positiveText = "Delete";

                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteScheduler(schPojo);
                                dialog.dismiss();
                            }
                        });

                String negativeText = "Cancel";
                builder.setNegativeButton(negativeText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();

                dialog.show();

                return false;
            }
        });

        v.setTag(i);*/

        return rowView;
    }

    private void updateScheStatus(RespScheduler schPojo, final int status) {
        progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        schPojo.setSchStatus(status);

        Call<JsonObject> call = Constants.myInterface.updateScheduler(schPojo);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {
                    Log.e("Json New Scheduler ", " .. " + response.body().toString());

                    JSONObject object = new JSONObject(response.body().toString());

                    Boolean error = object.getBoolean("error");

                    if (!error) {

                    } else {
                        Toast.makeText(activity, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Scheduler update ", " failed " + t.getMessage());


                Toast.makeText(activity, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteScheduler(RespScheduler schPojo) {

        final ProgressDialog progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        Call<JsonObject> call = Constants.myInterface.deleteScheduler(schPojo);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    JSONObject object = new JSONObject(response.body().toString());
                    Log.e("Json DELETE Scheduler ", " .. " + response.body().toString());

                    Boolean error = object.getBoolean("error");

                    if (!error) {
                        context.startActivity(new Intent(context, DimmerSchedulerActivity.class));
                        activity.finish();
                    } else {
                        Toast.makeText(activity, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception ", " .. " + e.getMessage());

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });


    }
}