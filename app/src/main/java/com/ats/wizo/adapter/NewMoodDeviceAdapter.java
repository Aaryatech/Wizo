package com.ats.wizo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ats.wizo.R;
import com.ats.wizo.activity.AddNewMoodActivity;
import com.ats.wizo.model.MoodDevice;

import java.util.List;

import static com.ats.wizo.activity.AddNewDeviceToMoodActivity.staticNewSelectedDeviceList;

public class NewMoodDeviceAdapter extends ArrayAdapter<MoodDevice> {

    private List<MoodDevice> list;
    public Activity activity;

    public Context context;


    // VH

    public static class ViewHolder {
        TextView tvCaption;
    }




    public NewMoodDeviceAdapter(Context context, List<MoodDevice> data, Activity activity) {
        super(context, R.layout.device_list_adapter, data);
        this.activity = activity;
        this.context = context;
        this.list = data;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MoodDevice getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        boolean isHeader = list.get(i).isHeader();
        MoodDevice moodDevice=list.get(i);

        System.out.println("getView " + i + " " + view + " isHeader = " + isHeader);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        holder = new ViewHolder();


        if(isHeader){
            view = inflater.inflate(R.layout.header_item, null);
            holder.tvCaption = view.findViewById(R.id.tvHeader);
            holder.tvCaption.setText(moodDevice.getRoomName());


        }else{
            view = inflater.inflate(R.layout.adapter_mood_device_list, null);
            holder.tvCaption = view.findViewById(R.id.tvDeviceName);
            holder.tvCaption.setText(moodDevice.getDevCaption());

            if(moodDevice.isSelected()){

                int color = Color.parseColor("#25A3DE");

                holder.tvCaption.setBackgroundColor( color);
            }

            final ViewHolder finalHolder = holder;
            holder.tvCaption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(list.get(i).isSelected()){
                        list.get(i).setSelected(false);
                        finalHolder.tvCaption.setBackgroundColor( Color.TRANSPARENT );
                        staticNewSelectedDeviceList.remove(list.get(i));

                    }else {

                        int color = Color.parseColor("#25A3DE");

                        finalHolder.tvCaption.setBackgroundColor( color );
                        staticNewSelectedDeviceList.add(list.get(i));

                        list.get(i).setSelected(true);
                    }

                }
            });


        }


        view.setTag(holder);


        return view;
    }


}
