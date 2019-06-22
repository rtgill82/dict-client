package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;

class DisplayOptionListPreference extends CustomListPreference {
    private CharSequence[] mEntryDescriptions;

    public DisplayOptionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(
            attrs,
            R.styleable.DisplayOptionListPreference,
            0, 0
        );

        try {
            mEntryDescriptions = a.getTextArray(
                R.styleable.DisplayOptionListPreference_entryDescriptions
            );
        } finally {
            a.recycle();
        }
    }

    @Override
    protected CharSequence styleText(int position, CharSequence entry) {
        String html = entry + "<br><i>" +
          mEntryDescriptions[position] + "</i>";
        return Html.fromHtml(html);
    }
}
