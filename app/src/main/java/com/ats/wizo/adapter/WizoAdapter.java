package com.ats.wizo.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ats.wizo.R;

import java.util.List;

/**
 * Created by maxadmin on 11/1/18.
 */

public class WizoAdapter  extends BaseAdapter {
    private List<ScanResult> routersList;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView tvWizoDevice;
    }

    public WizoAdapter(@NonNull Context context, List<ScanResult>  routersList) {
        Log.e("WizoAdapter ", "Constructor " );

        this.mContext = context;
        this.routersList = routersList;
    }

    @Override
    public int getCount() {
        return routersList.size();
    }

    @Override
    public Object getItem(int position) {
        return routersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.e("inside ", "getView " );

        WizoAdapter.ViewHolder viewHolder; // view lookup cache stored in tag
        ScanResult scanResult=routersList.get(position);


          if (convertView == null) {
              Log.e("WizoAdapter ", "view null " +position);

              viewHolder = new WizoAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.wizo_list_adapter, parent, false);
            viewHolder.tvWizoDevice = convertView.findViewById(R.id.tvWizoDevice);

            convertView.setTag(viewHolder);

          }
          else {
              Log.e("WizoAdapter ", "cached view " +position);

              viewHolder = (WizoAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.tvWizoDevice.setText(scanResult.SSID);

        return convertView;
    }
}
