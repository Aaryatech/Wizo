package com.ats.wizo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.User;
import com.ats.wizo.util.ImagePicker;
import com.ats.wizo.util.LocationAddress;
import com.ats.wizo.util.LocationService;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edUName, edMob, edEmail, edPassword;
    private ProgressDialog progressDialog;

    Location gpsLocation, networkLocation;
    private Context context;

    private String userAddress = "";
    private String userPic = "";

    CircleImageView ivProfile;
    private static final int PICK_IMAGE_ID = 234;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edUName = findViewById(R.id.edUName);
        edMob = findViewById(R.id.edMob);
        edEmail = findViewById(R.id.edEmail);
        ivProfile = findViewById(R.id.ivProfile);

        Button btnRegister = findViewById(R.id.btnRegister);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1001);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int rc = ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA);

                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(RegisterActivity.this);
                    startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                } else {
                    requestCameraPermission();
                }

            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isValid = true;
                String email = "NA";

                email = edEmail.getText().toString();


                if (userAddress.equalsIgnoreCase("")) {
                    getAddress();
                }


                if (edUName.getText().toString().equalsIgnoreCase("")) {
                    isValid = false;
                    edUName.setError("User name required");
                    edUName.requestFocus();

                } else if (edMob.getText().toString().length() != 10) {
                    isValid = false;
                    edMob.setError("Please enter valid mobile no.");
                    edMob.requestFocus();
                } else if (edEmail.getText().toString().length() > 0 && !isValidEmail(edEmail.getText().toString())) {
                    isValid = false;
                    edEmail.setError("Please enter valid email address");
                    edEmail.requestFocus();
                }

                if (isValid && isOnline()) {

                    User user = new User(edUName.getText().toString(), edMob.getText().toString(), edEmail.getText().toString(), userAddress, 1);
                    user.setUserPic(userPic);
                    Log.e("User Object "," is "+user.toString());

                       registerUser(user);
                }

            }
        });

    }

    private void registerUser(User user) {

        progressDialog = new ProgressDialog(RegisterActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Creating User Profile");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        Call<JsonObject> call = Constants.myInterface.userRegister(user);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.e("Success", "..");

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    JSONObject object = new JSONObject(response.body().toString());

                    Log.e("Json Register", " .. " + object.toString());

                    boolean error = object.getBoolean("error");
                    String msg = object.getString("message");
                    if (error) {
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "User profile created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Exception", " .. " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Fail", "..");
                Log.e("Json Register", " .. " + t.getMessage());

            }
        });


    }


    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet connection ! ", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        RegisterActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    userAddress = bundle.getString("address");
                    String pincode = bundle.getString("pincode");
                    String city = bundle.getString("locality");
                    Log.e("address", ".. " + userAddress);
                    break;
                default:
                    locationAddress = null;
            }


        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        getAddress();

    }

    private void getAddress() {

        boolean gps_enabled = false;
        boolean network_enabled = false;

        LocationManager lm = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location net_loc = null, gps_loc = null, finalLoc = null;

        LocationService locationService = new LocationService(getApplicationContext());

        gpsLocation = locationService
                .getLocation(LocationManager.GPS_PROVIDER);

        networkLocation = locationService
                .getLocation(LocationManager.NETWORK_PROVIDER);


        if (gps_enabled)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (network_enabled)
            net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null && net_loc != null) {

            //smaller the number more accurate result will
            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;


        } else {

            if (gps_loc != null) {
                finalLoc = gps_loc;
            } else if (net_loc != null) {
                finalLoc = net_loc;
            }
        }


        double longitude;
        double latitude;
        if (gps_loc != null) {
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());

            Log.e("Loc ", ".." + locationAddress.toString());

        } else if (net_loc != null) {
            latitude = networkLocation.getLatitude();
            longitude = networkLocation.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());

            Log.e("Loc ", ".." + locationAddress);
        } else if (!gps_enabled) {
            showSettingsAlert();
        }

    }


    // Handles the requesting of the camera permission.
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");

            Intent chooseImageIntent = ImagePicker.getPickImageIntent(RegisterActivity.this);
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Camera Persmission")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case PICK_IMAGE_ID:

                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);

                if (bitmap != null) {

                    uploadUserPic(bitmap);
                    ivProfile.setImageBitmap(bitmap);
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void uploadUserPic(Bitmap bitmap) {

        progressDialog = new ProgressDialog(RegisterActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        File f = new File(getApplicationContext().getCacheDir(), "user");
        try {
            f.createNewFile();


//Convert bitmap to byte array

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), f);


        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", f.getName(), requestFile);
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String timestamp = s.format(new Date());

        RequestBody bodyName = RequestBody.create(MediaType.parse("text/plain"), timestamp + ".png");

        Call<JsonObject> call = Constants.myInterface.uploadFile(body, bodyName);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    Log.e("Image Respo ", " .. " + jsonObject.toString());

                    boolean error = jsonObject.getBoolean("error");
                    if (error) {
                        Toast.makeText(RegisterActivity.this, "Failed, Please try again..", Toast.LENGTH_SHORT).show();
                        userPic = "";
                    } else {
                        userPic = jsonObject.getString("imageName");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


    }


}

