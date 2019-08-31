package org.lonestar.sdf.locke.apps.dictclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

class CustomListPreference extends ListPreference {
    private final int mPad;

    CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = context.getResources();
        mPad = (int) resources.getDimension(R.dimen.default_margins);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        int itemStyle = getAlertDialogStyle(getContext());
        builder.setAdapter(
            new Adapter(this.getContext(), itemStyle, getEntries()),
            null
        );
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

    private class Adapter extends ArrayAdapter<CharSequence> {
        public Adapter(Context context, int resource, CharSequence[] objects) {
            super(context, resource, objects);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView view = (CheckedTextView)
              super.getView(position, convertView, parent);
            view.setText(styleText(position, getItem(position)));
            view.setPadding(mPad, mPad, mPad, mPad);
            return view;
        }
    }
}
