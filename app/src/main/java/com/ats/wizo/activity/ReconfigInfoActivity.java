package com.ats.wizo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ats.wizo.R;

public class ReconfigInfoActivity extends AppCompatActivity {

    boolean isContinue=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconfig_info);

        final TextView tvLabel= findViewById(R.id.tvLabel);
        Button btnBack= findViewById(R.id.btnBack);
        Button btnContinue= findViewById(R.id.btnContinue);

        if(!isContinue){
        //    tvLabel.setText(R.string.info);
        }else {
            tvLabel.setText(R.string.steps);
        }


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(isContinue){



                        Intent intent = new Intent(getApplicationContext(), SelectExistingWizoActivity.class);
                        startActivity(intent);
                        finish();

                }else{

                    tvLabel.setText(R.string.steps);
                    isContinue=true;

                }



            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



    }




}
