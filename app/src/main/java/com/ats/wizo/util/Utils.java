package com.ats.wizo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.ats.wizo.activity.SelectHomeRouterActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by maxadmin on 12/1/18.
 */

public class Utils {
    public static void displayPromptForEnablingGPS(
            final Activity activity)
    {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Enable Location Service";

        builder.setCancelable(false);

        builder.setMessage(message)
                .setPositiveButton("ENABLE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.dismiss();
                                activity.startActivity(new Intent(action));
                            }
                        });
        builder.create().show();
    }
    @SuppressWarnings("boxing")
    private final static ArrayList<Integer> channelsFrequency = new ArrayList<Integer>(
            Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447,
                    2452, 2457, 2462, 2467, 2472, 2484));

    public static Integer getFrequencyFromChannel(int channel) {
        return channelsFrequency.get(channel);
    }

    public static int getChannelFromFrequency(int frequency) {
        return channelsFrequency.indexOf(Integer.valueOf(frequency));
    }

    public static void displayPromptForDisablingData(final Activity activity) {

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final String message = "Disable Mobile Data";

        builder.setCancelable(false);
        builder.setMessage(message)
                .setPositiveButton("Disable",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.dismiss();
                                activity.startActivity(intent);
                            }
                        });

        builder.create().show();



    }
}
