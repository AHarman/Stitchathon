package com.alexharman.stitchathon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.alexharman.stitchathon.KnitPackage.KnitPattern;
import com.alexharman.stitchathon.KnitPackage.Stitch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class KnitPatternView extends View {

    private KnitPattern pattern = null;

    // So far only built for two-color double knits.
    // TODO: Build and associative array of stitches->paints
    private Paint mainColorPaint;
    private Paint contrastColorPaint;
    private Paint doneOverlayPaint;
    private Paint bitmapToDrawPaint;
    private float stitchSize = 10;
    private float stitchPad = 2;

    private int viewHeight;
    private int viewWidth;
    int xOffset = 0;
    int yOffset = 0;
    private int[] backgroundColor = {0xFF, 0xFF, 0xFF, 0xFF};
    private boolean fitPatternWidth = true;
    HashMap<String, Bitmap> stitchBitmaps;
    Bitmap patternBitmap;
    Bitmap bitmapToDraw;

    private RectF patternDstRectangle;
    private Rect patternSrcRectangle;

    private Stack<Integer> undoStack = new Stack<>();

    private GestureDetector mGestureDetector;

    public KnitPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);

        stitchBitmaps = new HashMap<>();

        mainColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainColorPaint.setColor(Color.argb(255, 255, 0, 0));
        mainColorPaint.setStyle(Paint.Style.FILL);
        mainColorPaint.setAntiAlias(true);
        contrastColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contrastColorPaint.setColor(Color.argb(255, 0, 0, 255));
        contrastColorPaint.setStyle(Paint.Style.FILL);
        contrastColorPaint.setAntiAlias(true);
        doneOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doneOverlayPaint.setColor(Color.argb(150, backgroundColor[1], backgroundColor[2], backgroundColor[3]));
        doneOverlayPaint.setStyle(Paint.Style.FILL);
        bitmapToDrawPaint = new Paint();
        bitmapToDrawPaint.setAntiAlias(true);
        bitmapToDrawPaint.setFilterBitmap(true);

        mGestureDetector = new GestureDetector(this.getContext(), new gestureListener());
    }

    private void createPatternBitmap() {
        int bitmapWidth = (int) (pattern.getPatternWidth() * stitchSize + (pattern.getPatternWidth() + 1) * stitchPad);
        int bitmapHeight = (int) (pattern.getRows() * stitchSize + (pattern.getRows() + 1) * stitchPad);

        patternBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(patternBitmap);
        canvas.drawARGB(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        drawPattern(canvas);
    }

    private void createStitchBitmaps() {
        float startX = stitchSize * 7/10;
        float stopX = stitchSize * 3/10;
        Canvas canvas;
        Bitmap mBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_4444);
        Bitmap cBitmap = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(mBitmap); canvas.drawColor(mainColorPaint.getColor());
        canvas = new Canvas(cBitmap); canvas.drawColor(contrastColorPaint.getColor());
        stitchBitmaps.put("M", mBitmap);
        stitchBitmaps.put("C", cBitmap);

        Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setAntiAlias(true);
        whitePaint.setFilterBitmap(true);
        whitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setStrokeWidth(2.0f);
        whitePaint.setColor(0xFFFFFFFF);

        Path leftSide = new Path();
        Path rightSide = new Path();
        leftSide.setFillType(Path.FillType.EVEN_ODD);
        leftSide.lineTo(startX - 1.0f, 0);
        leftSide.lineTo(stopX - 1.0f, stitchSize);
        leftSide.lineTo(0, stitchSize);
        leftSide.close();
        rightSide.setFillType(Path.FillType.EVEN_ODD);
        rightSide.moveTo(startX + 1.0f, 0);
        rightSide.lineTo(stitchSize, 0);
        rightSide.lineTo(stitchSize, stitchSize);
        rightSide.lineTo(stopX + 1.0f, stitchSize);
        rightSide.close();

        for (String s : new String[]{"M/M", "C/C", "M/C", "C/M"}) {
            Bitmap b = Bitmap.createBitmap((int)stitchSize, (int)stitchSize, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(b); canvas.drawColor(whitePaint.getColor());
            canvas.drawPath(rightSide, s.startsWith("M") ? mainColorPaint : contrastColorPaint);
            canvas.drawPath(leftSide, s.endsWith("M") ? mainColorPaint : contrastColorPaint);
            stitchBitmaps.put(s, b);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWidth = w;
        bitmapToDraw = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        if (pattern != null) {
            updatePatternSrcRectangle();
            updatePatternDstRectangle();
            updateBitmapToDraw();
        }
        invalidate();
    }

    private void updatePatternSrcRectangle() {
        int left, right, top, bottom;
        if (fitPatternWidth) {
            left = 0;
            right = patternBitmap.getWidth();
            top = 0;
            float ratio = (float)patternBitmap.getWidth() / (float)viewWidth ;
            bottom = Math.min(patternBitmap.getHeight(), (int) ((float)viewHeight * ratio));
        } else {
            left = 0;
            right = Math.min(patternBitmap.getWidth(), viewWidth);
            top = 0;
            bottom = Math.min(patternBitmap.getHeight(), viewHeight);
            left += xOffset;
            right += xOffset;
        }
        top += yOffset;
        bottom += yOffset;
        patternSrcRectangle = new Rect(left, top, right, bottom);
    }

    private void updatePatternDstRectangle() {
        float left, right, top, bottom;
        if (fitPatternWidth) {
            left = 0;
            top = 0;
            right = viewWidth;
            float ratio = (float)viewWidth / (float)patternBitmap.getWidth();
            bottom = Math.min(viewHeight, patternBitmap.getHeight() * ratio);
        } else {
            left = 0;
            top = 0;
            right = Math.min(viewWidth, patternBitmap.getWidth());
            bottom = Math.min(viewHeight, patternBitmap.getHeight());
            if (patternBitmap.getWidth() < viewWidth) {
                left += (viewWidth - patternBitmap.getWidth()) / 2;
                right += (viewWidth - patternBitmap.getWidth()) / 2;
            }
        }
        patternDstRectangle = new RectF(left, top, right, bottom);
    }

    private void zoomPattern() {
        if (pattern == null) {
            return;
        }
        fitPatternWidth = !fitPatternWidth;
        xOffset = 0;
        updatePatternSrcRectangle();
        updatePatternDstRectangle();
        updateBitmapToDraw();
        invalidate();
    }

    private void scroll(float distanceX, float distanceY) {
        if (patternBitmap == null) {
            return;
        }
        float ratio = (float) patternSrcRectangle.width() / patternDstRectangle.width();
        xOffset = (int) Math.min(Math.max(distanceX * ratio + xOffset, 0), patternBitmap.getWidth() - patternSrcRectangle.width());
        yOffset = (int) Math.min(Math.max(distanceY * ratio + yOffset, 0), patternBitmap.getHeight() - patternSrcRectangle.height());
        updatePatternSrcRectangle();
        updateBitmapToDraw();
        invalidate();
    }

    public void incrementOne() {
        if (pattern == null) {
            return;
        }
        undoStack.push(1);
        markStitchesDone(1);
        pattern.increment();
        ((MainActivity) getContext()).updateStitchCounter();
        invalidate();
    }

    public void incrementRow() {
        if (pattern == null) {
            return;
        }
        markStitchesDone(pattern.getStitchesLeftInRow());
        undoStack.push(pattern.incrementRow());
        ((MainActivity) getContext()).updateStitchCounter();
        invalidate();
    }

    public void setPattern(KnitPattern pattern) {
        this.pattern = pattern;
        createStitchBitmaps();
        createPatternBitmap();
        if (viewWidth > 0) {
            updatePatternSrcRectangle();
            updatePatternDstRectangle();
            bitmapToDraw = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pattern == null) {
            return;
        }
        canvas.drawBitmap(bitmapToDraw, 0, 0, null);
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
        Bitmap b = stitchBitmaps.get(stitch.getType());
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
        updateBitmapToDraw();
    }

    private void updateBitmapToDraw() {
        Canvas canvas = new Canvas(bitmapToDraw);
        if (viewWidth > patternDstRectangle.width() ||
                viewHeight > patternDstRectangle.height()) {
            canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF);
        }
        canvas.drawBitmap(patternBitmap, patternSrcRectangle, patternDstRectangle, bitmapToDrawPaint);
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
        updateBitmapToDraw();
        ((MainActivity) getContext()).updateStitchCounter();
        invalidate();
    }

    private class gestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            incrementOne();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            zoomPattern();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scroll(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
