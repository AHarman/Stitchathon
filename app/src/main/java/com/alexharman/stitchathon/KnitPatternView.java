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
    private float stitchSize = 10;
    private float stitchPad = 2;

    private int canvasHeight;
    private int canvasWidth;
    Bitmap mcBitmap;
    Bitmap ccBitmap;
    Bitmap patternBitmap;

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
    }

    private void createPatternBitmap() {
        int bitmapWidth = (int) (pattern.getWidth() * stitchSize + (pattern.getWidth() + 1) * stitchPad);
        int bitmapHeight = (int) (pattern.getRows() * stitchSize + (pattern.getRows() + 1) * stitchPad);

        mcBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        ccBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        patternBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mcBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, mainColorPaint);
        canvas = new Canvas(ccBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, contrastColorPaint);
        canvas = new Canvas(patternBitmap);
        drawPattern(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasHeight = h;
        canvasWidth = w;
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
        createPatternBitmap();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pattern == null) {
            return;
        }
        canvas.drawBitmap(patternBitmap, 0.0f, 0.0f, null);
    }

    private void drawPattern(Canvas canvas) {
        canvas.translate(0, stitchPad);
        for (int row = 0; row < pattern.getRows(); row++) {
            canvas.save();
            canvas.translate(stitchPad, 0);
            for (int col = 0; col < pattern.getWidth(); col++) {
                drawStitch(canvas, pattern.stitches[row][col]);
                canvas.translate(stitchSize+stitchPad, 0);
            }
            canvas.restore();
            canvas.translate(0, stitchSize+stitchPad);
        }
    }

    private void drawStitch(Canvas canvas, Stitch stitch) {
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
