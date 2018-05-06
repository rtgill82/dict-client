package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DonateNotificationService extends Service {
    public static final String DONATE_ACTION = "DONATE_ACTION";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean donate = intent.getBooleanExtra(DONATE_ACTION, false);
        Activity activity =
          ((DictClient) getApplication()).getCurrentActivity();

        closeNotification();
        if (donate)
          showDonateDialog(activity);
        else
          setDonatedPreference();

        stopSelf();
        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void closeNotification() {
        NotificationManager notificationManager =
          (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void showDonateDialog(Activity activity) {
        if (activity != null)
          DonateDialog.show(activity);
        else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(DONATE_ACTION, true);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void setDonatedPreference() {
        SharedPreferences.Editor editor =
          PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.pref_key_donated), true);
        editor.apply();
    }
}
