package org.lonestar.sdf.locke.apps.dictclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

class CustomListPreference extends ListPreference {
    private final LayoutInflater mInflater;
    private final int mItemStyle;
    private final int mPad;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;

    CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        mItemStyle = getAlertDialogStyle(context);
        Resources resources = context.getResources();
        mPad = (int) resources.getDimension(R.dimen.default_margins);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mEntries = getEntries();
        mEntryValues = getEntryValues();
        builder.setAdapter(new Adapter(), null);
        super.onPrepareDialogBuilder(builder);
    }

    @SuppressLint("PrivateResource")
    private int getAlertDialogStyle(Context context) {
        int style;
        TypedArray a = context.obtainStyledAttributes(
            null,
            R.styleable.AlertDialog,
            R.attr.alertDialogStyle,
            0
        );

        try {
            style = a.getResourceId(
                R.styleable.AlertDialog_singleChoiceItemLayout,
                android.R.layout.select_dialog_singlechoice
            );
        } finally {
            a.recycle();
        }
        return style;
    }

    CharSequence styleText(int position, CharSequence entry) {
        return entry;
    }

    private class Adapter extends BaseAdapter {
        public int getCount() {
            if (mEntries != null)
              return mEntries.length;
            return 0;
        }

        public Object getItem(int position) {
            return mEntryValues[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView view = (CheckedTextView) convertView;
            if (view ==  null) {
                view = (CheckedTextView)
                  mInflater.inflate(mItemStyle, parent, false);
                view.setText(styleText(position, mEntries[position]));
                view.setPadding(mPad, mPad, mPad, mPad);
            }
            return view;
        }
    }
}
