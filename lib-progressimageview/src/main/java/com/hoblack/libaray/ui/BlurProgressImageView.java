package com.hoblack.libaray.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hoblack.libaray.widget.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Blur progress image/color
 *
 * @author hoblack
 * @version 0.1
 */
public class BlurProgressImageView extends View {
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int mOrientation = VERTICAL;
    private int progress;

    private Paint mBlurPaint;
    private Paint mProgressPaint;
    private Rect mSrcRect;
    private Rect mDestRect;
    private Rect mProgressRect;
    private Bitmap rawBitmap;
    private Bitmap progressBitmap;
    private Bitmap shadowBitmap;
    private int width;
    private int height;
    private float blurRadius;
    private float blurAlpha;
    private float gradRadius;
    private int progressColor;
    private boolean showImage = false;

    public BlurProgressImageView(Context context) {
        super(context);
        init(context, null);
    }

    public BlurProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BlurProgressImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mBlurPaint = new Paint();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BlurProgressImageView, 0, 0);
        int n = a.getIndexCount();
        Drawable src_resource;
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.BlurProgressImageView_progress_color) {
                progressColor = a.getColor(attr, Color.BLACK);

            } else if (attr == R.styleable.BlurProgressImageView_progress_blurRadius) {
                blurRadius = a.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.BlurProgressImageView_progress_blurAlpha) {
                blurAlpha = a.getFloat(attr, 1.0f);

            } else if (attr == R.styleable.BlurProgressImageView_progress_image) {
                src_resource = a.getDrawable(attr);
                rawBitmap = drawableToBitmap(src_resource);

            } else if (attr == R.styleable.BlurProgressImageView_progress_gradRadius) {
                gradRadius = a.getDimensionPixelSize(attr, 0);

            } else if (attr == R.styleable.BlurProgressImageView_progress_value) {
                progress = a.getInt(attr, 0);

            } else if (attr == R.styleable.BlurProgressImageView_progress_showImage) {
                showImage = a.getBoolean(attr, false);

            }
        }

        mSrcRect = new Rect();
        mDestRect = new Rect();
        mProgressRect = new Rect();

        mProgressPaint = new Paint();
    }

    @SuppressLint("DrawAllocation")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        canvas.translate(getBlurRadius(), getBlurRadius());

        width = (int) (this.getMeasuredWidth() - 2 * getBlurRadius());
        height = (int) (this.getMeasuredHeight() - 2 * getBlurRadius());

        //Get Alpha bitmap with despired width and height
        shadowBitmap = Bitmap.createScaledBitmap(rawBitmap.extractAlpha()
                , width, height, true);
        mSrcRect.set(0, 0, shadowBitmap.getWidth(), shadowBitmap.getHeight());
        mProgressRect.set(0, 0, shadowBitmap.getWidth(), shadowBitmap.getHeight());

        mBlurPaint.setColor(progressColor);
        if (blurRadius == 0) {
            blurRadius = 0.01f;
        }
        mBlurPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER));
        mBlurPaint.setAlpha((int) (blurAlpha * 255));
        //draw outer shadow
        canvas.drawBitmap(shadowBitmap, mSrcRect, mSrcRect, mBlurPaint);

        rawBitmap = Bitmap.createScaledBitmap(rawBitmap, width, height, true);
        progressBitmap = getProgressDrawToBitmap();

        if (progressBitmap == null) return;

        mBlurPaint.reset();
        int save = canvas.saveLayer(0, 0
                , mProgressRect.width(), mProgressRect.height()
                , mBlurPaint, Canvas.ALL_SAVE_FLAG); //离屛缓存

        canvas.drawBitmap(rawBitmap, 0, 0, mBlurPaint);
        mBlurPaint.setXfermode(new PorterDuffXfermode(showImage
                ? PorterDuff.Mode.DST_IN : PorterDuff.Mode.SRC_IN));//设置 Xfermode
        canvas.drawBitmap(progressBitmap, 0, 0, mBlurPaint);
        mBlurPaint.setXfermode(null); //用完之后及时清理

        canvas.restoreToCount(save);
//        Paint paint = new Paint();
//
//        Bitmap bitmapA = rawBitmap;//BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        Bitmap bitmapB = getProgressDrawToBitmap();
//
//        Shader shaderA = new BitmapShader(bitmapA, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//        Shader shaderB = new BitmapShader(bitmapB, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//
//        Shader shader2 = new ComposeShader(shaderA, shaderB, PorterDuff.Mode.SRC_IN);
//
//        paint.setShader(shader2);
//        canvas.drawRect(0, 0, width, height, paint);
    }

    /**
     * Get progress bitmap by linear gradient with position controlling progress
     */
    public Bitmap getProgressDrawToBitmap() {
        if (mProgressRect.height() + mProgressRect.width() == 0) return null;

        Bitmap whiteBgBitmap = Bitmap.createBitmap(mProgressRect.width()
                , mProgressRect.height()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBgBitmap);

        /**
         * Linear gradient
         */
        int[] colors = {progressColor, Color.TRANSPARENT, Color.TRANSPARENT};//, Color.YELLOW, Color.MAGENTA};
        float[] position = {progress / 100f
                , progress / 100f + gradRadius
                / (getOrientation() == VERTICAL ? mProgressRect.height() : mProgressRect.width())
                , 1f};//, 0.6f, 1.0f};
        Shader shader = null;

        if (getOrientation() == HORIZONTAL) {
            shader = new LinearGradient(-getBlurRadius(), 0
                    , mProgressRect.width(), 0
                    , colors, position, Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(mProgressRect.width(), mProgressRect.height() + getBlurRadius()
                    , mProgressRect.width(), 0
                    , colors, position, Shader.TileMode.CLAMP);
        }
        mProgressPaint.setShader(shader);
        canvas.drawRect(mProgressRect, mProgressPaint);

        return whiteBgBitmap;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Override onMeasure to solve wrap_content differ with match_parent
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = width;
        int width = height;
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSpecSize = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, height);
        }
    }

    /**
     * Should the layout be a column or a row.
     *
     * @param orientation Pass {@link #HORIZONTAL} or {@link #VERTICAL}. Default
     *                    value is {@link #HORIZONTAL}.
     * @attr ref android.R.styleable#LinearLayout_orientation
     */
    public void setOrientation(@OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }

    /**
     * Returns the current orientation.
     *
     * @return either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    @OrientationMode
    public int getOrientation() {
        return mOrientation;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress != getProgress()) {
            this.progress = progress;
            requestLayout();
        }
    }

    public float getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(float blurRadius) {
        if (blurRadius != getBlurRadius()) {
            this.blurRadius = blurRadius;
            requestLayout();
        }
    }

    public float getGradRadius() {
        return gradRadius;
    }

    public void setGradRadius(float gradRadius) {
        if (gradRadius != getGradRadius()) {
            this.gradRadius = gradRadius;
            requestLayout();
        }
    }

    public float getBlurAlpha() {
        return blurAlpha;
    }

    public void setBlurAlpha(float blurAlpha) {
        if (blurAlpha != getBlurAlpha()) {
            this.blurAlpha = blurAlpha;
            requestLayout();
        }
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        if (progressColor != getProgressColor()) {
            this.progressColor = progressColor;
            requestLayout();
        }
    }

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        if (showImage != isShowImage()) {
            this.showImage = showImage;
            requestLayout();
        }
    }
}
