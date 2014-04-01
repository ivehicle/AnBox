/*
 * AnBox, and an Android Blackbox application for the have-not-so-much-money's
 * Copyright (C) 2010 Yoonsoo Kim, Heekuk Lee, Heejin Sohn
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Some part of this source is quoted from AOSP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivehicle.AnBox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

class VideoPreview extends SurfaceView {
	/*
    private float mAspectRatio;
    private int mHorizontalTileSize = 1;
    private int mVerticalTileSize = 1;
    */

    /**
     * Setting the aspect ratio to this value means to not enforce an aspect ratio.
     */
    public static float DONT_CARE = 0.0f;

    public VideoPreview(Context context) {
        super(context);
    }

    public VideoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
/*
    public void setTileSize(int horizontalTileSize, int verticalTileSize) {
        if ((mHorizontalTileSize != horizontalTileSize)
                || (mVerticalTileSize != verticalTileSize)) {
            mHorizontalTileSize = horizontalTileSize;
            mVerticalTileSize = verticalTileSize;
            requestLayout();
            invalidate();
        }
    }

    public void setAspectRatio(int width, int height) {
        setAspectRatio(((float) width) / ((float) height));
    }

    public void setAspectRatio(float aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
            invalidate();
        }
    }
*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (mAspectRatio != DONT_CARE) {
            int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecSize > 0 && heightSpecSize > 0)
            {
                setMeasuredDimension(widthSpecSize, heightSpecSize);
                return;
            }
//        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
