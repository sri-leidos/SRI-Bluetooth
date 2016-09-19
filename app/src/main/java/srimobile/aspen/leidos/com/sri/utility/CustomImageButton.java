package srimobile.aspen.leidos.com.sri.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by walswortht on 5/1/2015.
 */
public class CustomImageButton extends ImageButton {

    public static float radius = 18.0f;

    public CustomImageButton(Context context) {
        super(context);
    }

    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //float radius = 36.0f;
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}