/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;

interface DonationFlowCallbacks
{
  Activity getActivity();
  void onPurchasesUpdated();
  void onItemUnavailable();
  void onServiceUnavailable();
  void onError();
  void onBillingUnavailable();
}
