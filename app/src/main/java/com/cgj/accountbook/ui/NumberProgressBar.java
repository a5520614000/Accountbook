package com.cgj.accountbook.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.cgj.accountbook.R;
import com.cgj.accountbook.util.LogUtil;

//主页柱状图view
public class NumberProgressBar extends View {

	private static final String TAG = "NumberProgressBar_Exception";
	public Context mContext;
	private int mMax = 100;
	private int mProgress = 0;
	private int mReachedBarColor;
	private int mUnreachedBarColor;
	private int mTextColor;
	private float mTextSize;
	private float mReachedBarHeight;
	private float mUnreachedBarHeight;
	private final int default_text_color = Color.rgb(66, 145, 241);
	private final int default_reached_color = Color.rgb(66, 145, 241);
	private final int default_unreached_color = Color.rgb(204, 204, 204);
	private final float default_progress_text_offset;
	private final float default_text_size;
	private final float default_reached_bar_height;
	private final float default_unreached_bar_height;
	private static final String INSTANCE_STATE = "saved_instance";
	private static final String INSTANCE_TEXT_COLOR = "text_color";
	private static final String INSTANCE_TEXT_SIZE = "text_size";
	private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
	private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
	private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
	private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
	private static final String INSTANCE_MAX = "max";
	private static final String INSTANCE_PROGRESS = "progress";
	private float mDrawTextWidth;
	private float mDrawTextStart;
	private float mDrawTextEnd;
	private String mCurrentDrawText;
	private Paint mReachedBarPaint;
	private Paint mUnreachedBarPaint;
	private Paint mTextPaint;
	private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);
	private RectF mReachedRectF = new RectF(0, 0, 0, 0);
	private float mOffset;
	private boolean mDrawUnreachedBar = true;
	private boolean mDrawReachedBar = true;

	public NumberProgressBar(Context context) {
		this(context, null);
	}

	public NumberProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.numberProgressBarStyle);
	}

	public NumberProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.mContext = context;
		default_reached_bar_height = dp2px(1.5f);
		default_unreached_bar_height = dp2px(1.0f);
		default_text_size = sp2px(10);
		default_progress_text_offset = dp2px(3.0f);
		final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.NumberProgressBar, defStyleAttr, 0);
		mReachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_reached_color, default_reached_color);
		mUnreachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_unreached_color, default_unreached_color);
		mTextColor = attributes.getColor(R.styleable.NumberProgressBar_progress_text_color, default_text_color);
		mTextSize = attributes.getDimension(R.styleable.NumberProgressBar_progress_text_size, default_text_size);
		mReachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_reached_bar_height, default_reached_bar_height);
		mUnreachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_unreached_bar_height, default_unreached_bar_height);
		mOffset = attributes.getDimension(R.styleable.NumberProgressBar_progress_text_offset, default_progress_text_offset);

		setProgress(attributes.getInt(R.styleable.NumberProgressBar_progress, 0));

		setMax(attributes.getInt(R.styleable.NumberProgressBar_max, 100));
		//回收属性文件
		attributes.recycle();
		//初始化绘图
		initializePainters();
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return (int) mTextSize;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return Math.max((int) mTextSize, Math.max((int) mReachedBarHeight, (int) mUnreachedBarHeight));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
	}

	private int measure(int measureSpec, boolean isWidth) {
		int result;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		int padding = isWidth ? getPaddingLeft() + getPaddingRight()
				: getPaddingTop() + getPaddingBottom();
		if (mode == MeasureSpec.EXACTLY) {
			result = size;
		} else {
			result = isWidth ? getSuggestedMinimumWidth()
					: getSuggestedMinimumHeight();
			result += padding;
			if (mode == MeasureSpec.AT_MOST) {
				if (isWidth) {
					result = Math.max(result, size);
				} else {
					result = Math.min(result, size);
				}
			}
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		calculateDrawRectF();
		if (mDrawReachedBar) {
			canvas.drawRect(mReachedRectF, mReachedBarPaint);
		}
		if (mDrawUnreachedBar) {
			canvas.drawRect(mUnreachedRectF, mUnreachedBarPaint);
		}

		canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextEnd,
				mTextPaint);
	}

	private void initializePainters() {
		mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mReachedBarPaint.setColor(mReachedBarColor);
		mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mUnreachedBarPaint.setColor(mUnreachedBarColor);
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(mTextSize);
	}

	/**
	 * 计算绘图区域
	 */
	private void calculateDrawRectF() {
		mCurrentDrawText = String.format("%d%%", getProgress() * 100 / getMax());
		mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);
		if (getProgress() == 0) {
			mDrawReachedBar = false;
			mDrawTextStart = getPaddingLeft();
		} else {
			mDrawReachedBar = true;
			mReachedRectF.left = getPaddingLeft();
			mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
			mReachedRectF.right = (getWidth() - getPaddingLeft() - getPaddingRight())
					/ (getMax() * 1.0f)
					* getProgress()
					- mOffset
					+ getPaddingLeft();
			mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight
					/ 2.0f;
			mDrawTextStart = (mReachedRectF.right + mOffset);
		}
		mDrawTextEnd = (int) ((getHeight() / 2.0f) - ((mTextPaint.descent() + mTextPaint
				.ascent()) / 2.0f));
		if ((mDrawTextStart + mDrawTextWidth) >= getWidth() - getPaddingRight()) {
			mDrawTextStart = getWidth() - getPaddingRight() - mDrawTextWidth;
			mReachedRectF.right = mDrawTextStart - mOffset;
		}
		float unreachBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
		if (unreachBarStart >= getWidth() - getPaddingRight()) {
			mDrawUnreachedBar = false;
		} else {
			mDrawUnreachedBar = true;
			mUnreachedRectF.left = unreachBarStart;
			mUnreachedRectF.right = getWidth() - getPaddingRight();
			mUnreachedRectF.top = getHeight() / 2.0f + -mUnreachedBarHeight
					/ 2.0f;
			mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight
					/ 2.0f;
		}
	}

	public int getTextColor() {
		return mTextColor;
	}

	public float getProgressTextSize() {
		return mTextSize;
	}

	public int getUnreachedBarColor() {
		return mUnreachedBarColor;
	}

	public int getReachedBarColor() {
		return mReachedBarColor;
	}

	public int getProgress() {
		return mProgress;
	}

	public int getMax() {
		return mMax;
	}

	public float getReachedBarHeight() {
		return mReachedBarHeight;
	}

	public float getUnreachedBarHeight() {
		return mUnreachedBarHeight;
	}

	public void setProgressTextSize(float TextSize) {
		this.mTextSize = TextSize;
		mTextPaint.setTextSize(mTextSize);
		invalidate();
	}

	public void setProgressTextColor(int TextColor) {
		this.mTextColor = TextColor;
		mTextPaint.setColor(mTextColor);
		invalidate();
	}

	public void setUnreachedBarColor(int BarColor) {
		this.mUnreachedBarColor = BarColor;
		mUnreachedBarPaint.setColor(mReachedBarColor);
		invalidate();
	}

	public void setReachedBarColor(int ProgressColor) {
		this.mReachedBarColor = ProgressColor;
		mReachedBarPaint.setColor(mReachedBarColor);
		invalidate();
	}

	/**
	 * 设置bar最大值
	 * @param Max 最大值
	 */
	public void setMax(int Max) {
		if (Max > 0) {
			this.mMax = Max;
			invalidate();
		}
	}

	public void incrementProgressBy(int by) {
		if (by > 0) {
			setProgress(getProgress() + by);
		}
	}

	/**
	 * 设置progress
	 * @param Progress 进度数据
	 */
	public void setProgress(int Progress) {
		if (Progress <= getMax() && Progress >= 0) {
			this.mProgress = Progress;
			invalidate();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
		bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
		bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize());
		bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight());
		bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight());
		bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor());
		bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor());
		bundle.putInt(INSTANCE_MAX, getMax());
		bundle.putInt(INSTANCE_PROGRESS, getProgress());
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			final Bundle bundle = (Bundle) state;
			mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
			mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
			mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
			mUnreachedBarHeight = bundle.getFloat(INSTANCE_UNREACHED_BAR_HEIGHT);
			mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
			mUnreachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
			initializePainters();
			setMax(bundle.getInt(INSTANCE_MAX));
			setProgress(bundle.getInt(INSTANCE_PROGRESS));
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
			return;
		}
		super.onRestoreInstanceState(state);
	}

	public float dp2px(float dp) {
		final float scale = getResources().getDisplayMetrics().density;
		return dp * scale + 0.5f;
	}

	public float sp2px(float sp) {
		final float scale = getResources().getDisplayMetrics().scaledDensity;
		return sp * scale;
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}

}
