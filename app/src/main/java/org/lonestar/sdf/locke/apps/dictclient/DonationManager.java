/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.android.billingclient.api.BillingClient.BillingResponse.BILLING_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.DEVELOPER_ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponse.ERROR;
import static com.android.billingclient.api.BillingClient.BillingResponse.ITEM_ALREADY_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponse.ITEM_NOT_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponse.ITEM_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.OK;
import static com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_DISCONNECTED;
import static com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.USER_CANCELED;
import static com.android.billingclient.api.BillingClient.Builder;
import static com.android.billingclient.api.BillingClient.SkuType;
import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;

class DonationManager implements PurchasesUpdatedListener
{
  private static final String TAG = "DONATIONMANAGER";
  private static DonationManager instance;

  private Context context;
  private BillingClient billingClient;
  private int retries;
  private boolean serviceConnected;

  private boolean donated;
  private List<Purchase> purchases;
  private SharedPreferences preferences;

  private Activity activity;
  private String sku;

  static public DonationManager initialize (Context context)
  {
    if (instance == null)
      instance = new DonationManager (context);
    return instance;
  }

  static public DonationManager getInstance ()
  {
    if (instance == null)
      throw new RuntimeException (DonationManager.class.getSimpleName ()
                                  + " not initialized.");

    return instance;
  }

  private DonationManager (Context context)
  {
    this.context = context;
    billingClient = new Builder (context).setListener (this).build ();
    preferences = PreferenceManager.getDefaultSharedPreferences (context);
    donated = preferences.getBoolean (
      context.getString (R.string.pref_key_donated),
      context.getResources ()
             .getBoolean (R.bool.pref_value_donated)
    );

    if (!donated)
      checkDonations (context);
  }

  @Override
  public void onPurchasesUpdated (int code, List<Purchase> purchases)
  {
    if (code == OK)
      {
        applyDonation (context, true);
        MessageDialog.show(activity,
            activity.getString (R.string.dialog_title_thank_you),
            activity.getString (R.string.dialog_message_thank_you)
        );
      }
    handleBillingFlowResponse (code);
  }

  public void makeDonation (Activity activity, String sku)
  {
    this.activity = activity;
    this.sku = sku;
    consumeAndPurchase ();
  }

  private List<Purchase> getPurchases ()
  {
    List<Purchase> purchases = null;
    PurchasesResult result = billingClient.queryPurchases (SkuType.INAPP);
    if (result.getResponseCode () == OK)
      purchases = result.getPurchasesList ();
    return purchases;
  }

  private void consumeAndPurchase ()
  {
    final Purchase purchase = findPurchaseBySku (sku);
    if (purchase == null)
      makePurchase ();
    else
      consumePurchase (true);
  }

  private void consumePurchase (final boolean repurchase)
  {
    final Purchase purchase = findPurchaseBySku (sku);
    executeServiceRequest (
      new Runnable ()
      {
        public void run ()
        {
          billingClient.consumeAsync (purchase.getPurchaseToken (),
            new ConsumeResponseListener ()
            {
              @Override
              public void onConsumeResponse (String token,
                                             @BillingResponse int code)
              {
                if ((code == OK || code == ITEM_NOT_OWNED)
                    && repurchase == true)
                  makePurchase ();
              }
            });
        }
      });
  }

  private void makePurchase ()
  {
    executeServiceRequest (
        new Runnable ()
        {
          public void run ()
          {
            BillingFlowParams.Builder builder =
                new BillingFlowParams.Builder ()
                    .setSku (sku)
                    .setType (SkuType.INAPP);

            int code = billingClient.launchBillingFlow (activity, builder.build ());
            handleBillingFlowResponse (code);
          }
        });
  }

  private Purchase findPurchaseBySku (String sku)
  {
    if (purchases == null)
      return null;

    Purchase rv = null;
    for (Purchase purchase : purchases)
      {
        if (purchase.getSku ().equals (sku))
          {
            rv = purchase;
            break;
          }
      }
    return rv;
  }

  private boolean getDonationStatus ()
  {
    return (purchases != null && purchases.size () > 0);
  }

  private void applyDonation (Context context, boolean donated)
  {
    preferences.edit ()
      .putBoolean (context.getString (R.string.pref_key_donated), donated)
      .apply ();
  }

  private void checkDonations (final Context context)
  {
    executeServiceRequest (
      new Runnable ()
      {
        public void run ()
        {
          purchases = getPurchases ();
          donated = getDonationStatus ();
          applyDonation (context, donated);
          endConnection ();

          if (!donated)
            startDonationNotificationService (context);
        }
      });
  }

  private void handleBillingFlowResponse(int code)
  {
    switch (code)
      {
      case USER_CANCELED:
        // Do nothing...
        break;

      case ITEM_ALREADY_OWNED:
        consumeAndPurchase ();
        break;

      case ITEM_UNAVAILABLE:
        ErrorDialog.show (
            activity,
            activity.getString (R.string.dialog_message_unavailable)
        );
        Log.e(TAG, "Item Unavailable");
        break;

      case SERVICE_UNAVAILABLE:
        ErrorDialog.show(
            activity,
            activity.getString (R.string.dialog_message_service_unavailable)
        );
        break;

      case ERROR:
        ErrorDialog.show(
            activity,
            activity.getString (R.string.dialog_message_unexpected_error)
        );
        Log.w (TAG, "Unexpected error");
        break;

      case BILLING_UNAVAILABLE:
        ErrorDialog.show(
            activity,
            activity.getString (R.string.dialog_message_billing_unavailable)
        );
        Log.w(TAG, "Billing API unavailable");
        break;

      case DEVELOPER_ERROR:
        Log.e(TAG, "Invalid API call");
        throw new RuntimeException ("Invalid API call");

      default:
        Log.i(TAG, "launchBillingFlow() Result: " + code);
      }
  }

  private void startServiceConnection (final Runnable runnable)
  {
    billingClient.startConnection (
      new BillingClientStateListener ()
      {
        @Override
        public void onBillingSetupFinished (@BillingResponse int code)
        {
          switch (code)
            {
            case OK:
              serviceConnected = true;
              if (runnable != null)
                runnable.run ();
              break;

            case SERVICE_DISCONNECTED:
              if (retries >= 3)
                {
                  ErrorDialog.show(
                      activity,
                      activity.getString (R.string.dialog_message_service_disconnected)
                  );
                  break;
                }

              retries += 1;
              startServiceConnection (runnable);
              break;

            case BILLING_UNAVAILABLE:
              Log.w(TAG, "Billing API unavailable");
              break;

            default:
              Log.w (TAG, "Unhandled Response Code: " + code);
            }
        }

        @Override
        public void onBillingServiceDisconnected ()
        {
          serviceConnected = false;
        }
      });
  }

  private void executeServiceRequest (Runnable runnable)
  {
    retries = 0;
    if (serviceConnected)
      runnable.run ();
    else
      startServiceConnection (runnable);
  }

  private void endConnection ()
  {
    serviceConnected = false;
    billingClient.endConnection ();
  }

  private PendingIntent createDonationIntent (Context context, boolean donate)
    {
      Intent intent = new Intent (context, DonateNotificationService.class);
      intent.putExtra (DONATE_ACTION, donate);
      PendingIntent pendingIntent = PendingIntent.getService (
          context, (int) System.currentTimeMillis (), intent, FLAG_ONE_SHOT
      );
      return pendingIntent;
    }

  private void startDonationNotificationService (Context context)
    {
      PendingIntent donateIntent = createDonationIntent (context, true);
      PendingIntent passIntent = createDonationIntent (context, false);

      NotificationCompat.BigTextStyle bigTextStyle =
          new NotificationCompat.BigTextStyle ();
      bigTextStyle.setBigContentTitle (
          context.getString (R.string.dialog_donate_title)
      );
      bigTextStyle.bigText (
          context.getString (R.string.dialog_donate_text)
      );

      NotificationCompat.Builder builder =
          new NotificationCompat.Builder (context)
              .setStyle (bigTextStyle)
              .setSmallIcon (R.drawable.ic_launcher)
              .addAction (0,
                  context.getString(R.string.notification_donate_donate),
                  donateIntent
              )
              .addAction (0,
                  context.getString(R.string.notification_donate_no_thanks),
                  passIntent
              );

      NotificationManager notificationManager = (NotificationManager)
          context.getSystemService (NOTIFICATION_SERVICE);
      notificationManager.notify (0, builder.build ());
    }
}
