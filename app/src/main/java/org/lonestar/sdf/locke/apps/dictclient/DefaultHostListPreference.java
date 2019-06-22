package org.lonestar.sdf.locke.apps.dictclient;

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

class DefaultHostListPreference extends ListPreference {
    private LayoutInflater mInflater;
    private int mItemStyle;
    private int mPad;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;

    public DefaultHostListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        setAlertDialogStyle(context);
        Resources resources = context.getResources();
        mPad = (int) resources.getDimension(R.dimen.default_margins);
        bindHosts();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mEntries = getEntries();
        mEntryValues = getEntryValues();
        builder.setAdapter(new Adapter(), null);
        super.onPrepareDialogBuilder(builder);
    }

    private void setAlertDialogStyle(Context context) {
        TypedArray a = context.obtainStyledAttributes(
                null,
                R.styleable.AlertDialog,
                R.attr.alertDialogStyle,
                0
        );

        try {
            mItemStyle = a.getResourceId(
                    R.styleable.AlertDialog_singleChoiceItemLayout,
                    android.R.layout.select_dialog_singlechoice
            );
        } finally {
            a.recycle();
        }
    }

    private void bindHosts() {
        HostCursor cursor = (HostCursor)
                DatabaseManager.find(Host.class, "hidden", false);
        CharSequence[] entries = new CharSequence[cursor.getCount()];
        CharSequence[] entryValues = new CharSequence[cursor.getCount()];

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            entries[i] = cursor.getHostName();
            entryValues[i] = Integer.toString(cursor.getId());
            i = i + 1;
            cursor.moveToNext();
        }

        Host defaultHost = DictClient.getDefaultHost();
        setEntries(entries);
        setEntryValues(entryValues);
        setValue(defaultHost.getId().toString());
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
                view.setText(mEntries[position]);
                view.setPadding(mPad, mPad, mPad, mPad);
            }
            return view;
        }
    }
}
