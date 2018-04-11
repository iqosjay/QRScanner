package com.iqos.qrscanner.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.iqos.qrscanner.R;
import com.iqos.qrscanner.camera.CameraManager;


/**
 * 二维码自定义扫描界面
 */
public final class ViewfinderView extends View {
    private static final long ANIMATION_DELAY = 1L;//刷新界面的时间
    private static final int DEFAULT_CORNER_WIDTH = 1;//四个绿色边角对应的宽度
    private static final int DEFAULT_CORNER_LENGTH = 30;//四个绿色边角对应的长度
    private static final int DEFAULT_MOVE_SPEED = 5;//中间那条线每次刷新移动的距离
    private static final int DEFAULT_TIP_TEXT_SIZE = 14;//默认字体大小
    private static final int DEFAULT_SCAN_RES_RECT_WIDTH = 18;//扫描线的粗细
    private static final int DEFAULT_TXT_PADDING_RECT = 10;//文本与取景框间距(默认值)
    private Paint mPaint = new Paint();//画笔对象的引用
    private int slideTop;//中间滑动线的最顶端位置
    private boolean tipAboveRect;//文本在上？
    private boolean isFirst;
    private int tipTextSize;//扫码文本字体大小
    private int cornerColor;//角的颜色
    private int cornerWidth;//角的宽度
    private int cornerLength;//角的长
    private int textPaddingRect;//文本与取景框间距
    private int scanLineRes;//扫描线的资源id
    private int lineMoveSpeed;//扫描线移动速度
    private String tipText;//显示文本
    private Context context;

    /**
     * 当从XML布局里引用此控件的时候、会调用两个参数的构造函数
     *
     * @param context 上下文
     * @param attrs   自定义属性
     */
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.initialAttrs(attrs);
    }

    /**
     * 读取XML里面的自定义属性
     *
     * @param attrs 自定义属性
     */
    private void initialAttrs(AttributeSet attrs) {
        TypedArray td = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView);

        //----------------------------------提示文本--------------------------------------------
        tipText = td.getString(R.styleable.ViewfinderView_scan_tip_text);

        //----------------------------------文本是否处于扫描框的上方--------------------------------
        tipAboveRect = td.getBoolean(R.styleable.ViewfinderView_scan_text_above_rect, false);

        //----------------------------------提示文本与扫描框的距离---------------------------------
        textPaddingRect = td.getDimensionPixelSize(R.styleable.ViewfinderView_scan_text_padding_rect, DEFAULT_TXT_PADDING_RECT);

        //----------------------------------提示文本大小---------------------------------------
        tipTextSize = td.getDimensionPixelSize(R.styleable.ViewfinderView_scan_tip_text_size, DEFAULT_TIP_TEXT_SIZE);

        //----------------------------------角的长度---------------------------------------------
        cornerLength = td.getDimensionPixelSize(R.styleable.ViewfinderView_scan_rect_corner_length, DEFAULT_CORNER_LENGTH);

        //----------------------------------角的颜色---------------------------------------------
        cornerColor = td.getColor(R.styleable.ViewfinderView_scan_rect_corner_color, Color.GREEN);

        //----------------------------------角宽-----------------------------------------------
        cornerWidth = td.getDimensionPixelOffset(R.styleable.ViewfinderView_scan_rect_corner_width, DEFAULT_CORNER_WIDTH);

        //----------------------------------扫描线---------------------------------------------
        scanLineRes = td.getResourceId(R.styleable.ViewfinderView_scan_line_res, -1);

        //----------------------------------扫描线移动速度--------------------------------------
        lineMoveSpeed = td.getInteger(R.styleable.ViewfinderView_scan_line_move_speed, DEFAULT_MOVE_SPEED);
        lineMoveSpeed = lineMoveSpeed > DEFAULT_MOVE_SPEED ? DEFAULT_MOVE_SPEED : lineMoveSpeed;
        td.recycle();
    }

    /**
     * 绘制扫描的界面
     *
     * @param canvas 画布
     */
    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) return;
        //初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
        }
        //获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        //画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        //扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);
        //画扫描框边上的角，总共8个部分
        mPaint.setColor(cornerColor);
        canvas.drawRect(frame.left, frame.top, frame.left + cornerLength, frame.top + cornerWidth, mPaint);
        canvas.drawRect(frame.left, frame.top, frame.left + cornerWidth, frame.top + cornerLength, mPaint);
        canvas.drawRect(frame.right - cornerLength, frame.top, frame.right, frame.top + cornerWidth, mPaint);
        canvas.drawRect(frame.right - cornerWidth, frame.top, frame.right, frame.top + cornerLength, mPaint);
        canvas.drawRect(frame.left, frame.bottom - cornerWidth, frame.left + cornerLength, frame.bottom, mPaint);
        canvas.drawRect(frame.left, frame.bottom - cornerLength, frame.left + cornerWidth, frame.bottom, mPaint);
        canvas.drawRect(frame.right - cornerLength, frame.bottom - cornerWidth, frame.right, frame.bottom, mPaint);
        canvas.drawRect(frame.right - cornerWidth, frame.bottom - cornerLength, frame.right, frame.bottom, mPaint);
        //---------------绘制中间的线,每次刷新界面，中间的线往下移动SPEED_DISTANCE---------------//
        slideTop += lineMoveSpeed;
        if (slideTop + DEFAULT_SCAN_RES_RECT_WIDTH >= frame.bottom) {
            slideTop = frame.top;
        }
        Rect lineRect = new Rect();
        lineRect.left = frame.left;
        lineRect.right = frame.right;
        lineRect.top = slideTop;
        lineRect.bottom = slideTop + DEFAULT_SCAN_RES_RECT_WIDTH;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), scanLineRes);
        if (null == bitmap) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qrcode_scan_line);
        }
        canvas.drawBitmap(bitmap, null, lineRect, mPaint);
        //----------------------------画扫描框下面的字---------------------------------//
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(tipTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAlpha(0x60);
        mPaint.setTypeface(Typeface.create(ViewfinderView.class.getSimpleName(), Typeface.BOLD));
        if (!TextUtils.isEmpty(tipText)) {
            if (tipAboveRect) {
                canvas.drawText(tipText, getMeasuredWidth() / 2, frame.top - textPaddingRect, mPaint);
            } else {
                Rect rect = new Rect();
                mPaint.getTextBounds(tipText, 0, tipText.length(), rect);
                canvas.drawText(tipText, getMeasuredWidth() / 2, frame.bottom + rect.height() + textPaddingRect, mPaint);
            }
        }
        //只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }

    public void drawViewfinder() {
        invalidate();
    }


}
