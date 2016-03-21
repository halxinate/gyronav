package com.kukarin.app.gyronav;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by Alex on 3/20/2016.
 */
public class RotmenuWidget extends View {
    //curved text
    private static final String MY_TEXT = "xjaphx: Draw Text on Curve";
    private Path mArc;
    private Paint mPaintText;

    //anim
    int framesPerSecond = 60;
    long animationDuration = 10000; // 10 seconds

    Matrix matrix = new Matrix(); // transformation matrix

    Path path = new Path();       // your path
    Paint paint = new Paint();    // your paint

    long startTime;


    public RotmenuWidget(Context context) {
        super(context);
        //curved text
        mArc = new Path();
        RectF oval = new RectF(50, 100, 200, 250);

        mArc.addArc(oval, -180, 200);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(20f);

        //anim
        // start the animation:
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //curved text
        canvas.drawTextOnPath(MY_TEXT, mArc, 0, 20, mPaintText);
        invalidate();

        //anim
        long elapsedTime = System.currentTimeMillis() - startTime;

        matrix.postRotate(30 * elapsedTime / 1000);        // rotate 30° every second
        matrix.postTranslate(100 * elapsedTime / 1000, 0); // move 100 pixels to the right
        // other transformations...

        canvas.concat(matrix);        // call this before drawing on the canvas!!

        canvas.drawPath(path, paint); // draw on canvas

        if (elapsedTime < animationDuration)
            this.postInvalidateDelayed(1000 / framesPerSecond);
    }
}
