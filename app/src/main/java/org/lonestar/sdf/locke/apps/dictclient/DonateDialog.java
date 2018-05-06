package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DonateDialog extends DialogFragment
{
  final private static String DONATION1 = "donation1";
  final private static String DONATION2 = "donation2";
  final private static String DONATION3 = "donation3";

  private static DonateDialog instance;
  private DonationFlowCallbacksImpl callbacks =
      new DonationFlowCallbacksImpl ();

  public static void show (Activity activity)
  {
    // Only show one donate dialog at a time.
    if (instance == null)
      {
        instance = new DonateDialog ();
        instance.show (activity.getFragmentManager (),
                       activity.getString (R.string.title_donate));
      }
  }

  @Override
  public Dialog onCreateDialog (Bundle savedInstanceState)
  {
    Dialog dialog = super.onCreateDialog (savedInstanceState);
    dialog.setTitle (R.string.title_donate);
    return dialog;
  }

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState)
  {
    View view = inflater.inflate (R.layout.dialog_donate, container, false);
    setupDonationButton (view, R.id.button_donate1, DONATION1);
    setupDonationButton (view, R.id.button_donate2, DONATION2);
    setupDonationButton (view, R.id.button_donate3, DONATION3);
    setupCancelButton (view);
    return view;
  }

  @Override
  public void onDismiss (DialogInterface dialog)
  {
    super.onDismiss (dialog);
    instance = null;
  }

  private void setupDonationButton (View view, int button_id, final String sku)
  {
    view.findViewById (button_id)
      .setOnClickListener (
        new View.OnClickListener ()
        {
          public void onClick (View view)
          {
            DonationManager.getInstance ()
                           .makeDonation (sku, callbacks);
          }
        });
  }

  private void setupCancelButton (View view)
  {
    view.findViewById (R.id.button_cancel)
      .setOnClickListener (
        new View.OnClickListener ()
        {
          public void onClick (View view)
          {
            dismiss ();
          }
        });
  }

  private class DonationFlowCallbacksImpl implements DonationFlowCallbacks
  {
    @Override
    public Activity getActivity ()
    {
      return DonateDialog.this.getActivity ();
    }

    @Override
    public void onPurchasesUpdated ()
    {
      Activity activity = getActivity ();
      MessageDialog.show(activity,
          activity.getString (R.string.dialog_title_thank_you),
          activity.getString (R.string.dialog_message_thank_you)
      );
    }

    @Override
    public void onItemUnavailable ()
    {
      Activity activity = getActivity ();
      ErrorDialog.show (
          activity,
          activity.getString (R.string.dialog_message_unavailable)
      );
    }

    @Override
    public void onServiceUnavailable ()
    {
      Activity activity = getActivity ();
      ErrorDialog.show(
          activity,
          activity.getString (R.string.dialog_message_service_unavailable)
      );
    }

    @Override
    public void onError ()
    {
      Activity activity = getActivity ();
      ErrorDialog.show(
          activity,
          activity.getString (R.string.dialog_message_unexpected_error)
      );
    }

    @Override
    public void onBillingUnavailable ()
    {
      Activity activity = getActivity ();
      ErrorDialog.show(
          activity,
          activity.getString (R.string.dialog_message_billing_unavailable)
      );
    }
  }
}
