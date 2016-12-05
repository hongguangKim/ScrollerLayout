package com.example.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.example.scrollerlayout.R;

/** * Created by guolin on 16/1/12. */
public class ScrollerLayout extends RelativeLayout {

	/** * 用于完成滚动操作的实例 */
	private Scroller mScroller;

	/** * 判定为拖动的最小移动像素数 */
	private int mTouchSlop;

	/** * 手机按下时的屏幕坐标 */
	private float mXDown, mYDown;

	/** * 手机当时所处的屏幕坐标 */
	private float mXMove, mYMove;

	/** * 上次触发ACTION_MOVE事件时的屏幕坐标 */
	private float mXLastMove, mYLastMove;

	/** * 界面可滚动的边界 */
	private int leftBorder, rightBorder, topBorder, bottomBorder;

	/** * 滚动模式 */
	private static int SCROLLER_HORIZONTAL_MODE = 0;
	private static int SCROLLER_VERTICAL_MODE = 1;
	private int scrollerMode = SCROLLER_HORIZONTAL_MODE;

	int windowWidth = 0;

	public ScrollerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollerLayout);  
		scrollerMode= a.getInt(R.styleable.ScrollerLayout_scroller_mode, SCROLLER_HORIZONTAL_MODE); 
		a.recycle();//必须回收  
		// 第一步，创建Scroller的实例
		mScroller = new Scroller(context);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		// 获取TouchSlop值
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
		windowWidth = getWindowWidth();
	}

	private int getWindowWidth() {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay().getWidth();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			measureChild(childView, widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);

				LayoutParams linearParams = (LayoutParams) childView.getLayoutParams();
				linearParams.width = windowWidth;
				linearParams.height = childView.getMeasuredHeight();
				childView.setLayoutParams(linearParams);
				if (scrollerMode == SCROLLER_HORIZONTAL_MODE)
					childView.layout(i * childView.getMeasuredWidth(), 0,
							(i + 1) * childView.getMeasuredWidth(),childView.getMeasuredHeight());
				else if (scrollerMode == SCROLLER_VERTICAL_MODE)
					childView.layout(0, i * childView.getMeasuredHeight(),
							childView.getMeasuredWidth(),(i + 1) * childView.getMeasuredHeight());
			}
			
			if (scrollerMode == SCROLLER_HORIZONTAL_MODE) {
				// 初始化左右边界值
				leftBorder = getChildAt(0).getLeft();
				rightBorder = getChildAt(getChildCount() - 1).getRight();
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				topBorder = 0;
				bottomBorder = getChildAt(getChildCount() - 1).getBottom();
			}
			getLayoutParams().height = getChildAt(0).getMeasuredHeight();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (scrollerMode == SCROLLER_HORIZONTAL_MODE) {
				mXDown = ev.getRawX();
				mXLastMove = mXDown;
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				mYDown = ev.getRawY();
				mYLastMove = mYDown;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float diff = 0;
			if (scrollerMode == SCROLLER_HORIZONTAL_MODE) {
				mXMove = ev.getRawX();
				diff = Math.abs(mXMove - mXDown);
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				mYMove = ev.getRawY();
				diff = Math.abs(mYMove - mYDown);
			}
			// 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
			if (diff > mTouchSlop) {
				return true;
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (scrollerMode == SCROLLER_HORIZONTAL_MODE) {
				mXMove = event.getRawX();
				int scrolledX = (int) (mXLastMove - mXMove);
				if (getScrollX() + scrolledX < leftBorder) {
					scrollTo(leftBorder, 0);
					return true;
				} else if (getScrollX() + getWidth() + scrolledX > rightBorder) {
					scrollTo(rightBorder - getWidth(), 0);
					return true;
				}
				scrollBy(scrolledX, 0);
				mXLastMove = mXMove;
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				mYMove = event.getRawY();
				int scrolledY = (int) (mYLastMove - mYMove);
				if (getScrollY() + scrolledY < topBorder) {
					scrollTo(0, topBorder);
					return true;
				} else if (getScrollY() + getHeight() + scrolledY > bottomBorder) {
					scrollTo(0, bottomBorder - getHeight());
					return true;
				}
				scrollBy(0, scrolledY);
				mYLastMove = mYMove;
			}

			break;
		case MotionEvent.ACTION_UP:
			int targetIndex = 0;
			if (scrollerMode == SCROLLER_HORIZONTAL_MODE) {
				// 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
				targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
				int dx = targetIndex * getWidth() - getScrollX();
				// 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
				mScroller.startScroll(getScrollX(), 0, dx, 0);
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				// 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
				targetIndex = (getScrollY() + getHeight() / 2) / getHeight();
				int dx = targetIndex
						* getChildAt(targetIndex).getMeasuredHeight()
						- getScrollY();
				// 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
				mScroller.startScroll(0, getScrollY(), 0, dx);
			}
			LayoutParams linearParams = (LayoutParams) getLayoutParams();
			linearParams.height = getChildAt(targetIndex).getMeasuredHeight();
			setLayoutParams(linearParams);
			invalidate();
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		// 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}
}
