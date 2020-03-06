package com.ats.wizo.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.activity.MoodDeviceListActivity;
import com.ats.wizo.activity.ProfileActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.MoodMaster;
import com.ats.wizo.model.Room;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoodAdapter extends BaseAdapter {


    private Context mContext;
    private List<MoodMaster> moodMasterList;
    ProgressDialog progressDialog;
    Activity activity;

    public MoodAdapter(Context mContext, List<MoodMaster> roomList, Activity activity) {
        this.mContext = mContext;
        this.moodMasterList = roomList;
        this.activity = activity;
    }


    @Override
    public int getCount() {
        return moodMasterList.size();
    }

    @Override
    public MoodMaster getItem(int position) {
        return moodMasterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.room_grid_view, null);
            TextView textView = (TextView) grid.findViewById(R.id.tvGridRoomName);
            ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

            textView.setText(moodMasterList.get(position).getMoodName());
            imageView.setImageResource(R.mipmap.ic_mood);


            grid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent deviceListIntent = new Intent(mContext, MoodDeviceListActivity.class);
                    deviceListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    deviceListIntent.putExtra("moodId", moodMasterList.get(position).getMoodId());
                    deviceListIntent.putExtra("moodName", moodMasterList.get(position).getMoodName());
                    mContext.startActivity(deviceListIntent);

                }
            });

            grid.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showAlert(moodMasterList.get(position));

                    return true;
                }
            });

        } else {
            grid = (View) convertView;
        }


        if (moodMasterList.get(position).getMoodStatus() == 1) {

            int color = Color.parseColor("#25A3DE");
            grid.setBackgroundColor(color);

        }


        return grid;
    }


    private void showAlert(final MoodMaster moodMaster) {

        if (Variables.isInternetAvailable) {

            final Dialog dialog = new Dialog(HomeActivity.getInstance());
            dialog.setContentView(R.layout.dialog_edit_room);

            ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

            // set the custom dialog components - text, image and button
            final EditText editText = dialog.findViewById(R.id.edCaption);
            Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
            Button btnDelete = dialog.findViewById(R.id.btnDelete);
            TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);

            tvTitle.setText("Edit Mood");
            btnDelete.setText("Delete Mood");

            editText.setText(moodMaster.getMoodName());

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String caption = editText.getText().toString();

                    if (caption.equalsIgnoreCase("")) {
                        editText.setError("Please enter valid name");
                    } else {

                        //  updateCaption(caption, device);
                        updateMoodCaption(moodMaster, caption);


                        dialog.dismiss();
                    }


                }
            });


            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();

                    showDeleteAlert(moodMaster);

                }
            });


            dialog.show();

        } else {
            Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();

        }


    }

    private void updateMoodCaption(final MoodMaster moodMaster, final String caption) {

        progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodMaster.getMoodId()));
        RequestBody bodyMoodName = RequestBody.create(MediaType.parse("text/plain"), caption);

        Call<JsonObject> call = Constants.myInterface.updateMoodCaption(bodyUserId, bodyMoodId, bodyMoodName);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    Log.e("Caption update ", " respo " + jsonObject.toString());

                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {

                        Toast.makeText(activity, "Caption updated successfully", Toast.LENGTH_SHORT).show();
                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.updateMoodCaption(caption, moodMaster.getMoodId());

//                        HomeActivity.getInstance().finish();
//
//                        mContext.startActivity(new Intent(mContext, HomeActivity.class));


                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                    } else {


                        Toast.makeText(activity, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();

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

                Toast.makeText(activity, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void showDeleteAlert(final MoodMaster moodMaster) {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.getInstance(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete");
        builder.setMessage("Configuration related to this Mood will be lost. Are you sure want to delete this Mood?");

        String positiveText = mContext.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        deleteMoodFromServer(moodMaster);

                    }
                });

        String negativeText = mContext.getString(android.R.string.cancel);
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

    private void deleteMoodFromServer(final MoodMaster moodMaster) {

        progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        RequestBody bodyUserId = RequestBody.create(MediaType.parse("text/plain"), Constants.userId);
        RequestBody bodyMoodId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(moodMaster.getMoodId()));

        Call<JsonObject> call = Constants.myInterface.deleteMood(bodyUserId, bodyMoodId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                try {

                    if (progressDialog.isShowing()) {

                        progressDialog.dismiss();

                    }

                    Log.e("Delete Mood ", " respo " + response.body().toString());

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    boolean error = jsonObject.getBoolean("error");

                    if (!error) {

                        Toast.makeText(activity, "Mood Deleted Successfully", Toast.LENGTH_SHORT).show();

                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.deleteMood(moodMaster.getMoodId());
                        //HomeActivity.getInstance().finish();
                        //mContext.startActivity(new Intent(mContext, HomeActivity.class));


                        //HomeActivity.getInstance().finish();

                        Intent intent = new Intent(mContext, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);

                    } else {

                        Toast.makeText(activity, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();

                    }


                } catch (Exception e) {

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(activity, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show();


            }
        });


    }

}
