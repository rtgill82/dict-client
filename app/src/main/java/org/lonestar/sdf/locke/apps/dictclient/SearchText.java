package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;

class SearchText extends AppCompatEditText {
    private boolean mDoubleTap = false;
    private GestureDetector mGestureDetector;

    public SearchText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGestureDetector();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
          return true;

        if (event.getAction() == MotionEvent.ACTION_UP && mDoubleTap) {
            mDoubleTap = false;
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(this.getContext(),
          new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                mDoubleTap = true;
                SearchText.this.selectAll();
                return true;
            }
          });
    }
}
