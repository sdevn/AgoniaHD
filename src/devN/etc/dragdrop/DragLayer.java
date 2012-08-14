/*
 * Copyright (C) 2008 The Android Open Source Project
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

package devN.etc.dragdrop;

import devN.games.agonia.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * A ViewGroup that coordinated dragging across its descendants
 */
public class DragLayer extends FrameLayout implements DragController {

	private static final float DRAG_ALPHA = 191.0f;

    private boolean mDragging = false;
    private boolean mShouldDrop;
    private float mLastMotionX;
    private float mLastMotionY;

    /**
     * The bitmap that is currently being dragged
     */
    private Bitmap mDragBitmap = null;
    private View mOriginator;

    private int mBitmapOffsetX;
    private int mBitmapOffsetY;

    /**
     * X offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetX;

    /**
     * Y offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetY;

    /**
     * Utility rectangle
     */
    private Rect mDragRect = new Rect();

    /**
     * Where the drag originated
     */
    private DragSource mDragSource;

    /**
     * The data associated with the object being dragged
     */
    private Object mDragInfo;

    private final Rect mRect = new Rect();
    private final int[] mDropCoordinates = new int[2];

//    private final Vibrator mVibrator = new Vibrator();

    private DragListener mListener;

    private View mIgnoredDropTarget;

    private RectF mDragRegion;
    private boolean mEnteredRegion;
    private DropTarget mLastDropTarget;

    private final Paint mTrashPaint = new Paint();
    private Paint mDragPaint;

    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attributes set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int srcColor = context.getResources().getColor(R.color.delete_color_filter);
        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));

        // Make estimated paint area in gray
        int snagColor = context.getResources().getColor(R.color.snag_callout_color);
        Paint estimatedPaint = new Paint();
        estimatedPaint.setColor(snagColor);
        estimatedPaint.setStrokeWidth(3);
        estimatedPaint.setAntiAlias(true);

    }

    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {

        if (mListener != null) {
            mListener.onDragStart(v, source, dragInfo, dragAction);
        }

        Rect r = mDragRect;
        r.set(v.getScrollX(), v.getScrollY(), 0, 0);

        offsetDescendantRectToMyCoords(v, r);
        mTouchOffsetX = mLastMotionX - r.left;
        mTouchOffsetY = mLastMotionY - r.top;

        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap viewBitmap = v.getDrawingCache();
        int width = viewBitmap.getWidth();
        int height = viewBitmap.getHeight();


        mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        final Bitmap dragBitmap = mDragBitmap;
        mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
        mBitmapOffsetY = (dragBitmap.getHeight() - height) / 2;

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(GONE);
        }

        mDragPaint = null;
        mDragging = true;
        mShouldDrop = true;
        mOriginator = v;
        mDragSource = source;
        mDragInfo = dragInfo;

//        mVibrator.vibrate(VIBRATE_DURATION);

        mEnteredRegion = false;

        invalidate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging || super.dispatchKeyEvent(event);
    }

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		
		 if (!mDragging || mDragBitmap == null) 
		 {
			 return;
		 }
		 
		// Draw actual icon being dragged
		if (mDragPaint == null)
		{
			mDragPaint = new Paint();
		}
		mDragPaint.setAlpha((int) DRAG_ALPHA);
		canvas.drawBitmap(mDragBitmap, 
		                getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX, 
						getScrollY() + mLastMotionY	- mTouchOffsetY	- mBitmapOffsetY, 
						mDragPaint);

	}

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
            }
            if (mOriginator != null) {
                mOriginator.setVisibility(VISIBLE);
            }
            if (mListener != null) {
                mListener.onDragEnd();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mLastDropTarget = null;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mShouldDrop && drop(x, y)) {
                    mShouldDrop = false;
                }
                endDrag();
                break;
        }

        return mDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:

            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;

            break;
        case MotionEvent.ACTION_MOVE:
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();

            final float touchX = mTouchOffsetX;
            final float touchY = mTouchOffsetY;

            final int offsetX = mBitmapOffsetX;
            final int offsetY = mBitmapOffsetY;

            int left = (int) (scrollX + mLastMotionX - touchX - offsetX);
            int top = (int) (scrollY + mLastMotionY - touchY - offsetY);

            final Bitmap dragBitmap = mDragBitmap;
            final int width = dragBitmap.getWidth();
            final int height = dragBitmap.getHeight();

            final Rect rect = mRect;
            rect.set(left - 1, top - 1, left + width + 1, top + height + 1);

            mLastMotionX = x;
            mLastMotionY = y;

            left = (int) (scrollX + x - touchX - offsetX);
            top = (int) (scrollY + y - touchY - offsetY);

            // Invalidate current icon position
            rect.union(left - 1, top - 1, left + width + 1, top + height + 1);

            final int[] coordinates = mDropCoordinates;
            DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                    dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                    }
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            }

            invalidate(rect);

            mLastDropTarget = dropTarget;

            if (mDragRegion != null) {
                final RectF region = mDragRegion;
                final boolean inRegion = region.contains(ev.getRawX(), ev.getRawY());
                if (!mEnteredRegion && inRegion) {
                    mDragPaint = mTrashPaint;
                    mEnteredRegion = true;
                } else if (mEnteredRegion && !inRegion) {
                    mDragPaint = null;
                    mEnteredRegion = false;
                }
            }

            break;
        case MotionEvent.ACTION_UP:
            if (mShouldDrop) {
                drop(x, y);
                mShouldDrop = false;
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            endDrag();
        }

        return true;
    }

    private boolean drop(float x, float y) {
        invalidate();

        final int[] coordinates = mDropCoordinates;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        if (dropTarget != null) {
            dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
            if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo)) {
                dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                mDragSource.onDropCompleted((View) dropTarget, true);
                return true;
            } else {
                mDragSource.onDropCompleted((View) dropTarget, false);
                return true;
            }
        }
//        Log.d(TAG, "cant find dropTarget!");
        return false;
    }

    DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        return findDropTarget(this, x, y, dropCoordinates);
    }

    private DropTarget findDropTarget(ViewGroup container, int x, int y, int[] dropCoordinates) {
        final Rect r = mDragRect;
        final int count = container.getChildCount();
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();
        final View ignoredDropTarget = mIgnoredDropTarget;

        for (int i = count - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() == VISIBLE && child != ignoredDropTarget) {
                child.getHitRect(r);
                if (r.contains(scrolledX, scrolledY)) {
                    DropTarget target = null;
                    if (child instanceof ViewGroup) {
                        x = scrolledX - child.getLeft();
                        y = scrolledY - child.getTop();
//                        Log.d(TAG, "searching in container i = " + i);
                        target = findDropTarget((ViewGroup) child, x, y, dropCoordinates);
                    }
                    if (target == null) {
                        if (child instanceof DropTarget) {
                            // Only consider this child if they will accept
                            DropTarget childTarget = (DropTarget) child;
                            if (childTarget.acceptDrop(mDragSource, x, y, 0, 0, mDragInfo)) {
                                dropCoordinates[0] = x;
                                dropCoordinates[1] = y;
//                                Log.d(TAG, "Chilstarget accept drops " + child.getContentDescription());
                                return (DropTarget) child;
                            } else {
//                            	Log.d(TAG, "child is not accept drops");
                                return null;
                            }
                        }
//                        Log.d(TAG, "target is null, but child is not a drop target " + child.getContentDescription());
                    } else {
//                    	Log.d(TAG, "target = " + target.toString());
                        return target;
                    }
                }
            }
        }

        return null;
    }

    public void setDragListener(DragListener l) {
        mListener = l;
    }

    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Specifies the view that must be ignored when looking for a drop target.
     *
     * @param view The view that will not be taken into account while looking
     *        for a drop target.
     */
    void setIgnoredDropTarget(View view) {
        mIgnoredDropTarget = view;
    }

    /**
     * Specifies the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDragRegion = region;
    }
}
