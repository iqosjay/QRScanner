package com.iqos.qrscanner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

/**
 * Created by 水银灯、 on 2018/03/13.
 * Bitmap与drawable转化
 */
public class BitmapUtil {
    public static Drawable bitmap2Drawable(Context context, int resId) {
        return context.getResources().getDrawable(resId);
    }

    /**
     * bitmap 转 drawable
     *
     * @param bitmap 位图
     * @return drawable
     */
    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    /**
     * drawable to bitmap
     *
     * @param drawable drawable
     * @return bitmap
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (null == drawable) return null;
        if (drawable instanceof BitmapDrawable) {
            //一般图片
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            //.9图片
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ?
                    Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    /**
     * modify the bitmap's color.
     *
     * @param inBitmap  old bitmap.
     * @param tintColor the color what you would like modify.
     * @return bitmap after modified the color
     */
    public static Bitmap tintBitmap(Bitmap inBitmap, int tintColor) {
        if (inBitmap == null) return null;
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inBitmap, 0, 0, paint);
        return outBitmap;
    }


    /**
     * drawable to bitmap
     *
     * @param context 上下文
     * @param resId   资源id
     * @return bitmap
     */
    public static Bitmap drawable2Bitmap(Context context, int resId) {
        if (-1 == resId) return null;
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }
}
