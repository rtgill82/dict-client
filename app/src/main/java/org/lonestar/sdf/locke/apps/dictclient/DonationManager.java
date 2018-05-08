/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

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
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.android.billingclient.api.BillingClient.BillingResponse.*;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;
import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;

class DonationManager implements PurchasesUpdatedListener {
    private static final String TAG = "DONATIONMANAGER";
    private static DonationManager instance;

    private Context context;
    private SharedPreferences preferences;
    private BillingClient billingClient;
    private boolean donated;
    private boolean serviceConnected;
    private int retries;

    private DonationFlowCallbacks donationFlowCallbacks;

    static public DonationManager initialize(Context context) {
        if (instance == null)
          instance = new DonationManager(context);
        return instance;
    }

    static public DonationManager getInstance() {
        if (instance == null)
          throw new RuntimeException(DonationManager.class.getSimpleName()
                                     + " not initialized.");

        return instance;
    }

    private DonationManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        donated = preferences.getBoolean(
            context.getString(R.string.pref_key_donated),
            context.getResources().getBoolean(R.bool.pref_value_donated)
        );
        checkDonations(context);
    }

    public void makeDonation(final String sku, DonationFlowCallbacks listener) {
        this.donationFlowCallbacks = listener;
        this.billingClient = buildBillingClient();

        queryPurchases(
            new PurchaseHistoryResponseListener() {
                @Override
                public void onPurchaseHistoryResponse(
                    @BillingResponse int code,
                    List<Purchase> purchases)
                {
                    if (code == OK)
                      consumeAndPurchase(sku, purchases);
                    else
                      handleBillingFlowResponse(code);
                }
            }
        );
    }

    private void queryPurchases(final PurchaseHistoryResponseListener l) {
        executeServiceRequest(
            new Runnable() {
                public void run() {
                    billingClient.queryPurchaseHistoryAsync(INAPP, l);
                }
            }
        );
    }

    private void consumeAndPurchase(String sku, List<Purchase> purchases) {
        Purchase purchase = findPurchaseBySku(sku, purchases);
        if (purchase == null)
          makePurchase(sku);
        else
          consumePurchase(purchase, true);
    }

    private void makePurchase(final String sku) {
        executeServiceRequest(
            new Runnable() {
                public void run() {
                    BillingFlowParams.Builder builder =
                        BillingFlowParams.newBuilder()
                            .setSku(sku)
                            .setType(INAPP);

                    int code = billingClient.launchBillingFlow(
                        donationFlowCallbacks.getActivity(),
                        builder.build()
                    );
                    handleBillingFlowResponse(code);
                }
            }
        );
    }

    private void consumePurchase(final Purchase purchase,
                                 final boolean repurchase) {
        executeServiceRequest(
            new Runnable() {
                public void run() {
                    billingClient.consumeAsync(purchase.getPurchaseToken(),
                        new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(
                                @BillingResponse int code,
                                String token
                            ) {
                                Log.d(TAG, "consume response = " + code);
                                if ((code == OK || code == ITEM_NOT_OWNED)
                                    && repurchase)
                                  makePurchase(purchase.getSku());
                            }
                        });
                }
            });
    }

    private Purchase findPurchaseBySku(String sku, List<Purchase> purchases) {
        if (purchases == null)
          return null;

        Purchase rv = null;
        for (Purchase purchase : purchases) {
            if (purchase.getSku().equals(sku)) {
                rv = purchase;
                break;
            }
        }
        return rv;
    }

    private void applyDonation(Context context, boolean donated) {
        this.donated = donated;
        preferences.edit()
          .putBoolean(context.getString(R.string.pref_key_donated), donated)
          .apply();
    }

    private void checkDonations(final Context context) {
        if (!donated) {
            billingClient = buildBillingClient();
            queryPurchases(
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(
                        @BillingResponse int code,
                        List<Purchase> purchases
                    ) {
                        if (code != OK) {
                            handleBillingFlowResponse(code);
                        } else {
                            if (purchases != null && purchases.size() > 0)
                              applyDonation(context, true);
                            if (!donated)
                              startDonationNotificationService(context);
                            endConnection();
                        }
                    }
                });
        }
    }

    private void handleBillingFlowResponse(int code) {
        switch (code) {
          case USER_CANCELED:
            // Do nothing...
            break;

          case ITEM_ALREADY_OWNED:
            Log.w(TAG, "Item already owned.");
            break;

          case ITEM_UNAVAILABLE:
            if (donationFlowCallbacks != null)
              donationFlowCallbacks.onItemUnavailable();
            Log.e(TAG, "Item Unavailable");
            break;

          case SERVICE_UNAVAILABLE:
            if (donationFlowCallbacks != null)
              donationFlowCallbacks.onServiceUnavailable();
            break;

          case ERROR:
            if (donationFlowCallbacks != null)
              donationFlowCallbacks.onError();
            Log.w(TAG, "Unexpected error");
            break;

          case BILLING_UNAVAILABLE:
            if (donationFlowCallbacks != null)
              donationFlowCallbacks.onBillingUnavailable();
            Log.w(TAG, "Billing API unavailable");
            break;

          case DEVELOPER_ERROR:
            Log.e(TAG, "Invalid API call");
            throw new RuntimeException("Invalid API call");

          default:
            Log.i(TAG, "launchBillingFlow() Result: " + code);
        }

        if (code !=  ITEM_ALREADY_OWNED) {
            endConnection();
            donationFlowCallbacks = null;
        }
    }

    private void startServiceConnection(final Runnable runnable) {
        billingClient.startConnection(
            new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingResponse int code) {
                  Log.d(TAG, "onBillingSetupFinished()");
                  switch (code) {
                    case OK:
                      serviceConnected = true;
                      Log.i(TAG, "Service connected");
                      if (runnable != null)
                        runnable.run();
                      break;

                    case SERVICE_DISCONNECTED:
                      if (retries >= 3) {
                          Log.i(TAG, "Service disconnected, giving up");
                          retries = 0;
                          if (donationFlowCallbacks != null)
                            donationFlowCallbacks.onServiceUnavailable();
                          break;
                      }
                      Log.i(TAG, "Service disconnected, retrying");
                      retries += 1;
                      billingClient.startConnection(this);
                      break;

                    case BILLING_UNAVAILABLE:
                      Log.w(TAG, "Billing API unavailable");
                      break;

                    default:
                      Log.w(TAG, "Unhandled Response Code: " + code);
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    serviceConnected = false;
                }
            });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (serviceConnected)
          runnable.run();
        else
          startServiceConnection(runnable);
    }

    private void endConnection() {
        serviceConnected = false;
        billingClient.endConnection();
    }

    @Override
    public void onPurchasesUpdated(int code, List<Purchase> purchases) {
        if (code == OK) {
            Log.i(TAG, "Purchases updated");
            applyDonation(context, true);
            if (donationFlowCallbacks != null)
              donationFlowCallbacks.onPurchasesUpdated();
        }
        handleBillingFlowResponse(code);
    }

    private BillingClient buildBillingClient() {
        return BillingClient.newBuilder(context)
                            .setListener(this)
                            .build();
    }

    private PendingIntent createDonationIntent(
        Context context,
        boolean donate
    ) {
        Intent intent = new Intent(context, DonateNotificationService.class);
        intent.putExtra(DONATE_ACTION, donate);
        PendingIntent pendingIntent = PendingIntent.getService(
            context, (int) System.currentTimeMillis(), intent, FLAG_ONE_SHOT
        );
        return pendingIntent;
    }

    private void startDonationNotificationService(Context context) {
        PendingIntent donateIntent = createDonationIntent(context, true);
        PendingIntent passIntent = createDonationIntent(context, false);

        NotificationCompat.BigTextStyle bigTextStyle =
          new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(
            context.getString(R.string.dialog_donate_title)
        );
        bigTextStyle.bigText(
            context.getString(R.string.dialog_donate_text)
        );

        NotificationCompat.Builder builder =
          new NotificationCompat.Builder(context)
              .setStyle(bigTextStyle)
              .setSmallIcon(R.drawable.ic_launcher)
              .addAction(0,
                    context.getString(R.string.notification_donate_donate),
                    donateIntent
              )
              .addAction(0,
                    context.getString(R.string.notification_donate_no_thanks),
                    passIntent
              );

        NotificationManager notificationManager = (NotificationManager)
          context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
