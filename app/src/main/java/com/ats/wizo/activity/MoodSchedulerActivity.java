package com.ats.wizo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.fragment.OffScheduleFragment;
import com.ats.wizo.fragment.OnScheduleFragment;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.RespMoodScheduleData;
import com.ats.wizo.model.RespMoodScheduler;
import com.ats.wizo.model.RespScheduler;
import com.ats.wizo.model.RespSchedulerData;
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

public class MoodSchedulerActivity extends AppCompatActivity {

    public static Device device;

    public static ArrayList<RespScheduler> onSchList = new ArrayList<>();
    public static ArrayList<RespScheduler> offSchList = new ArrayList<>();

    public static ArrayList<RespMoodScheduler> onMoodSchList = new ArrayList<>();
    public static ArrayList<RespMoodScheduler> offMoodSchList = new ArrayList<>();

    ProgressDialog progressDialog;
    public static String type = "";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView tvCaption;

    int moodId;
    String moodName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_scheduler);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        moodId = getIntent().getIntExtra("moodId", 0);
        moodName = getIntent().getStringExtra("moodName");

        if (Variables.isInternetAvailable) {
            getMoodSchData();
        } else {
            Toast.makeText(this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
        }

        tvCaption = findViewById(R.id.tvCaption);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);

        LinearLayout myLayout = findViewById(R.id.myLayout);

        AutoTransition autoTransition = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            autoTransition = new AutoTransition();

            autoTransition.setDuration(3000);

            TransitionManager.beginDelayedTransition(myLayout, autoTransition);
        }

        try {
            tvCaption.setText(moodName);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", ".." + e.getMessage());

        }
    }

    private void getMoodSchData() {

        progressDialog = new ProgressDialog(MoodSchedulerActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodId));

        Call<RespMoodScheduleData> call = Constants.myInterface.getMoodSchedulerList(bodyUserId, bodyMoodId);
        call.enqueue(new Callback<RespMoodScheduleData>() {
            @Override
            public void onResponse(Call<RespMoodScheduleData> call, Response<RespMoodScheduleData> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespMoodScheduleData respSchedulerData = response.body();
                    Log.e("MOOD : ", "--------------------------------");
                    Log.e("## Scheduler List ", " is \n\n " + response.body().toString());

                    if (!respSchedulerData.isError()) {

                        onMoodSchList = new ArrayList<>();
                        offMoodSchList = new ArrayList<>();

                        if (respSchedulerData.getSchedulerList() != null) {
                            for (RespMoodScheduler respScheduler : respSchedulerData.getSchedulerList()) {

                                if (respScheduler.getOperation() == 1) {
                                    onMoodSchList.add(respScheduler);

                                } else if (respScheduler.getOperation() == 0) {
                                    offMoodSchList.add(respScheduler);

                                }

                            }
                        }
                        setupViewPager(viewPager);

                        tabLayout.setupWithViewPager(viewPager);
                        setupTabIcons();
                        OffScheduleFragment.offSchedulerAdapter.notifyDataSetChanged();
                        OnScheduleFragment.onSchedulerAdapter.notifyDataSetChanged();
                        OffScheduleFragment.offMoodSchedulerAdapter.notifyDataSetChanged();
                        OnScheduleFragment.onMoodSchedulerAdapter.notifyDataSetChanged();

                    }


                } catch (Exception e) {
                    Log.e("Exception ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespMoodScheduleData> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Exception ", " .. " + t.getMessage());

            }
        });


    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OnScheduleFragment(moodId, moodName), "ON Scheduler");
        adapter.addFragment(new OffScheduleFragment(moodId, moodName), "OFF Scheduler");

        viewPager.setAdapter(adapter);
        //  viewPager.setPageTransformer(true, new CubeOutTransformer());


    }

    private void setupTabIcons() {

        Integer[] tabIcons = new Integer[]{R.mipmap.schedule_on_icon, R.mipmap.schedule_off_icon};
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private Transition exitTransition() {
        ChangeBounds bounds = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            bounds = new ChangeBounds();

            bounds.setInterpolator(new BounceInterpolator());
            bounds.setDuration(2000);
        }
        return bounds;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //   startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        //   finish();

    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int title) {

            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setPositiveButton(R.string.Reboot,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    ((MoodSchedulerActivity) getActivity())
                                            .doPositiveClick();
                                }
                            })
                    .setNegativeButton(R.string.Reset,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    ((MoodSchedulerActivity) getActivity())
                                            .doNegativeClick();
                                }
                            }).create();
        }
    }


    public void doPositiveClick() {


        Log.i("FragmentAlertDialog", "Positive click!");
    }

    public void doNegativeClick() {

        Log.i("FragmentAlertDialog", "Negative click!");
    }


}
