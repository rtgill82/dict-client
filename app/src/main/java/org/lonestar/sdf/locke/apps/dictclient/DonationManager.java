/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import static com.android.billingclient.api.BillingClient.BillingResponse.*;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

class DonationManager implements PurchasesUpdatedListener {
    private static final String TAG = "DONATIONMANAGER";
    private static DonationManager sInstance;

    final private Context mContext;
    final private SharedPreferences mPreferences;

    private BillingClient mBillingClient;
    private boolean mDonated;
    private boolean mServiceConnected;
    private int mRetries;

    private DonationFlowListener mDonationFlowListener;

    static public void initialize(Context context) {
        if (sInstance == null)
          sInstance = new DonationManager(context);
    }

    static public DonationManager getInstance() {
        if (sInstance == null)
          throw new RuntimeException(DonationManager.class.getSimpleName()
                                     + " not initialized.");

        return sInstance;
    }

    private DonationManager(final Context context) {
        mContext = context;
        final String keyDonated = context.getString(R.string.pref_key_donated);
        boolean valueDonated = context.getResources()
                                      .getBoolean(R.bool.pref_value_donated);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(
                            SharedPreferences preferences,
                            String key
                    ) {
                        if (key.equals(keyDonated))
                          mDonated = preferences.getBoolean(key, false);
                    }
                }
        );
        mDonated = mPreferences.getBoolean(keyDonated, valueDonated);
    }

    public void makeDonation(final String sku, DonationFlowListener listener) {
        mDonationFlowListener = listener;
        mBillingClient = buildBillingClient();

        queryPurchases(
            new PurchaseHistoryResponseListener() {
                @Override
                public void onPurchaseHistoryResponse(
                  @BillingResponse int code,
                  List<Purchase> purchases
                ) {
                    handleBillingFlowResponse(code);
                    if (code == OK) {
                        mBillingClient = buildBillingClient();
                        consumeAndPurchase(sku, purchases);
                    }
                }
            }
        );
    }

    public void checkDonations(final Context context,
                               final OnHasDonatedListener listener) {
        if (mDonated) {
            listener.hasDonated(true);
            return;
        }

        mBillingClient = buildBillingClient();
        queryPurchases(
            new PurchaseHistoryResponseListener() {
                @Override
                public void onPurchaseHistoryResponse(
                  @BillingResponse int code,
                  List<Purchase> purchases
                ) {
                    handleBillingFlowResponse(code);
                    if (code == OK) {
                        if (purchases != null && purchases.size() > 0)
                          applyDonation(context, true);
                    }
                    listener.hasDonated(mDonated);
                }
            });
    }

    private void queryPurchases(final PurchaseHistoryResponseListener l) {
        executeServiceRequest(
            new Runnable() {
                public void run() {
                    mBillingClient.queryPurchaseHistoryAsync(INAPP, l);
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

                    mBillingClient.launchBillingFlow(
                      mDonationFlowListener.getActivity(),
                      builder.build()
                    );
                }
            }
        );
    }

    private void consumePurchase(final Purchase purchase,
                                 final boolean repurchase) {
        executeServiceRequest(
            new Runnable() {
                public void run() {
                    mBillingClient.consumeAsync(purchase.getPurchaseToken(),
                        new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(
                              @BillingResponse int code,
                              String token
                            ) {
                                handleBillingFlowResponse(code);
                                if ((code == OK || code == ITEM_NOT_OWNED)
                                      && repurchase) {
                                    mBillingClient = buildBillingClient();
                                    makePurchase (purchase.getSku ());
                                }
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
        mDonated = donated;
        mPreferences.edit()
          .putBoolean(context.getString(R.string.pref_key_donated), donated)
          .apply();
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
            if (mDonationFlowListener != null)
              mDonationFlowListener.onItemUnavailable();
            Log.e(TAG, "Item Unavailable");
            break;

          case SERVICE_UNAVAILABLE:
            if (mDonationFlowListener != null)
              mDonationFlowListener.onServiceUnavailable();
            break;

          case ERROR:
            if (mDonationFlowListener != null)
              mDonationFlowListener.onError();
            Log.w(TAG, "Unexpected error");
            break;

          case BILLING_UNAVAILABLE:
            if (mDonationFlowListener != null)
              mDonationFlowListener.onBillingUnavailable();
            Log.w(TAG, "Billing API unavailable");
            break;

          case DEVELOPER_ERROR:
            Log.e(TAG, "Invalid API call");
            throw new RuntimeException("Invalid API call");

          default:
            Log.i(TAG, "launchBillingFlow() Result: " + code);
        }
        endConnection();
    }

    private void startServiceConnection(final Runnable runnable) {
        mBillingClient.startConnection(
            new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingResponse int code) {
                  switch (code) {
                    case OK:
                      mServiceConnected = true;
                      Log.i(TAG, "Service connected");
                      if (runnable != null)
                        runnable.run();
                      break;

                    case SERVICE_DISCONNECTED:
                      if (mRetries >= 3) {
                          Log.i(TAG, "Service disconnected, giving up");
                          mRetries = 0;
                          if (mDonationFlowListener != null)
                            mDonationFlowListener.onServiceUnavailable();
                          break;
                      }
                      Log.i(TAG, "Service disconnected, retrying");
                      mRetries += 1;
                      mBillingClient.startConnection(this);
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
                    mServiceConnected = false;
                }
            });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (mServiceConnected)
          runnable.run();
        else
          startServiceConnection(runnable);
    }

    private void endConnection() {
        mServiceConnected = false;
        mBillingClient.endConnection();
    }

    @Override
    public void onPurchasesUpdated(int code, List<Purchase> purchases) {
        handleBillingFlowResponse(code);
        if (code == OK) {
            Log.i(TAG, "Purchases updated");
            applyDonation(mContext, true);
            if (mDonationFlowListener != null)
              mDonationFlowListener.onPurchasesUpdated();
        }

    }

    private BillingClient buildBillingClient() {
        return BillingClient.newBuilder(mContext)
                            .setListener(this)
                            .build();
    }

    public interface DonationFlowListener {
        Activity getActivity();
        void onPurchasesUpdated();
        void onItemUnavailable();
        void onServiceUnavailable();
        void onError();
        void onBillingUnavailable();
    }

    public interface OnHasDonatedListener {
        void hasDonated(boolean donated);
    }
}
