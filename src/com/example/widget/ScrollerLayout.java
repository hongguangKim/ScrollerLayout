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

	/** * ������ɹ���������ʵ�� */
	private Scroller mScroller;

	/** * �ж�Ϊ�϶�����С�ƶ������� */
	private int mTouchSlop;

	/** * �ֻ�����ʱ����Ļ���� */
	private float mXDown, mYDown;

	/** * �ֻ���ʱ��������Ļ���� */
	private float mXMove, mYMove;

	/** * �ϴδ���ACTION_MOVE�¼�ʱ����Ļ���� */
	private float mXLastMove, mYLastMove;

	/** * ����ɹ����ı߽� */
	private int leftBorder, rightBorder, topBorder, bottomBorder;

	/** * ����ģʽ */
	private static int SCROLLER_HORIZONTAL_MODE = 0;
	private static int SCROLLER_VERTICAL_MODE = 1;
	private int scrollerMode = SCROLLER_HORIZONTAL_MODE;

	int windowWidth = 0;

	public ScrollerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollerLayout);  
		scrollerMode= a.getInt(R.styleable.ScrollerLayout_scroller_mode, SCROLLER_HORIZONTAL_MODE); 
		a.recycle();//�������  
		// ��һ��������Scroller��ʵ��
		mScroller = new Scroller(context);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		// ��ȡTouchSlopֵ
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
				// ��ʼ�����ұ߽�ֵ
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
			// ����ָ�϶�ֵ����TouchSlopֵʱ����ΪӦ�ý��й����������ӿؼ����¼�
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
				// ����ָ̧��ʱ�����ݵ�ǰ�Ĺ���ֵ���ж�Ӧ�ù������ĸ��ӿؼ��Ľ���
				targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
				int dx = targetIndex * getWidth() - getScrollX();
				// �ڶ���������startScroll()��������ʼ���������ݲ�ˢ�½���
				mScroller.startScroll(getScrollX(), 0, dx, 0);
			} else if (scrollerMode == SCROLLER_VERTICAL_MODE) {
				// ����ָ̧��ʱ�����ݵ�ǰ�Ĺ���ֵ���ж�Ӧ�ù������ĸ��ӿؼ��Ľ���
				targetIndex = (getScrollY() + getHeight() / 2) / getHeight();
				int dx = targetIndex
						* getChildAt(targetIndex).getMeasuredHeight()
						- getScrollY();
				// �ڶ���������startScroll()��������ʼ���������ݲ�ˢ�½���
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
		// ����������дcomputeScroll()�������������ڲ����ƽ���������߼�
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}
}
