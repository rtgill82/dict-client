/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.lonestar.sdf.locke.apps.dictclient.DictClient.CHANNEL;
import static org.lonestar.sdf.locke.apps.dictclient.DonationManager.OnHasDonatedListener;

public class DonateNotificationService extends Service {
    public static final String DONATE_ACTION = "DONATE_ACTION";

    public static void start(final Context context) {
        startService(context);
        DonationManager.getInstance().checkDonations(context,
            new OnHasDonatedListener() {
                public void hasDonated(boolean donated) {
                    if (!donated)
                      startService(context);
                }
            });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean donate = intent.getBooleanExtra(DONATE_ACTION, false);

        closeNotification();
        if (donate)
          showDonateDialog();
        else
          setDonatedPreference();

        stopSelf();
        return START_STICKY;
    }

    private static void startService(Context context) {
        PendingIntent donateIntent = buildIntent(context, true);
        PendingIntent passIntent = buildIntent(context, false);

        NotificationCompat.BigTextStyle bigTextStyle =
            new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(
            context.getString(R.string.dialog_donate_title)
        );
        bigTextStyle.bigText(
            context.getString(R.string.dialog_donate_text)
        );

        NotificationManager notificationManager = (NotificationManager)
          context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL,
                context.getString(R.string.app_name),
                IMPORTANCE_DEFAULT
              );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL)
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setColor(Color.CYAN)
                .addAction(0,
                    context.getString(R.string.notification_donate_donate),
                    donateIntent
                )
                .addAction(0,
                    context.getString(R.string.notification_donate_no_thanks),
                    passIntent
                );

        notificationManager.notify(0, builder.build());
    }

    private static PendingIntent buildIntent(Context context, boolean donate) {
        Intent intent = new Intent(context, DonateNotificationService.class);
        intent.putExtra(DONATE_ACTION, donate);
        PendingIntent pendingIntent = PendingIntent.getService(
            context, (int) System.currentTimeMillis(), intent, FLAG_ONE_SHOT
        );
        return pendingIntent;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void closeNotification() {
        NotificationManager notificationManager =
          (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void showDonateDialog() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(intent);
        intent = new Intent(this, MainActivity.class);
        intent.putExtra(DONATE_ACTION, true);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setDonatedPreference() {
        SharedPreferences.Editor editor =
          PreferenceManager
              .getDefaultSharedPreferences(getApplicationContext())
              .edit();
        editor.putBoolean(getString(R.string.pref_key_donated), true);
        editor.apply();
    }
}
