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
    private int[] backgroundColor = {0xFF, 0xFF, 0xFF, 0xFF};
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
        doneOverlayPaint.setColor(Color.argb(150, backgroundColor[1], backgroundColor[2], backgroundColor[3]));
        doneOverlayPaint.setStyle(Paint.Style.FILL);

        mGestureDetector = new GestureDetector(this.getContext(), new gestureListener());
    }

    private void createPatternBitmap() {
        int bitmapWidth = (int) (pattern.getPatternWidth() * stitchSize + (pattern.getPatternWidth() + 1) * stitchPad);
        int bitmapHeight = (int) (pattern.getRows() * stitchSize + (pattern.getRows() + 1) * stitchPad);

        mcBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        ccBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_8888);
        patternBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mcBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, mainColorPaint);
        canvas = new Canvas(ccBitmap);
        canvas.drawRect(0.0f, 0.0f, stitchSize, stitchSize, contrastColorPaint);
        canvas = new Canvas(patternBitmap);
        canvas.drawARGB(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        drawPattern(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasHeight = h;
        canvasWidth = w;
    }

    public void incrementOne() {
        undoStack.push(1);
        markStitchesDone(1);
        pattern.increment();
        invalidate();
    }

    public void incrementRow() {
        markStitchesDone(pattern.getStitchesLeftInRow());
        undoStack.push(pattern.incrementRow());
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
            for (int col = 0; col < pattern.getPatternWidth(); col++) {
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
            canvas.drawRect(stitchPad, stitchPad, stitchSize, stitchSize, doneOverlayPaint);
        }
    }

    // Only works until end of row, don't use beyond that
    private void markStitchesDone(int numStitches) {
        int row = pattern.getCurrentRow();
        int col = pattern.getNextStitchInRow();
        Stitch stitch = pattern.stitches[row][col];
        Canvas canvas = new Canvas(patternBitmap);
        float yTranslate = stitchPad + row * (stitchPad + stitchSize);
        float xTranslate;
        if (pattern.getRowDirection() == 1) {
            xTranslate = stitchPad + pattern.getCurrentDistanceInRow() * (stitchPad + stitchSize);
            xTranslate += (stitch.getWidth() - 1) * (stitchSize + stitchPad);
        } else {
            xTranslate = patternBitmap.getWidth() - (pattern.getCurrentDistanceInRow() + 1) * (stitchPad + stitchSize);
        }
        canvas.translate(xTranslate, yTranslate);
        xTranslate = pattern.getRowDirection() * (stitchPad + stitchSize);
        for (int i = 0; i < numStitches; i++) {
            canvas.drawRect(0, 0, stitchSize, stitchSize, doneOverlayPaint);
            canvas.translate(xTranslate, 0);
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
        Canvas canvas = new Canvas(patternBitmap);
        Stitch lastStitch;
        float xTranslate;
        float yTranslate;

        for (int i = 0; i < stitchesToUndo; i++) {
            pattern.undoStitch();
            lastStitch = pattern.stitches[pattern.getCurrentRow()][pattern.getNextStitchInRow()];
            if (pattern.getRowDirection() == 1) {
                xTranslate = pattern.getCurrentDistanceInRow() * (stitchPad + stitchSize) + stitchPad;
            } else {
                xTranslate = patternBitmap.getWidth() - (pattern.getCurrentDistanceInRow() + 1) * (stitchPad + stitchSize);
            }
            yTranslate = pattern.getCurrentRow() * (stitchPad + stitchSize) + stitchPad;
            canvas.translate(xTranslate, yTranslate);
            drawStitch(canvas, lastStitch);
            canvas.setMatrix(null);
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
