package com.ats.wizo.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.ats.wizo.model.RespScheduler;
import com.ats.wizo.model.RespSchedulerData;
import com.ats.wizo.model.Schedular;
import com.ats.wizo.sqlite.DBHandler;
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

public class DeviceDetailsActivity extends AppCompatActivity {

    public static Device device;

    public static ArrayList<RespScheduler> onSchList = new ArrayList<>();
    public static ArrayList<RespScheduler> offSchList = new ArrayList<>();
    public static ArrayList<RespScheduler> fanSchList = new ArrayList<>();
    public static ArrayList<RespScheduler> dimmerSchList = new ArrayList<>();
    ProgressDialog progressDialog;
    public  static String type="";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView tvCaption;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        if(Variables.isInternetAvailable) {
            getDeviceSchData();
        }else{
            Toast.makeText(this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
        }

        boolean displayed= Variables.sh.getBoolean("isScheHelpDisplayed",false);
        if(!displayed) {
            showSchedulerHelp();
        }
        tvCaption =  findViewById(R.id.tvCaption);
        viewPager =  findViewById(R.id.viewpager);
        tabLayout =  findViewById(R.id.tabs);

        LinearLayout myLayout = findViewById(R.id.myLayout);
        ImageView ivEditCaption = findViewById(R.id.ivEditCaption);


        ivEditCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(Variables.isInternetAvailable) {

                    final Dialog dialog = new Dialog(DeviceDetailsActivity.this,R.style.MyAlertDialogStyle);
                    dialog.setContentView(R.layout.dialog_edit_caption);

                    ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                    // set the custom dialog components - text, image and button
                    final EditText editText = dialog.findViewById(R.id.edCaption);
                    Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                    btnSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            String caption = editText.getText().toString();

                            if (caption.equalsIgnoreCase("")) {
                                editText.setError("Please enter valid name");
                            } else {

                                updateCaption(caption, device);
                                dialog.dismiss();
                            }


                        }
                    });




                    dialog.show();

                }else{
                    Toast.makeText(DeviceDetailsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    
                }

            }
//
        });

        AutoTransition autoTransition = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            autoTransition = new AutoTransition();

            autoTransition.setDuration(3000);

            TransitionManager.beginDelayedTransition(myLayout, autoTransition);
        }
        try {
            tvCaption.setText(device.getDevCaption());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Excption", ".." + e.getMessage());

        }

    }

    private void updateCaption(String caption, Device  device) {

        tvCaption.setText(caption);

        DBHandler dbHandler=new DBHandler(getApplicationContext());
        dbHandler.updateDeviceCaption(caption,device);

        registerDeviceToServer();



    }

    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(DeviceDetailsActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Updating");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        DBHandler dbHandler = new DBHandler(DeviceDetailsActivity.this);
        List<Device> allDeviceList = dbHandler.getAllDevices();
        List<DataUploadDevices> deviceList = new ArrayList<>();

        for (Device device : allDeviceList) {

            DataUploadDevices dataUploadDevices = new DataUploadDevices(Integer.parseInt(Constants.userId), device.getDevIp(), device.getDevMac(), device.getDevCaption(), device.getDevType(), device.getDevPosition(), device.getDevSsid(), device.getDevRoomId(), device.getDevIsUsed());

            deviceList.add(dataUploadDevices);
        }


        Call<JsonObject> call = Constants.myInterface.uploadDeviceData(deviceList);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    if (!jsonObject.getBoolean("error")) {
                        Toast.makeText(DeviceDetailsActivity.this, "Caption Updated Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DeviceDetailsActivity.this, "Failed To Update, Please Try Again...", Toast.LENGTH_SHORT).show();
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
                Log.e("Data upload ", "failed " + t.getMessage());
                Toast.makeText(DeviceDetailsActivity.this, "Something Went Wrong, Please Try Again...", Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void getDeviceSchData() {

        progressDialog = new ProgressDialog(DeviceDetailsActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyDevMac = RequestBody.create(MediaType.parse("text/plain"), device.getDevMac());
        RequestBody bodyDevType = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(device.getDevType()));

        Log.e("Parameter : ","--------- User Id : "+bodyUserId+"              MAC : "+bodyDevMac+"             TYPE : "+bodyDevType);


        Call<RespSchedulerData> call = Constants.myInterface.getSchedulerList( bodyUserId,bodyDevMac,bodyDevType);
        call.enqueue(new Callback<RespSchedulerData>() {
            @Override
            public void onResponse(Call<RespSchedulerData> call, Response<RespSchedulerData> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    RespSchedulerData respSchedulerData= response.body();

                    Log.e("## Scheduler List "," is \n\n "+response.body().toString());

                    if(!respSchedulerData.isError()){

                        onSchList = new ArrayList<RespScheduler>();
                        offSchList = new ArrayList<RespScheduler>();

                        for(RespScheduler respScheduler : respSchedulerData.getSchedulerList() ){

                                if(respScheduler.getOperation()==1){
                                  onSchList.add(respScheduler);

                                }else if(respScheduler.getOperation() ==0){
                                    offSchList.add(respScheduler);

                                }else{
                                    onSchList.add(respScheduler);
                                }

                        }
                        setupViewPager(viewPager);

                        tabLayout.setupWithViewPager(viewPager);
                        setupTabIcons();
                        OffScheduleFragment.offSchedulerAdapter.notifyDataSetChanged();
                        OnScheduleFragment.onSchedulerAdapter.notifyDataSetChanged();

                    }



                } catch (Exception e) {
                    Log.e("Exception ", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<RespSchedulerData> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Exception ", " .. " + t.getMessage());

            }
        });


    }


    private void setupViewPager(ViewPager viewPager) {
        DeviceDetailsActivity.ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OnScheduleFragment(), "ON Scheduler");
        adapter.addFragment(new OffScheduleFragment(), "OFF Scheduler");

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
                                    ((DeviceDetailsActivity) getActivity())
                                            .doPositiveClick();
                                }
                            })
                    .setNegativeButton(R.string.Reset,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    ((DeviceDetailsActivity) getActivity())
                                            .doNegativeClick();
                                }
                            }).create();
        }
    }


    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }

    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }


    public void showSchedulerHelp(){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int trans = Color.parseColor("#49000000");

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(trans));

        dialog.setContentView(R.layout.help_scheduler);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Variables.e.putBoolean("isScheHelpDisplayed",true);
                Variables.e.commit();
                Variables.e.apply();
            }
        });
        dialog.show();
    }

}
