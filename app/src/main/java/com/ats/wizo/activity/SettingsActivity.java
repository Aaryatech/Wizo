package com.ats.wizo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.ats.wizo.R;
import com.ats.wizo.adapter.SettingsAdapter;
import com.ats.wizo.common.Variables;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView lvSettings=findViewById(R.id.lvSettings);
        ImageView  ivBack= findViewById(R.id.ivBack);

        List<String> settingsList=new ArrayList<String>();
        settingsList.add("Add New Room");
        settingsList.add("Add New Mood");

        settingsList.add("Upload Data");
        settingsList.add("Download Data");


        settingsList.add("Sync With Router");
        settingsList.add("My Profile");
        settingsList.add("Help");
        settingsList.add("Logout");

        SettingsAdapter  settingsAdapter=new SettingsAdapter(getApplicationContext(),settingsList,SettingsActivity.this);
        lvSettings.setAdapter(settingsAdapter);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();

    }
}
