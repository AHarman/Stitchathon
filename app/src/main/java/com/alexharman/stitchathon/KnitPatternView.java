package com.alexharman.stitchathon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class KnitPatternView extends View {

    private KnitPattern pattern = null;

    // So far only built for two-color double knits.
    // TODO: Build and associative array of stitches->paints
    private Paint mainColorPaint;
    private Paint contrastColorPaint;
    private Paint textPaint;
    private int stitchSize = 20;
    private int stitchPad = 5;

    private int canvasHeight;
    private int canvasWidth;
    private int stitchesWide;
    private int stitchesHigh;

    public KnitPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mainColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColorPaint.setColor(Color.argb(255, 255, 0, 0));
        mainColorPaint.setStyle(Paint.Style.FILL);
        contrastColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contrastColorPaint.setColor(Color.argb(255, 0, 0, 255));
        contrastColorPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasHeight = h - getPaddingBottom() - getPaddingBottom();
        canvasWidth = w - getPaddingLeft() - getPaddingRight();

        // Want a stitch either side just in case we're not square on.
        stitchesHigh = h / stitchSize + 2;
        stitchesWide = w / stitchSize + 2;
        invalidate();
    }

    public void progressed() {
        invalidate();
    }

    public void setPattern(KnitPattern pattern) {
        this.pattern = pattern;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pattern == null) {
            return;
        }

        //int startPos =

        canvas.translate(canvasWidth - stitchSize, canvasHeight - stitchSize);
        canvas.drawRect(0, 0, stitchSize, stitchSize, mainColorPaint);
        canvas.translate(-stitchSize-stitchPad, 0);
        canvas.drawRect(0, 0, stitchSize, stitchSize, mainColorPaint);

        /*int currentRow = pattern.getCurrentRow();
        for (int patternRow = currentRow - stitchesHigh/2; i < currentRow + stitchesHigh/2; i++) {
            for (int patternCol = pattern.get)
        }*/
    }
}

