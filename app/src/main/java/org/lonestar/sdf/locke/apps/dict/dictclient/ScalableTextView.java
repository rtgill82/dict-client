/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class ScalableTextView extends TextView
{
  private static final float MIN_TEXT_SIZE = 8.0f;
  private static final float MAX_TEXT_SIZE = 60.0f;

  private ScaleGestureDetector scaleGesture;

  public ScalableTextView (Context context, AttributeSet attrs)
  {
    super (context, attrs);
    setHorizontallyScrolling (true);
    scaleGesture = createScaleGestureDetector ();
  }

  @Override
  public boolean onTouchEvent (MotionEvent event)
  {
    scaleGesture.onTouchEvent (event);
    return super.onTouchEvent (event);
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
