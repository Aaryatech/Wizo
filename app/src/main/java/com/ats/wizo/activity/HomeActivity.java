package com.ats.wizo.activity;

import android.animation.Animator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.adapter.MoodAdapter;
import com.ats.wizo.adapter.RoomsAdapter;
import com.ats.wizo.barcode.BarcodeCaptureActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.DataUploadDevices;
import com.ats.wizo.model.Device;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.Room;
import com.ats.wizo.model.ScanDevice;
import com.ats.wizo.sqlite.DBHandler;
import com.ats.wizo.util.ConnectivityChangeReceiver;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1001;
    private static final String LOG_TAG = "Home Activity";

    FloatingActionButton fab, fabNew, fabExisting;
    LinearLayout fabLayout1;
    View fabBGLayout;
    boolean isFABOpen = false;

    private static boolean isRegister;
    private BroadcastReceiver connectivityChangeReceiver = null;
    public static List<String> topicList = new ArrayList<>();
    public static List<String> routerList = new ArrayList<>();

    static HomeActivity homeActivity;

    List<MoodMaster> moodMasterList;

    MoodAdapter moodAdapter;
    GridView gvMoods;

    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GridView gvTiles = findViewById(R.id.gvTiles);
        gvMoods = findViewById(R.id.gvMoods);

        ImageView ivRefresh = findViewById(R.id.ivRefresh);
        ImageView ivSetting = findViewById(R.id.ivSetting);

        homeActivity = this;

        boolean displayed = Variables.sh.getBoolean("isHomeHelpDisplayed", false);

        if (!displayed) {
            showHomeHelp();
        }


        if (connectivityChangeReceiver == null) {
            connectivityChangeReceiver = new ConnectivityChangeReceiver(findViewById(R.id.homeLayout));
        }

        final List<Room> roomList = getRoomsList();
        Log.e("Room LIST ","-------------- "+roomList);

        RoomsAdapter roomsAdapter = new RoomsAdapter(getApplicationContext(), roomList);
        gvTiles.setAdapter(roomsAdapter);


        fabLayout1 = findViewById(R.id.fabLayout1);

        fab = findViewById(R.id.fab);
        fabNew = findViewById(R.id.fabNew);
        fabExisting = findViewById(R.id.fabExisting);

        fabBGLayout = findViewById(R.id.fabBGLayout);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (Variables.isInternetAvailable) {


                    dialog = new Dialog(HomeActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_before_config);
                    dialog.show();


                    CheckBox cbMessage = dialog.findViewById(R.id.cbChecked);

                    final Button btnContinue = dialog.findViewById(R.id.btnContinue);
                    btnContinue.setVisibility(View.INVISIBLE);

                    cbMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                            if (isChecked) {

                                btnContinue.setVisibility(View.VISIBLE);
                            } else {

                                btnContinue.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    btnContinue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), SelectHomeRouterActivity.class);
                            intent.putExtra("isForConfig", true);
                            startActivity(intent);
                            finish();
                        }
                    });


                } else {
                    Toast.makeText(HomeActivity.this, "Please Connect To Internet", Toast.LENGTH_SHORT).show();

                }


            }
        });

        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFABMenu();
                Intent intent = new Intent(getApplicationContext(), SelectHomeRouterActivity.class);
                intent.putExtra("isForConfig", true);
                startActivity(intent);
                finish();


            }
        });


        fabExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFABMenu();
                Intent intent = new Intent(getApplicationContext(), ReconfigInfoActivity.class);
                startActivity(intent);


            }
        });

        ivRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(getIntent());
                //finish();

            }
        });

        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        });


    }

    private List<MoodMaster> getMoodsList() {

        DBHandler dbHandler = new DBHandler(getApplicationContext());

        return dbHandler.getAllMoods();
    }

    public void showPopUpMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);

        PopupMenu popup = new PopupMenu(wrapper, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.my_menu, popup.getMenu());

        Menu menu = popup.getMenu();
        MenuItem item = menu.findItem(R.id.synch);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.synch:
                        Log.e("Synch data ", "...");
                        //  newAsynchUpdateIP newAsynchUpdateIP = new newAsynchUpdateIP();
                        // newAsynchUpdateIP.execute();

                        break;
                    case R.id.registerToServer:
                        Log.e("Register data ", "...");
                        if (isOnline(getApplicationContext())) {
                            registerDeviceToServer();
                        } else {
                            Toast.makeText(HomeActivity.this, "Please connect to internet first", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

                return true;
            }
        });

        popup.show();

    }

    private void registerDeviceToServer() {

        final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setTitle("Uploading Data");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        DBHandler dbHandler = new DBHandler(getApplicationContext());
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
                        Toast.makeText(HomeActivity.this, "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to upload data, Please try again...", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(HomeActivity.this, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();

            }
        });

    }

    // Get room list to set as tiles
    private List<Room> getRoomsList() {

        DBHandler dbHandler = new DBHandler(getApplicationContext());

        List<Room> roomsList = dbHandler.getAllRooms();
        //   Log.e("Home Act ", " Room List " + roomsList.toString());

        return roomsList;
    }

    private void showFABMenu() {
        isFABOpen = true;
        fabLayout1.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);

        fab.animate().rotationBy(225);
        fabLayout1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));

    }

    private void closeFABMenu() {

        isFABOpen = false;

        fabBGLayout.setVisibility(View.GONE);

        fab.animate().rotationBy(-225);

        fabLayout1.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fabLayout1.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();


        moodMasterList = getMoodsList();

        moodAdapter = new MoodAdapter(getApplicationContext(), moodMasterList, HomeActivity.this);
        gvMoods.setAdapter(moodAdapter);


        DBHandler dbHandler = new DBHandler(getApplicationContext());
        topicList = dbHandler.getAllTopics();

        routerList = dbHandler.getAllHomeRouters();

        if (!isRegister) {
            registerReceiver(connectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            isRegister = true;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isRegister) {
            unregisterReceiver(connectivityChangeReceiver);
            isRegister = false;
        }

    }


    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Exit");
            builder.setMessage("Do you really want to Exit ?");

            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finishAffinity();
                            finish();
                        }
                    });

            String negativeText = getString(android.R.string.cancel);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case BARCODE_READER_REQUEST_CODE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        Point[] p = barcode.cornerPoints;
                        // mResultTextView.setText(barcode.displayValue);

                        String barcodeMac = barcode.displayValue;


                        uploadScanDeviceToServer(barcodeMac);


                    } else {
                        Toast.makeText(this, "No Wizzo Device Found", Toast.LENGTH_SHORT).show();

                    }

                } else Log.e(LOG_TAG + String.format(" Barcode Error "),
                        CommonStatusCodes.getStatusCodeString(resultCode));
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void uploadScanDeviceToServer(final String barcode) {

        final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ScanDevice scanDevice = new ScanDevice();
        scanDevice.setDevMac(barcode.toLowerCase());
        scanDevice.setUserId(Integer.parseInt(Constants.userId));

        Call<JsonObject> call = Constants.myInterface.addNewScanDevice(scanDevice);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {

                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        dbHandler.addNewMac(barcode.toLowerCase());

                        //      Toast.makeText(homeActivity, "MAC " + barcode.toLowerCase(), Toast.LENGTH_SHORT).show();
                        closeFABMenu();

                        Intent intent = new Intent(getApplicationContext(), SelectHomeRouterActivity.class);
                        intent.putExtra("isForConfig", true);
                        startActivity(intent);
                        finish();

                    } else {

                        Toast.makeText(HomeActivity.this, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();
                    }

                    Log.e("", "");

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


    }

  /*  private boolean isValidQR(String displayValue) {

        try {
            JSONObject object = new JSONObject(displayValue);
            String id = object.getString("id");
            if (id.equalsIgnoreCase("Wizzo")) {
                String mac = object.getString("mac");
                Log.e(LOG_TAG, " Mac address " + mac);
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
*/



    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(context.getApplicationContext(), "No Internet connection ! ", Toast.LENGTH_LONG).show();
            Variables.isInternetAvailable = false;

            return false;
        }
        Variables.isInternetAvailable = true;
        return true;
    }


    public void showHomeHelp() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int trans = Color.parseColor("#49000000");

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(trans));

        dialog.setContentView(R.layout.help_home);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Variables.e.putBoolean("isHomeHelpDisplayed", true);
                Variables.e.apply();
                Variables.e.commit();

            }
        });
        dialog.show();
    }

    public static HomeActivity getInstance() {
        return homeActivity;
    }

}
