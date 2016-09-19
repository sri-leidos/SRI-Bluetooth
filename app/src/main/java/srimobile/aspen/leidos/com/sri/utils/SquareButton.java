package srimobile.aspen.leidos.com.sri.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by walswortht on 5/6/2015.
 */
public class SquareButton extends Button {

    public SquareButton(Context context) {
        super(context);
    }

    public SquareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // This is used to make square buttons.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}