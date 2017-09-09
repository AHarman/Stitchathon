package com.alexharman.stitchathon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class KnitPatternView extends View {

    private KnitPattern pattern = null;

    // So far only built for two-color double knits.
    // TODO: Build and associative array of stitches->paints
    private Paint mainColorPaint;
    private Paint contrastColorPaint;
    private Paint doneOverlayPaint;
    private Paint textPaint;
    private float stitchSize = 1;
    private float stitchPad = 2;

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
        doneOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doneOverlayPaint.setColor(Color.argb(100, 255, 255, 255));
        doneOverlayPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasHeight = h;
        canvasWidth = w;

        Log.wtf("ok", "onsizechanged");

        // Want a stitch either side just in case we're not square on.
        stitchesHigh = (int) (h / (stitchSize + stitchPad) + 1);
        stitchesWide = (int) (w / (stitchSize + stitchPad) + 1);
        fitPatternWidth();
    }

    public void progressed() {
        invalidate();
    }

    public void setPattern(KnitPattern pattern) {
        this.pattern = pattern;
        invalidate();
    }

    public void fitPatternWidth() {
        Log.wtf("ok", "longest: " + pattern.getWidestRow());
        stitchesWide = pattern.getWidestRow();
        float totalPadding = stitchPad * (stitchesWide + 1);
        stitchSize = (float)(canvasWidth - totalPadding)/ (float)stitchesWide;
        stitchesHigh = (int) (canvasHeight / (stitchSize + stitchPad) + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pattern == null) {
            return;
        }

        canvas.translate(canvasWidth, canvasHeight - stitchPad - stitchSize);
        Log.wtf("ok", "stitch size: " + stitchSize);
        Log.wtf("ok", "translate: " + (canvasWidth - stitchPad - stitchSize) + ", " + (canvasHeight - stitchPad - stitchSize));
        for (int row = 0; row < Math.min(stitchesHigh, pattern.stitches.length); row++) {
            canvas.save();
            for (int col = 0; col < Math.min(pattern.stitches[row].length, stitchesWide); col++) {
                canvas.translate(-stitchSize-stitchPad, 0);
                canvas.drawRect(0, 0, stitchSize, stitchSize, pattern.stitches[row][col].getType() == "M" ? mainColorPaint : contrastColorPaint);
            }
            canvas.restore();
            canvas.translate(0, -stitchSize-stitchPad);
        }
    }
}
