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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.activity.AddNewRoomActivity;
import com.ats.wizo.activity.DeviceDetailsActivity;
import com.ats.wizo.activity.DeviceListActivity;
import com.ats.wizo.activity.HomeActivity;
import com.ats.wizo.common.Variables;
import com.ats.wizo.constant.Constants;
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

/**
 * Created by maxadmin on 9/1/18.
 */

public class RoomsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Room> roomList;
    private static LayoutInflater inflater = null;


    public RoomsAdapter(Context mContext, List<Room> roomList) {
        this.mContext = mContext;
        this.roomList = roomList;
        this.inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView textView;
        ImageView imageView;
        LinearLayout linearLayout;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.room_grid_view, null);
        holder.textView = rowView.findViewById(R.id.tvGridRoomName);
        holder.imageView = rowView.findViewById(R.id.grid_image);
        holder.linearLayout = rowView.findViewById(R.id.linearLayout);

        holder.textView.setText(""+roomList.get(position).getRoomName());

        final Room room =roomList.get(position);
        switch (roomList.get(position).getRoomIcon()) {

            case "LR":
                holder. imageView.setImageResource(R.mipmap.living_room_icon);
                break;
            case "MB":
                holder.imageView.setImageResource(R.mipmap.master_bedroom_icon);
                break;
            case "K":
                holder. imageView.setImageResource(R.mipmap.kitchen_icon);
                break;
            case "B":
                holder.imageView.setImageResource(R.mipmap.bedroom_icon);
                break;
            default:
                holder.imageView.setImageResource(R.mipmap.living_room_icon);
                break;

        }


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deviceListIntent = new Intent(mContext, DeviceListActivity.class);
                deviceListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                deviceListIntent.putExtra("roomId", roomList.get(position).getRoomId());
                deviceListIntent.putExtra("roomName", roomList.get(position).getRoomName());
                deviceListIntent.putExtra("roomIcon", roomList.get(position).getRoomIcon());
                mContext.startActivity(deviceListIntent);

            }
        });

        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //   Toast.makeText(mContext, "Room Edit", Toast.LENGTH_SHORT).show();

                if(Variables.isInternetAvailable) {

                    final Dialog dialog = new Dialog(HomeActivity.getInstance());
                    dialog.setContentView(R.layout.dialog_edit_room);

                    ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                    // set the custom dialog components - text, image and button
                    final EditText editText = dialog.findViewById(R.id.edCaption);
                    Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
                    Button  btnDelete = dialog.findViewById(R.id.btnDelete);


                    String roomName=room.getRoomName();
                    editText.setText(roomName);

                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String caption = editText.getText().toString();

                            if (caption.equalsIgnoreCase("")) {
                                editText.setError("Please enter valid name");
                            } else {

                                //  updateCaption(caption, device);

                                room.setRoomName(caption);
                                room.setUserId(Integer.valueOf(Constants.userId));

                                addNewRoomToServer(room);

                                dialog.dismiss();
                            }

                        }
                    });

                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                            room.setUserId(Integer.valueOf(Constants.userId));

                            showAlert(room);

                        }
                    });

                    dialog.show();

                }else{

                    Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();

                }

                return true;
            }
        });


        //-----------------------------------------------------

       /* View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.room_grid_view, null);

            TextView textView = (TextView) grid.findViewById(R.id.tvGridRoomName);
            ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

            Log.e("POSITION : ","------- "+position);
            Log.e("ROOM NAME : ","------- "+roomList.get(position).getRoomName());

            textView.setText(roomList.get(position).getRoomName());
            textView.setTextColor(Color.parseColor("#FFFFFFFF"));

            final Room room =roomList.get(position);
            switch (roomList.get(position).getRoomIcon()) {

                case "LR":
                    imageView.setImageResource(R.mipmap.living_room_icon);
                    break;
                case "MB":
                    imageView.setImageResource(R.mipmap.master_bedroom_icon);
                    break;
                case "K":
                    imageView.setImageResource(R.mipmap.kitchen_icon);
                    break;
                case "B":
                    imageView.setImageResource(R.mipmap.bedroom_icon);
                    break;
                default:
                    imageView.setImageResource(R.mipmap.living_room_icon);
                    break;

            }

            grid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent deviceListIntent = new Intent(mContext, DeviceListActivity.class);
                    deviceListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    deviceListIntent.putExtra("roomId", roomList.get(position).getRoomId());
                    deviceListIntent.putExtra("roomName", roomList.get(position).getRoomName());
                    deviceListIntent.putExtra("roomIcon", roomList.get(position).getRoomIcon());
                    mContext.startActivity(deviceListIntent);

                }
            });

            grid.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                 //   Toast.makeText(mContext, "Room Edit", Toast.LENGTH_SHORT).show();

                    if(Variables.isInternetAvailable) {

                        final Dialog dialog = new Dialog(HomeActivity.getInstance());
                        dialog.setContentView(R.layout.dialog_edit_room);

                        ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                        // set the custom dialog components - text, image and button
                        final EditText editText = dialog.findViewById(R.id.edCaption);
                        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
                        Button  btnDelete = dialog.findViewById(R.id.btnDelete);


                        String roomName=room.getRoomName();
                        editText.setText(roomName);

                        btnUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String caption = editText.getText().toString();

                                if (caption.equalsIgnoreCase("")) {
                                    editText.setError("Please enter valid name");
                                } else {

                                  //  updateCaption(caption, device);

                                    room.setRoomName(caption);
                                    room.setUserId(Integer.valueOf(Constants.userId));

                                    addNewRoomToServer(room);

                                    dialog.dismiss();
                                }

                            }
                        });

                        btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                room.setUserId(Integer.valueOf(Constants.userId));

                                showAlert(room);

                            }
                        });

                        dialog.show();

                    }else{

                        Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();

                    }

                    return true;
                }
            });



        } else {
            grid = (View) convertView;
        }*/

        return rowView;
    }

    private void showAlert(final Room room) {


        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.getInstance(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete");
        builder.setMessage("Configuration related to this room will get lost. Are you sure want to delete this room?");

        String positiveText = mContext.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        deleteRoomFromServer(room);

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

    private void deleteRoomFromServer(final Room room) {

        final ProgressDialog progressDialog=new ProgressDialog(HomeActivity.getInstance(),R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(room.getRoomId()));

        Call<JsonObject> call = Constants.myInterface.deleteRoom(id);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                try{

                    JSONObject jsonObject=new JSONObject(response.body().toString());
                    boolean error = jsonObject.getBoolean("error");

                    if(!error) {

                     Toast.makeText(mContext, "Deleted successfully", Toast.LENGTH_SHORT).show();

                        DBHandler dbHandler= new DBHandler(mContext);
                        dbHandler.deleteRoom(room.getRoomId());

                        HomeActivity.getInstance().finish();
                        mContext.startActivity(new Intent(mContext, HomeActivity.class));


                    }else{

                        Toast.makeText(mContext, "Failed,Please try again later...", Toast.LENGTH_SHORT).show();

                    }
                    }catch (Exception e){
                    
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(mContext, "Something went wrong, Please try again..", Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void addNewRoomToServer(final Room room) {

        final ProgressDialog progressDialog=new ProgressDialog(HomeActivity.getInstance(),R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        Call<JsonObject> call = Constants.myInterface.addNewRoom(room);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                try{

                    JSONObject jsonObject=new JSONObject(response.body().toString());
                    boolean error = jsonObject.getBoolean("error");

                    if(!error){

                        JSONObject roomObj= jsonObject.getJSONObject("room");

                        room.setRoomId(roomObj.getInt("roomId"));

                        DBHandler dbHandler = new DBHandler(mContext);
                        dbHandler.updateRoomCaption(room.getRoomName(),room.getRoomId());

                        Toast.makeText(mContext, "Updated successfully", Toast.LENGTH_SHORT).show();
                        HomeActivity.getInstance().finish();

                        mContext.startActivity(new Intent(mContext, HomeActivity.class));

                    }else{

                        Toast.makeText(mContext, "Failed, Try again...", Toast.LENGTH_SHORT).show();
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(mContext, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();
                Log.e("Fail", "..");
                Log.e("Json add new room", " .. " + t.getMessage());

            }
        });


    }




}
