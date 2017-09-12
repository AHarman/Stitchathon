package com.alexharman.stitchathon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.alexharman.stitchathon.KnitPackage.KnitPattern;
import com.alexharman.stitchathon.KnitPackage.Stitch;

import java.util.Stack;


public class KnitPatternView extends View {

    private KnitPattern pattern = null;

    // So far only built for two-color double knits.
    // TODO: Build and associative array of stitches->paints
    private Paint mainColorPaint;
    private Paint contrastColorPaint;
    private Paint doneOverlayPaint;
    private float stitchSize = 1;
    private float stitchPad = 2;

    private int canvasHeight;
    private int canvasWidth;
    private int stitchesWide;
    private int stitchesHigh;
    Bitmap mcBitmap;
    Bitmap ccBitmap;

    private Stack<Integer> undoStack = new Stack<>();

    private GestureDetector mGestureDetector;

    public KnitPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mainColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColorPaint.setColor(Color.argb(255, 255, 0, 0));
        mainColorPaint.setStyle(Paint.Style.FILL);
        contrastColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contrastColorPaint.setColor(Color.argb(255, 0, 0, 255));
        contrastColorPaint.setStyle(Paint.Style.FILL);
        doneOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doneOverlayPaint.setColor(Color.argb(150, 255, 255, 255));
        doneOverlayPaint.setStyle(Paint.Style.FILL);

        mGestureDetector = new GestureDetector(this.getContext(), new gestureListener());

        mcBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        ccBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mcBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, mainColorPaint);
        canvas = new Canvas(ccBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, contrastColorPaint);
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

    public void incrementOne() {
        pattern.increment();
        undoStack.push(1);
        invalidate();
    }

    public void incrementRow() {
        Integer stitchesIncremented = pattern.incrementRow();
        undoStack.push(stitchesIncremented);
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
        stitchSize = (canvasWidth - totalPadding) / (float)stitchesWide;
        stitchesHigh = (int) (canvasHeight / (stitchSize + stitchPad) + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pattern == null) {
            return;
        }

        canvas.translate(canvasWidth, canvasHeight - stitchPad - stitchSize);
        for (int row = 0; row < Math.min(stitchesHigh, pattern.stitches.length); row++) {
            canvas.save();
            for (int col = 0; col < Math.min(pattern.stitches[row].length, stitchesWide); col++) {
                drawStitch(canvas, pattern.stitches[row][col]);
            }
            canvas.restore();
            canvas.translate(0, -stitchSize-stitchPad);
        }
    }

    private void drawStitch(Canvas canvas, Stitch stitch) {
        canvas.translate(-stitchSize-stitchPad, 0);
        Bitmap b = stitch.getType().equals("M") ? mcBitmap : ccBitmap;
        canvas.drawBitmap(b, 0, 0, stitch.done ? doneOverlayPaint : null);
        if (stitch.done) {
            canvas.drawRect(0, 0, stitchSize, stitchSize, doneOverlayPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void undo() {
        if (undoStack.size() == 0) {
            return;
        }
        Integer stitchesToUndo = undoStack.pop();
        for (int i = 0; i < stitchesToUndo; i++) {
            pattern.undoStitch();
        }
        invalidate();
    }

    private class gestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            incrementOne();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
