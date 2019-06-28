package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

class DisplayOptionListPreference extends CustomListPreference {
    private CharSequence[] mEntryDescriptions;

    public DisplayOptionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEntryDescriptions = getEntryDescriptions(context, attrs);
    }

    @Override
    protected CharSequence styleText(int position, CharSequence entry) {
        Context context = getContext();
        TextAppearanceSpan large = new TextAppearanceSpan(context,
            android.R.style.TextAppearance_Large);
        TextAppearanceSpan small = new TextAppearanceSpan(context,
            android.R.style.TextAppearance_Small);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        int pos = entry.length();
        builder.append(entry);
        builder.setSpan(large, 0, pos, SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n" + mEntryDescriptions[position]);

        pos += 1;
        builder.setSpan(small, pos, builder.length(), 0);
        return builder;
    }

    private CharSequence[] getEntryDescriptions(Context context,
                                                 AttributeSet attrs) {
        CharSequence[] entryDescriptions;
        TypedArray a = context.obtainStyledAttributes(
            attrs,
            R.styleable.DisplayOptionListPreference,
            0, 0
        );

        entryDescriptions = a.getTextArray(
            R.styleable.DisplayOptionListPreference_entryDescriptions
        );
        a.recycle();
        return entryDescriptions;
    }
}
