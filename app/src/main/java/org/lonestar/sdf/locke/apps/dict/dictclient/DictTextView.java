/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class DictTextView extends TextView
{
  private static final float MIN_TEXT_SIZE = 8.0f;
  private static final float MAX_TEXT_SIZE = 60.0f;

  private ScaleGestureDetector scaleGesture;
  private float textSize;

  public DictTextView(Context context, AttributeSet attrs)
  {
    super (context, attrs);
    setHorizontallyScrolling (true);
    addTextChangedListener (createTextWatcher ());
    scaleGesture = createScaleGestureDetector ();
  }

  @Override
  public void onFinishInflate ()
  {
    super.onFinishInflate ();

    // Save original text size
    textSize = getTextSize ();
  }

  @Override
  public boolean onTouchEvent (MotionEvent event)
  {
    scaleGesture.onTouchEvent (event);
    return super.onTouchEvent (event);
  }

  private TextWatcher createTextWatcher ()
  {
    return new TextWatcher () {
      public void afterTextChanged (Editable s)
      {
        setTextSize (TypedValue.COMPLEX_UNIT_PX, textSize);
        scrollTo (0, 0);
      }

      public void beforeTextChanged (CharSequence s, int start, int count,
                                     int after)
      {
      }

      public void onTextChanged (CharSequence s, int start, int before,
                                 int count)
      {
      }
    };
  }

  private ScaleGestureDetector createScaleGestureDetector ()
  {
    return new ScaleGestureDetector (this.getContext (),
        new ScaleGestureDetector.SimpleOnScaleGestureListener () {
          @Override
          public void onScaleEnd (ScaleGestureDetector detector)
          {
            float newSize = getTextSize () * detector.getScaleFactor ();
            if (newSize >= MIN_TEXT_SIZE && newSize <= MAX_TEXT_SIZE)
              setTextSize (newSize);
          }
        });
  }
}
