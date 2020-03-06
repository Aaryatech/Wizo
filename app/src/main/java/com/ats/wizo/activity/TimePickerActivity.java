package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.ErrorMessage;
import com.ats.wizo.model.RespMoodScheduleData;
import com.ats.wizo.model.RespMoodScheduler;
import com.ats.wizo.model.RespScheduler;
import com.ats.wizo.model.RespSchedulerData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ats.wizo.fragment.OffScheduleFragment.offSchedulerAdapter;
import static com.ats.wizo.fragment.OnScheduleFragment.onSchedulerAdapter;

public class TimePickerActivity extends AppCompatActivity implements View.OnClickListener {

    private CheckBox cBox;
    private TimePicker tPicker;
    private TextView showTime, tvSKNote;
    private Button btnDone;

    private TextView tvSun, tvMon, tvTue, tvWed, tvThr, tvFri, tvSat;

    private String hour;
    private String minute;
    public static String setTimeText;
    private String op = "OFF";
    private String type = "0";

    private ProgressDialog progressDialog;
    SeekBar skIntensity, skDimmer;
    String value = "";

    RadioGroup rgFan;
    RadioButton rb0, rb25, rb50, rb75, rb100;

    int moodId, fanDevType = 0, dimmerDevType = 0, devType = 0;

    ArrayList<Integer> daysArray = new ArrayList<>();

    RespScheduler schModel;
    RespMoodScheduler moodSchModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        tPicker = (TimePicker) findViewById(R.id.time_picker);
        btnDone = (Button) findViewById(R.id.done);

        cBox = (CheckBox) findViewById(R.id.time_picker_checkbox);

        rgFan = findViewById(R.id.rgFan);

        rb0 = findViewById(R.id.rb0);
        rb25 = findViewById(R.id.rb25);
        rb50 = findViewById(R.id.rb50);
        rb75 = findViewById(R.id.rb75);
        rb100 = findViewById(R.id.rb100);

        tvSun = findViewById(R.id.tvSun);
        tvMon = findViewById(R.id.tvMon);
        tvTue = findViewById(R.id.tvTue);
        tvWed = findViewById(R.id.tvWed);
        tvThr = findViewById(R.id.tvThr);
        tvFri = findViewById(R.id.tvFri);
        tvSat = findViewById(R.id.tvSat);

        tvSun.setOnClickListener(this);
        tvMon.setOnClickListener(this);
        tvTue.setOnClickListener(this);
        tvWed.setOnClickListener(this);
        tvThr.setOnClickListener(this);
        tvFri.setOnClickListener(this);
        tvSat.setOnClickListener(this);


        skIntensity = (SeekBar) findViewById(R.id.skIntensity);
        skDimmer = (SeekBar) findViewById(R.id.skDimmer);
        tvSKNote = (TextView) findViewById(R.id.tvSKNote);

        skIntensity.setMax(Constants.fanSpeed);
        skDimmer.setMax(Constants.dimmer1Speed);
        op = getIntent().getStringExtra("op");

        Log.e("OP ", "********************* --------------------------- " + op);

        moodId = getIntent().getIntExtra("moodId", -1);
        devType = getIntent().getIntExtra("type", 0);
        fanDevType = getIntent().getIntExtra("devType", 0);
        dimmerDevType = getIntent().getIntExtra("dimmerDevType", 0);

        Log.e("TIME PICK ACT", "---------------------------- DIMMER DEV TYPE - " + dimmerDevType);

        String modelStr = getIntent().getStringExtra("model");
        Gson gson = new Gson();

        if (moodId == -1) {
            schModel = gson.fromJson(modelStr, RespScheduler.class);
        } else {
            moodSchModel = gson.fromJson(modelStr, RespMoodScheduler.class);
        }


        Log.e("SCHEDULE ", "-------------**********--------------- " + schModel);

        Log.e("MOOD SCHEDULE ", "-------------**********--------------- " + moodSchModel);

        if (schModel != null) {

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

            try {

                if (schModel.getDevType() == 12) {
                    skDimmer.setProgress(schModel.getOperation());
                } else if (schModel.getDevType() == 13) {
                    skDimmer.setProgress(schModel.getOperation());
                } else if (schModel.getDevType() == 678) {
                    skIntensity.setProgress(schModel.getOperation());
                }


                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(schModel.getTime()));
                int hr = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);

                Log.e("HOURS : " + hr, "-----------------MIN : " + min);

                tPicker.setHour(hr);
                tPicker.setMinute(min);


            } catch (Exception e) {
                Log.e("TIME PICKER ACT", "------------ DATE EXC " + e.getMessage());
                e.printStackTrace();
            }


            // tPicker

            int days = schModel.getDay();

            ArrayList<Integer> daysList = new ArrayList<>();

            try {

                while (days > 0) {
                    daysList.add(days % 10);
                    days = days / 10;
                }

                Log.e("DAYS LIST ", "-*********************************------------- " + daysList);

                if (daysList.contains(1)) {
                    Log.e("DAY", "********************************** 1");
                    if (daysArray.isEmpty()) {
                        daysArray.add(1);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(1)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 1) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(1);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(2)) {
                    Log.e("DAY", "********************************** 2");
                    if (daysArray.isEmpty()) {
                        daysArray.add(2);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(2)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 2) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(2);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(3)) {
                    Log.e("DAY", "********************************** 3");
                    if (daysArray.isEmpty()) {
                        daysArray.add(3);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(3)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 3) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(3);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(4)) {
                    Log.e("DAY", "********************************** 4");
                    if (daysArray.isEmpty()) {
                        daysArray.add(4);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(4)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 4) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(4);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(5)) {
                    Log.e("DAY", "********************************** 5");
                    if (daysArray.isEmpty()) {
                        daysArray.add(5);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(5)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 5) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(5);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(6)) {
                    Log.e("DAY", "********************************** 6");
                    if (daysArray.isEmpty()) {
                        daysArray.add(6);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(6)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 6) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(6);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(7)) {
                    Log.e("DAY", "********************************** 7");
                    if (daysArray.isEmpty()) {
                        daysArray.add(7);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(7)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 7) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(7);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        if (moodSchModel != null) {

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

            try {


                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(moodSchModel.getTime()));
                int hr = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);

                Log.e("HOURS : " + hr, "-----------------MIN : " + min);

                tPicker.setHour(hr);
                tPicker.setMinute(min);


            } catch (Exception e) {
                Log.e("TIME PICKER ACT", "------------ DATE EXC " + e.getMessage());
                e.printStackTrace();
            }


            // tPicker

            int days = moodSchModel.getDay();

            ArrayList<Integer> daysList = new ArrayList<>();

            try {

                while (days > 0) {
                    daysList.add(days % 10);
                    days = days / 10;
                }

                Log.e("DAYS LIST ", "-*********************************------------- " + daysList);

                if (daysList.contains(1)) {
                    Log.e("DAY", "********************************** 1");
                    if (daysArray.isEmpty()) {
                        daysArray.add(1);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(1)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 1) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(1);
                        tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSun.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(2)) {
                    Log.e("DAY", "********************************** 2");
                    if (daysArray.isEmpty()) {
                        daysArray.add(2);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(2)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 2) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(2);
                        tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvMon.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(3)) {
                    Log.e("DAY", "********************************** 3");
                    if (daysArray.isEmpty()) {
                        daysArray.add(3);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(3)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 3) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(3);
                        tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvTue.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }

                if (daysList.contains(4)) {
                    Log.e("DAY", "********************************** 4");
                    if (daysArray.isEmpty()) {
                        daysArray.add(4);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(4)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 4) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(4);
                        tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvWed.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(5)) {
                    Log.e("DAY", "********************************** 5");
                    if (daysArray.isEmpty()) {
                        daysArray.add(5);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(5)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 5) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(5);
                        tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvThr.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(6)) {
                    Log.e("DAY", "********************************** 6");
                    if (daysArray.isEmpty()) {
                        daysArray.add(6);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(6)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 6) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(6);
                        tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvFri.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


                if (daysList.contains(7)) {
                    Log.e("DAY", "********************************** 7");
                    if (daysArray.isEmpty()) {
                        daysArray.add(7);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appTheme));
                    } else if (daysArray.contains(7)) {
                        int pos = 0;
                        for (int i = 0; i < daysArray.size(); i++) {
                            if (daysArray.get(i) == 7) {
                                pos = i;
                            }
                        }
                        daysArray.remove(pos);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appGreen));
                    } else {
                        daysArray.add(7);
                        tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                        tvSat.setTextColor(getResources().getColor(R.color.appTheme));
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }



        skIntensity.setVisibility(View.INVISIBLE);
        skDimmer.setVisibility(View.INVISIBLE);

        skIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(TimePickerActivity.this, "Speed : " + progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        skDimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(TimePickerActivity.this, "Value : " + progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if (moodId == -1 && fanDevType == 678) {

            skIntensity.setVisibility(View.VISIBLE);

            rgFan.setVisibility(View.GONE);
        } else if (moodId == -1 && dimmerDevType == 12) {
            skIntensity.setVisibility(View.GONE);
            rgFan.setVisibility(View.GONE);

            skDimmer.setVisibility(View.VISIBLE);
        } else if (moodId == -1 && dimmerDevType == 13) {
            skIntensity.setVisibility(View.GONE);
            rgFan.setVisibility(View.GONE);

            skDimmer.setVisibility(View.VISIBLE);
        }


        cBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cBox.isChecked()) {
                    Log.e("CB checked ", "..");
                } else {
                    Log.e("CB unchecked ", "..");
                }

            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("USER ID : ", "------------" + Constants.userId);

                String days = "";
                if (daysArray.size() > 0) {
                    for (int i = 0; i < daysArray.size(); i++) {
                        days = days + daysArray.get(i);
                    }
                }

                Log.e("Days String ", " ------------ " + days);

                int daysInt = 0;
                try {
                    daysInt = Integer.parseInt(days);
                } catch (Exception e) {
                    daysInt = 0;
                }
                Log.e("Days Integer ", "------------ " + daysInt);

                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                Log.e("DAY OF WEEK ", "--------------- " + dayOfWeek);

                //--------------------------------------------------


                tPicker.clearFocus();
                hour = tPicker.getCurrentHour().toString();
                minute = tPicker.getCurrentMinute().toString();

                if (tPicker.getCurrentMinute().intValue() < 10) {

                    setTimeText = hour + ":" + "0" + minute + ":00";
                    Log.e("Set Time", " .. " + setTimeText);

                } else {
                    setTimeText = hour + ":" + minute + ":00";
                    Log.e("Set Time", " .. " + setTimeText);
                }
                if (type.equalsIgnoreCase("5") && value.equalsIgnoreCase("")) {

                    Toast.makeText(TimePickerActivity.this, "Please Select Value First", Toast.LENGTH_SHORT).show();
                } else {

                    // Log.e("DEV TYPE : ", "----------------- " + DeviceDetailsActivity.device.getDevType());

                    if (daysInt != 0) {

                        try {
                            if (moodId == -1 && devType < 10) {
                                Log.e("TIME PICK ACT", "--------****************----------- addNewScheduler");
                                addNewScheduler(daysInt);
                            } else if (moodId == -1 && fanDevType == 678) {
                                Log.e("TIME PICK ACT", "--------****************----------- addFanScheduler");
                                addFanScheduler(daysInt);
                            } else if (moodId == -1 && dimmerDevType == 12) {
                                Log.e("TIME PICK ACT", "--------****************-----12------ addDimmerScheduler");
                                addDimmerScheduler(12, daysInt);
                            } else if (moodId == -1 && dimmerDevType == 13) {
                                Log.e("TIME PICK ACT", "--------****************-----13------ addDimmerScheduler");
                                addDimmerScheduler(13, daysInt);
                            } else {
                                Log.e("TIME PICK ACT", "--------****************----------- addNewMoodScheduler");
                                addNewMoodScheduler(daysInt);
                            }
                        } catch (Exception e) {
                            Log.e("TimePickerAct : ", "-------- EXCEPTION : " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TimePickerActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle("Please select day for scheduler");

                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog dialog = builder.create();

                        dialog.show();
                    }


                }
            }
        });

    }

  /*  private void addFanScheduler() {


        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        //final String dev_id = DeviceDetailsActivity.device.getDevMac();
        final String dev_id = FanSchedulerActivity.device.getDevMac();
        String type = "678";

        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();


        respScheduler.setOperation(skIntensity.getProgress());


        if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }


        respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.fanSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            DeviceDetailsActivity.fanSchList.add(respScheduler);
                          *//*  if (respScheduler.getOperation() == 1) {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            } else if (respScheduler.getOperation() == 0) {
                                DeviceDetailsActivity.offSchList.add(respScheduler);

                            } else {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            }*//*
                        }

                        Intent intent = new Intent(TimePickerActivity.this, FanSchedulerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                        //onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });


    }

    private void addNewScheduler() {

        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        final String dev_id = DeviceDetailsActivity.device.getDevMac();
        String type = String.valueOf(DeviceDetailsActivity.device.getDevType());
//
//        if(type.equalsIgnoreCase("5")&& op.equalsIgnoreCase("ON")){
//            op=value;
//        }
        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();


        if (op.equalsIgnoreCase("ON")) {
            respScheduler.setOperation(1);
        } else {
            respScheduler.setOperation(0);
        }

        if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }


        respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.onSchList = new ArrayList<RespScheduler>();
                        DeviceDetailsActivity.offSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            if (respScheduler.getOperation() == 1) {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            } else if (respScheduler.getOperation() == 0) {
                                DeviceDetailsActivity.offSchList.add(respScheduler);

                            }
                        }


                        onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });
    }

    private void addNewMoodScheduler() {

        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RespMoodScheduler respMoodScheduler = new RespMoodScheduler();


        if (op.equalsIgnoreCase("ON")) {

            respMoodScheduler.setOperation(1);

        } else {
            respMoodScheduler.setOperation(0);
        }

        if (cBox.isChecked()) {
            respMoodScheduler.setDay(1);
        } else {
            respMoodScheduler.setDay(0);
        }


        respMoodScheduler.setSchStatus(1);
        respMoodScheduler.setMoodId(moodId);
        respMoodScheduler.setTime(setTimeText);
        respMoodScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "mood scheduler JSON " + respMoodScheduler.toString());

        Call<RespMoodScheduleData> call = Constants.myInterface.addNewMoodScheduler(respMoodScheduler);
        call.enqueue(new Callback<RespMoodScheduleData>() {
            @Override
            public void onResponse(Call<RespMoodScheduleData> call, Response<RespMoodScheduleData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespMoodScheduleData respSchedulerData = response.body();
                    Log.e("add new ", "mood scheduler Response\n\n " + response.body().toString());

                    if (!respSchedulerData.isError()) {

                        MoodSchedulerActivity.onMoodSchList = new ArrayList<>();
                        MoodSchedulerActivity.offMoodSchList = new ArrayList<>();

                        for (RespMoodScheduler respMoodScheduler : respSchedulerData.getSchedulerList()) {

                            if (respMoodScheduler.getOperation() == 1) {
                                MoodSchedulerActivity.onMoodSchList.add(respMoodScheduler);

                            } else if (respMoodScheduler.getOperation() == 0) {
                                MoodSchedulerActivity.offMoodSchList.add(respMoodScheduler);

                            }
                        }

                        onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<RespMoodScheduleData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());
            }
        });
    }

    private void addDimmerScheduler(final int dimDevType) {


        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        //final String dev_id = DeviceDetailsActivity.device.getDevMac();
        final String dev_id = DimmerSchedulerActivity.device.getDevMac();
        String type = ""+dimDevType;

        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();


        respScheduler.setOperation(skDimmer.getProgress());


        if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }


        respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.dimmerSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            DeviceDetailsActivity.dimmerSchList.add(respScheduler);

                        }

                        Intent intent = new Intent(TimePickerActivity.this, DimmerSchedulerActivity.class);
                        intent.putExtra("devType",dimDevType);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                        //onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });


    }
*/


    private void addFanScheduler(int days) {


        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        //final String dev_id = DeviceDetailsActivity.device.getDevMac();
        final String dev_id = FanSchedulerActivity.device.getDevMac();
        String type = "678";

        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();

        if (schModel != null) {
            respScheduler.setSchId(schModel.getSchId());
            respScheduler.setSchStatus(schModel.getSchStatus());
        } else {
            respScheduler.setSchId(0);
            respScheduler.setSchStatus(1);
        }

        respScheduler.setOperation(skIntensity.getProgress());


       /* if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }*/

        respScheduler.setDay(days);
        // respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.fanSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            DeviceDetailsActivity.fanSchList.add(respScheduler);
                          /*  if (respScheduler.getOperation() == 1) {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            } else if (respScheduler.getOperation() == 0) {
                                DeviceDetailsActivity.offSchList.add(respScheduler);

                            } else {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            }*/
                        }

                        if (schModel != null) {
                            onBackPressed();
                        } else {
                            Intent intent = new Intent(TimePickerActivity.this, FanSchedulerActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }


                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });


    }

    private void addNewScheduler(int days) {

        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        final String dev_id = DeviceDetailsActivity.device.getDevMac();
        String type = String.valueOf(DeviceDetailsActivity.device.getDevType());
//
//        if(type.equalsIgnoreCase("5")&& op.equalsIgnoreCase("ON")){
//            op=value;
//        }
        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();

        if (schModel != null) {
            respScheduler.setSchId(schModel.getSchId());
            respScheduler.setSchStatus(schModel.getSchStatus());
        } else {
            respScheduler.setSchId(0);
            respScheduler.setSchStatus(1);

        }

        if (op.equalsIgnoreCase("ON")) {
            respScheduler.setOperation(1);
        } else {
            respScheduler.setOperation(0);
        }

       /* if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }*/

        respScheduler.setDay(days);
        //respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.onSchList = new ArrayList<RespScheduler>();
                        DeviceDetailsActivity.offSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            if (respScheduler.getOperation() == 1) {
                                DeviceDetailsActivity.onSchList.add(respScheduler);

                            } else if (respScheduler.getOperation() == 0) {
                                DeviceDetailsActivity.offSchList.add(respScheduler);

                            }
                        }


                        onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });
    }

    private void addNewMoodScheduler(int days) {

        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RespMoodScheduler respMoodScheduler = new RespMoodScheduler();

        if (moodSchModel != null) {
            respMoodScheduler.setScheId(moodSchModel.getScheId());
            respMoodScheduler.setSchStatus(moodSchModel.getSchStatus());
        } else {
            respMoodScheduler.setScheId(0);
            respMoodScheduler.setSchStatus(1);

        }

        if (op.equalsIgnoreCase("ON")) {

            respMoodScheduler.setOperation(1);

        } else {
            respMoodScheduler.setOperation(0);
        }

        /*if (cBox.isChecked()) {
            respMoodScheduler.setDay(1);
        } else {
            respMoodScheduler.setDay(0);
        }*/

        respMoodScheduler.setDay(days);
        respMoodScheduler.setSchStatus(1);
        respMoodScheduler.setMoodId(moodId);
        respMoodScheduler.setTime(setTimeText);
        respMoodScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "mood scheduler JSON " + respMoodScheduler.toString());

        Call<RespMoodScheduleData> call = Constants.myInterface.addNewMoodScheduler(respMoodScheduler);
        call.enqueue(new Callback<RespMoodScheduleData>() {
            @Override
            public void onResponse(Call<RespMoodScheduleData> call, Response<RespMoodScheduleData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespMoodScheduleData respSchedulerData = response.body();
                    Log.e("add new ", "mood scheduler Response\n\n " + response.body().toString());

                    if (!respSchedulerData.isError()) {

                        MoodSchedulerActivity.onMoodSchList = new ArrayList<>();
                        MoodSchedulerActivity.offMoodSchList = new ArrayList<>();

                        for (RespMoodScheduler respMoodScheduler : respSchedulerData.getSchedulerList()) {

                            if (respMoodScheduler.getOperation() == 1) {
                                MoodSchedulerActivity.onMoodSchList.add(respMoodScheduler);

                            } else if (respMoodScheduler.getOperation() == 0) {
                                MoodSchedulerActivity.offMoodSchList.add(respMoodScheduler);

                            }
                        }

                        onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<RespMoodScheduleData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());
            }
        });
    }

    private void addDimmerScheduler(final int dimDevType, int days) {


        progressDialog = new ProgressDialog(TimePickerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating New Schedule");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        //final String dev_id = DeviceDetailsActivity.device.getDevMac();
        final String dev_id = DimmerSchedulerActivity.device.getDevMac();
        String type = "" + dimDevType;

        RequestBody bodyReq = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(1));

        RespScheduler respScheduler = new RespScheduler();

        if (schModel != null) {
            respScheduler.setSchId(schModel.getSchId());
            respScheduler.setSchStatus(schModel.getSchStatus());
        } else {
            respScheduler.setSchId(0);
            respScheduler.setSchStatus(1);
        }


        respScheduler.setOperation(skDimmer.getProgress());


        /*if (cBox.isChecked()) {
            respScheduler.setDay(1);
        } else {
            respScheduler.setDay(0);
        }*/

        respScheduler.setDay(days);
        // respScheduler.setSchStatus(1);
        respScheduler.setDevMac(dev_id);
        respScheduler.setDevType(Integer.parseInt(type));
        respScheduler.setTime(setTimeText);
        respScheduler.setUserId(Integer.parseInt(Constants.userId));


        Log.e("add new ", "scheduler JSON " + respScheduler.toString());

        Call<RespSchedulerData> call = Constants.myInterface.addNewScheduler(respScheduler);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData = response.body();
                    Log.e("add new ", "scheduler Response\n\n " + response.body().toString());


                    if (!respSchedulerData.isError()) {

                        DeviceDetailsActivity.dimmerSchList = new ArrayList<RespScheduler>();

                        for (RespScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                            DeviceDetailsActivity.dimmerSchList.add(respScheduler);

                        }

                        if (schModel != null) {
                            onBackPressed();
                        } else {
                            Intent intent = new Intent(TimePickerActivity.this, DimmerSchedulerActivity.class);
                            intent.putExtra("devType", dimDevType);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        // onBackPressed();
                    } else {
                        Toast.makeText(TimePickerActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception Scheduler ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Json Fail Scheduler ", " .. " + t.getMessage());

            }
        });


    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvSun) {

            if (daysArray.isEmpty()) {
                daysArray.add(1);
                tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvSun.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(1)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 1) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvSun.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvSun.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(1);
                tvSun.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvSun.setTextColor(getResources().getColor(R.color.appTheme));
            }


        } else if (v.getId() == R.id.tvMon) {

            if (daysArray.isEmpty()) {
                daysArray.add(2);
                tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvMon.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(2)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 2) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvMon.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvMon.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(2);
                tvMon.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvMon.setTextColor(getResources().getColor(R.color.appTheme));
            }

        } else if (v.getId() == R.id.tvTue) {

            if (daysArray.isEmpty()) {
                daysArray.add(3);
                tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvTue.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(3)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 3) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvTue.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvTue.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(3);
                tvTue.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvTue.setTextColor(getResources().getColor(R.color.appTheme));
            }

        } else if (v.getId() == R.id.tvWed) {

            if (daysArray.isEmpty()) {
                daysArray.add(4);
                tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvWed.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(4)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 4) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvWed.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvWed.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(4);
                tvWed.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvWed.setTextColor(getResources().getColor(R.color.appTheme));
            }

        } else if (v.getId() == R.id.tvThr) {

            if (daysArray.isEmpty()) {
                daysArray.add(5);
                tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvThr.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(5)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 5) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvThr.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvThr.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(5);
                tvThr.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvThr.setTextColor(getResources().getColor(R.color.appTheme));
            }

        } else if (v.getId() == R.id.tvFri) {

            if (daysArray.isEmpty()) {
                daysArray.add(6);
                tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvFri.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(6)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 6) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvFri.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvFri.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(6);
                tvFri.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvFri.setTextColor(getResources().getColor(R.color.appTheme));
            }

        } else if (v.getId() == R.id.tvSat) {

            if (daysArray.isEmpty()) {
                daysArray.add(7);
                tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvSat.setTextColor(getResources().getColor(R.color.appTheme));
            } else if (daysArray.contains(7)) {
                int pos = 0;
                for (int i = 0; i < daysArray.size(); i++) {
                    if (daysArray.get(i) == 7) {
                        pos = i;
                    }
                }
                daysArray.remove(pos);
                tvSat.setBackground(getResources().getDrawable(R.drawable.unselected_day));
                tvSat.setTextColor(getResources().getColor(R.color.appGreen));
            } else {
                daysArray.add(7);
                tvSat.setBackground(getResources().getDrawable(R.drawable.selected_day));
                tvSat.setTextColor(getResources().getColor(R.color.appTheme));
            }

        }
        Log.e("Days Array : ", "------- " + daysArray.toString());

    }
}
