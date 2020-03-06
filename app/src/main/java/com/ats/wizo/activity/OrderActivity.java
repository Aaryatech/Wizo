package com.ats.wizo.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ats.wizo.R;
import com.ats.wizo.constant.Constants;
import com.ats.wizo.model.Order;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {


    private EditText edUName,edMob,edMob2,edRemarks;
    private Button btnOrderNow;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);


        edUName =findViewById(R.id.edUName);
        edMob =findViewById(R.id.edMob);
        edMob2 =findViewById(R.id.edMob2);
        edRemarks =findViewById(R.id.edRemarks);

        btnOrderNow =findViewById(R.id.btnOrderNow);

        btnOrderNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=edUName.getText().toString();
                String mob=edMob.getText().toString();
                String mob2=edMob2.getText().toString();
                String remarks=edRemarks.getText().toString();

                boolean isValid =true;
                if(name.equalsIgnoreCase("")){
                    isValid=false;
                    edUName.setError("Please Enter Name");
                    edUName.requestFocus();
                }else if(mob.equalsIgnoreCase("")){
                    isValid=false;
                    edMob.setError("Please Enter Mobile No");
                    edMob.requestFocus();
                }else if(edMob.getText().toString().length()!=10){

                    isValid=false;
                    edMob.setError("Please Enter Valid Mobile No");
                    edMob.requestFocus();
                }



                if(isValid){

                    Order order =new Order();

                    order.setMobile1(mob);
                    order.setName(name);
                    order.setMobile2(mob2);
                    order.setRemark(remarks);


                    placeOrder(order);

                }

            }
        });


    }

    private void placeOrder(Order order) {

        progressDialog = new ProgressDialog(OrderActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Placing Your Order");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();


        Call<JsonObject> call = Constants.myInterface.newOrder(order);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                
                
                try{

                    JSONObject jsonObject=new JSONObject(response.body().toString());
                    
                    if(! jsonObject.getBoolean("error")){
                        Toast.makeText(OrderActivity.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else{

                        Toast.makeText(OrderActivity.this, "Failed, Try Again", Toast.LENGTH_SHORT).show();

                    }
                    
                    
                    
                }catch (Exception e){
                    e.printStackTrace();
                }
                
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                Toast.makeText(OrderActivity.this, "Failed, Try Again", Toast.LENGTH_SHORT).show();
            }
        });





    }
}
