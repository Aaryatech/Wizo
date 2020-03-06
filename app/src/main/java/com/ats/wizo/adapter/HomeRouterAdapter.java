package com.ats.wizo.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ats.wizo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxadmin on 11/1/18.
 */

public class HomeRouterAdapter extends ArrayAdapter<ScanResult> {
    private List<ScanResult> routersList;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView tvHomeRouter;
    }

    public HomeRouterAdapter(@NonNull Context context,List<ScanResult>  routersList) {
        super(context, R.layout.router_list_adapter, routersList);
        this.mContext = context;
        this.routersList = routersList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder; // view lookup cache stored in tag
        ScanResult scanResult=routersList.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.router_list_adapter, parent, false);
            viewHolder.tvHomeRouter = convertView.findViewById(R.id.tvHomeRouter);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvHomeRouter.setText(scanResult.SSID);

        return convertView;
    }
}
