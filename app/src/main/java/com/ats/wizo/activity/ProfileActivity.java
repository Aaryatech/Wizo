package com.ats.wizo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.RespRoomData;
import com.ats.wizo.model.User;
import com.ats.wizo.util.ImagePicker;
import com.google.gson.JsonObject;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_ID = 234;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private static final String TAG ="ProfileActivity" ;

    CircleImageView ivProfile;

    Button btnSaveProfile;
    EditText edName,edMobNo,edEmail;
    private ProgressDialog progressDialog;
    String userPic;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageView ivBack = findViewById(R.id.ivBack);
         ivProfile = findViewById(R.id.ivProfile);

        btnSaveProfile =findViewById(R.id.btnSaveProfile);
        edEmail =findViewById(R.id.edEmail);
        edMobNo =findViewById(R.id.edMobNo);
        edName =findViewById(R.id.edName);


        getUserData();


        ivBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rc = ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA);

                if (rc == PackageManager.PERMISSION_GRANTED) {
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileActivity.this);
                    startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                } else {
                    requestCameraPermission();
                }

            }
        });


        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(edName.getText().toString().equalsIgnoreCase("")){

                    edName.setError("Please enter name");

                }else{

                    String name =edName.getText().toString();
                    user.setUserName(name);
                    user.setUserEmail(edEmail.getText().toString());

                    user.setUserPic(userPic);

                    updateUserProfile(user);

                }

            }
        });


    }

    private void updateUserProfile(User user) {


        Log.e("updating Profile "," for "+Constants.userId);


        final ProgressDialog progressDialog=new ProgressDialog(ProfileActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading ");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        Call<JsonObject> call = Constants.myInterface.updateUserProfile(user);
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
                        Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                       // finish();
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

    private void getUserData() {

        Log.e("User Profile "," for "+Constants.userId);


        final ProgressDialog progressDialog=new ProgressDialog(ProfileActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading ");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        Call<JsonObject> call = Constants.myInterface.getUserDetails(bodyUserId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                try{
                    Log.e("User Profile "," respo "+response.body().toString());

                    JSONObject jsonObject= new JSONObject(response.body().toString());



                    boolean error =jsonObject.getBoolean("error");

                    if(!error){

                       JSONObject userObj= jsonObject.getJSONObject("user");

                        edName.setText(userObj.getString("userName"));
                        edMobNo.setText(userObj.getString("userMobile"));
                        edEmail.setText(userObj.getString("userEmail"));

                         userPic=userObj.getString("userPic");

                         user =new User();
                         user.setUserId(userObj.getInt("userId"));
                         user.setAuthKey(userObj.getString("authKey"));
                         user.setUserPic(userPic);
                         user.setUserEmail(userObj.getString("userEmail"));
                         user.setUserIsUsed(1);
                         user.setUserLocation(userObj.getString("userLocation"));
                         user.setUserMobile(userObj.getString("userMobile"));
                         user.setUserName(userObj.getString("userName"));


                        Picasso.get()
                                .load(Constants.imagePath+userPic)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .placeholder(R.mipmap.ic_user_pic)
                                .into(ivProfile);


                    }else{

                        Toast.makeText(ProfileActivity.this, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();
                    }


                }catch (Exception  e)
                {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if(progressDialog.isShowing()){

                    progressDialog.dismiss();

                }

                Toast.makeText(ProfileActivity.this, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();


            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {

            case PICK_IMAGE_ID:

                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);

                    if(bitmap!= null) {
                        ivProfile.setImageBitmap(bitmap);
                        uploadUserPic(bitmap);

                    }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void uploadUserPic(Bitmap bitmap) {


        progressDialog = new ProgressDialog(ProfileActivity.this,R.style.MyAlertDialogStyle);
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

        Log.e("User Pic" ,":- "+userPic);

        if(userPic == null || userPic.equalsIgnoreCase("")) {
           userPic = s.format(new Date());
           userPic =userPic+ ".png";
        }
        RequestBody bodyName = RequestBody.create(MediaType.parse("text/plain"), userPic );

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
                        Toast.makeText(ProfileActivity.this, "Failed, Please try again..", Toast.LENGTH_SHORT).show();
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


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(ProfileActivity.this, "Failed, Please try again..", Toast.LENGTH_SHORT).show();

            }
        });


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
            boolean autoFocus = true;
            boolean useFlash = false;
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileActivity.this);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Camera Permission")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.e("ProfileAct "," pause method");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ProfileAct "," resume method");
    }



}
