package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Room;
import com.ats.wizo.sqlite.DBHandler;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewRoomActivity extends AppCompatActivity {


    EditText etRoomName;
    RadioGroup rgIcon;
    Button btnAddNewRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_room);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView ivBack = findViewById(R.id.ivBack);

        etRoomName = findViewById(R.id.etRoomName);
        rgIcon = findViewById(R.id.rgIcon);
        btnAddNewRoom = findViewById(R.id.btnAddNewRoom);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

        btnAddNewRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String roomName = etRoomName.getText().toString();

                if (roomName.equals(null) || roomName.equals("")) {

                    etRoomName.setError("Please Enter Room Name");
                } else {

                    Room room = new Room();
                    room.setRoomName(roomName);
                    room.setRoomIsUsed(1);
                    room.setUserId(Integer.valueOf(Constants.userId));

                    // get selected radio button from radioGroup
                    int selectedId = rgIcon.getCheckedRadioButtonId();

                    // find the radiobutton by returned id
                    RadioButton radioButton = (RadioButton) findViewById(selectedId);
                    String strRoom = radioButton.getText().toString();

                    switch (strRoom) {

                        case "Living Room":
                            System.out.println("Living Room");
                            room.setRoomIcon("LR");
                            break;

                        case "Bedroom":
                            System.out.println("Bedroom");
                            room.setRoomIcon("B");
                            break;

                        case "Master Bedroom":
                            System.out.println("Master Bedroom");
                            room.setRoomIcon("MB");
                            break;

                        case "Kitchen":
                            System.out.println("Kitchen");
                            room.setRoomIcon("K");
                            break;

                        default:
                            System.out.println("Default: Living Room");
                            room.setRoomIcon("LR");
                            break;

                    }

                    Room returnRoom = addNewRoomToServer(room);


                }

            }
        });


    }

    private Room addNewRoomToServer(final Room room) {

        final ProgressDialog progressDialog=new ProgressDialog(AddNewRoomActivity.this,R.style.MyAlertDialogStyle);
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

                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        dbHandler.addNewRoom(room);

                        Toast.makeText(AddNewRoomActivity.this, "New Room Added Successfully", Toast.LENGTH_SHORT).show();
                        HomeActivity.getInstance().finish();

                        onBackPressed();

                    }else{

                        Toast.makeText(AddNewRoomActivity.this, "Failed, Try again...", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(AddNewRoomActivity.this, "Something went wrong, Please try again...", Toast.LENGTH_SHORT).show();
                Log.e("Fail", "..");
                Log.e("Json add new room", " .. " + t.getMessage());

            }
        });

        return null;

    }

}
