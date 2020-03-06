package com.ats.wizo.fragment;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.DeviceDetailsActivity;
import com.ats.wizo.activity.TimePickerActivity;
import com.ats.wizo.adapter.MoodSchedulerAdapter;
import com.ats.wizo.adapter.SchedulerAdapter;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Schedular;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ats.wizo.activity.DeviceDetailsActivity.device;
import static com.ats.wizo.activity.DeviceDetailsActivity.onSchList;
import static com.ats.wizo.activity.MoodSchedulerActivity.onMoodSchList;

/**
 * Created by eis-01 on 16/2/17.
 */

public class OnScheduleFragment extends Fragment {


    private ListView lvOnSch;
    FloatingActionButton fabOnScheduler;
    public static SchedulerAdapter onSchedulerAdapter;
    public static MoodSchedulerAdapter onMoodSchedulerAdapter;

    int moodId;
    String moodName;

    public OnScheduleFragment() {
    }


    @SuppressLint("ValidFragment")
    public OnScheduleFragment(int moodId, String moodName) {
        this.moodId = moodId;
        this.moodName = moodName;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = null;
        rootView = inflater.inflate(R.layout.fragment_on_sch, null);

        Log.e("MOOD ID : ", "----------------" + moodId);

        lvOnSch = rootView.findViewById(R.id.lvOnSch);

        fabOnScheduler = rootView.findViewById(R.id.fabOnScheduler);

        Log.e("Onsch list ", " .. " + onSchList.toString());


        fabOnScheduler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Variables.isInternetAvailable) {

                    if (moodId == 0) {
                        Intent intent = new Intent(getActivity(), TimePickerActivity.class);
                        intent.putExtra("op", "ON");
                        intent.putExtra("type", DeviceDetailsActivity.device.getDevType());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), TimePickerActivity.class);
                        intent.putExtra("op", "ON");
                        intent.putExtra("moodId", moodId);
                        intent.putExtra("type", -1);
                        startActivity(intent);
                    }


                } else {
                    Toast.makeText(getActivity(), "Please Connect To Internet First", Toast.LENGTH_SHORT).show();
                }


            }
        });
        return rootView;


    }


    @Override
    public void onResume() {
        super.onResume();

        if (moodId == 0) {
            onSchedulerAdapter = new SchedulerAdapter(getActivity(), onSchList, getActivity());
            lvOnSch.setAdapter(onSchedulerAdapter);
        } else {
            onMoodSchedulerAdapter = new MoodSchedulerAdapter(getActivity(), onMoodSchList, getActivity(), moodName);
            lvOnSch.setAdapter(onMoodSchedulerAdapter);
        }

    }
}
