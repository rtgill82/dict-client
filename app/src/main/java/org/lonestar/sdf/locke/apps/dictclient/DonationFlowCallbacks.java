package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;

interface DonationFlowCallbacks
{
  Activity getActivity ();
  void onPurchasesUpdated ();
  void onItemUnavailable ();
  void onServiceUnavailable ();
  void onError ();
  void onBillingUnavailable ();
}
