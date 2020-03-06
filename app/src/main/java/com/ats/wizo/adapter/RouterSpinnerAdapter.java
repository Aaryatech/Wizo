package com.ats.wizo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ats.wizo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIRACLEINFOTAINMENT on 03/03/18.
 */

public class RouterSpinnerAdapter  extends BaseAdapter implements SpinnerAdapter {

    private final Context activity;
    private List<ScanResult> asr;

    public RouterSpinnerAdapter(Context context,List<ScanResult> asr) {
        this.asr=asr;
        activity = context;
    }


    public int getCount()
    {
        return asr.size();
    }

    public Object getItem(int i)
    {
        return asr.get(i);
    }

    public long getItemId(int i)
    {
        return (long)i;
    }




    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=(LayoutInflater) activity.getApplicationContext().getSystemService(  Context.LAYOUT_INFLATER_SERVICE );
        View row=inflater.inflate(R.layout.dropdown_item, parent, false);
        TextView txt=(TextView)row.findViewById(R.id.tvSpItem);


        txt.setText(asr.get(position).SSID);
        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(activity);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(16);
        txt.setText(asr.get(i).SSID);
        txt.setTextColor(Color.parseColor("#FFFFFF"));
        return  txt;
    }

}
