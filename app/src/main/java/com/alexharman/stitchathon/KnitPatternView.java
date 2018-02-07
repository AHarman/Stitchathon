package com.alexharman.stitchathon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.alexharman.stitchathon.KnitPackage.KnitPattern;
import com.alexharman.stitchathon.KnitPackage.Stitch;

import java.util.HashMap;
import java.util.Stack;


public class KnitPatternView extends View {

    private KnitPattern pattern = null;

    // So far only built for two-color double knits.
    private float stitchSize = 10;
    private float stitchPad = 2;

    private boolean fitPatternWidth = true;
    private int viewHeight;
    private int viewWidth;
    int xOffset = 0;
    int yOffset = 0;

    // TODO: Create colour changer and pass this in from preferences
    private int backgroundColor = 0xFF808080;
    private int doneOverlayColor = 0x80FFFFFF;
    int[] colours = {0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0x00000000, 0xFFFFFFFF};

    HashMap<Stitch, Bitmap> stitchBitmaps;
    HashMap<Stitch, Paint> stitchPaints;
    private Paint bitmapToDrawPaint;
    private Paint doneOverlayPaint;
    Bitmap patternBitmap;

    //TODO: Rename "currentView" or something.
    Bitmap bitmapToDraw;

    private RectF patternDstRectangle;
    private Rect patternSrcRectangle;

    private Stack<Integer> undoStack = new Stack<>();

    private GestureDetector mGestureDetector;

    public KnitPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(this.getContext(), new gestureListener());
    }

    public Bitmap createPatternBitmap(KnitPattern knitPattern) {
        int bitmapWidth = (int) (knitPattern.getPatternWidth() * stitchSize + (knitPattern.getPatternWidth() + 1) * stitchPad);
        int bitmapHeight = (int) (knitPattern.getRows() * stitchSize + (knitPattern.getRows() + 1) * stitchPad);

        // TODO: Maybe don't set properties here
        this.stitchPaints = createPaints(knitPattern.stitchTypes);
        this.stitchBitmaps = createStitchBitmaps(knitPattern.stitchTypes);

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundColor);
        drawPattern(canvas, knitPattern);

        return bitmap;
    }

    // TODO: Variable width
    private HashMap<Stitch, Bitmap> createStitchBitmaps(Stitch[] stitches) {
        HashMap<Stitch, Bitmap> stitchBitmaps = new HashMap<>();
        Bitmap bitmap;
        int bitmapWidth;
        Stitch currentStitch;

        for (int i = 0; i < stitches.length; i++) {
            currentStitch = stitches[i];
            if (currentStitch.isSplit()) {
                bitmap = createSplitStitchBitmap(currentStitch);
            } else {
                bitmapWidth = (int) (stitchSize * currentStitch.getWidth() + (currentStitch.getWidth() - 1) * stitchPad);
                bitmap = Bitmap.createBitmap(bitmapWidth, (int) stitchSize, Bitmap.Config.ARGB_8888);
                new Canvas(bitmap).drawPaint(stitchPaints.get(currentStitch));
            }
            stitchBitmaps.put(stitches[i], bitmap);
        }

        return stitchBitmaps;
    }

    // TODO: Variable width
    // We're going to assume that there's only 2 colours
    private Bitmap createSplitStitchBitmap(Stitch stitch) {
        int bitmapWidth = (int) (stitchSize * stitch.getWidth() + (stitch.getWidth() - 1) * stitchPad);
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, (int) stitchSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path leftSide;
        Path rightSide;

        // Can't think of better names for these
        // Basically they define the diagonal
        float startX = stitchSize * 7/10;
        float stopX = stitchSize * 3/10;
        Matrix matrix;

        leftSide = new Path();
        leftSide.setFillType(Path.FillType.EVEN_ODD);
        leftSide.lineTo(startX - 1.0f, 0);
        leftSide.lineTo(stopX - 1.0f, stitchSize);
        leftSide.lineTo(0, stitchSize);
        leftSide.close();

        rightSide = new Path(leftSide);
        matrix = new Matrix();
        matrix.postRotate(180, bitmap.getWidth()/2,bitmap.getHeight() / 2);
        rightSide.transform(matrix);

        canvas.drawPath(rightSide, stitchPaints.get(stitch.getMadeOf()[0]));
        canvas.drawPath(leftSide, stitchPaints.get(stitch.getMadeOf()[1]));
        return bitmap;
    }

    private HashMap<Stitch, Paint> createPaints(Stitch[] stitches) {
        Paint p;
        HashMap<Stitch, Paint> paints = new HashMap<>();
        int colourCount = 0;

        for (Stitch stitch: stitches) {
            if (!stitch.isSplit()) {
                p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(colours[colourCount]);
                p.setStyle(Paint.Style.FILL);
                paints.put(stitch, p);
                colourCount++;
            }
        }

        // TODO: Split stitch paints and others into another function
        doneOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        doneOverlayPaint.setColor(doneOverlayColor);
        doneOverlayPaint.setStyle(Paint.Style.FILL);
        bitmapToDrawPaint = new Paint();
        bitmapToDrawPaint.setAntiAlias(true);
        bitmapToDrawPaint.setFilterBitmap(true);

        return paints;
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

    // TODO: maybe save paints and stitch bitmaps to file or something.
    public void setPattern(KnitPattern pattern, @Nullable Bitmap bitmap) {
        this.pattern = pattern;
        stitchPaints = createPaints(pattern.stitchTypes);
        stitchBitmaps = createStitchBitmaps(pattern.stitchTypes);
        if (bitmap == null) {
            patternBitmap = createPatternBitmap(pattern);
        } else {
            this.patternBitmap = bitmap;
        }

        if (viewWidth > 0) {
            updatePatternSrcRectangle();
            updatePatternDstRectangle();
            bitmapToDraw = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
            updateBitmapToDraw();
        }
        invalidate();
    }

    public void setPattern(KnitPattern pattern) {
        setPattern(pattern, null);
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
        drawPattern(canvas, this.pattern);
    }

    private void drawPattern(Canvas canvas, KnitPattern knitPattern) {
        canvas.translate(0, stitchPad);
        for (int row = 0; row < knitPattern.getRows(); row++) {
            canvas.save();
            canvas.translate(stitchPad, 0);
            for (int col = 0; col < knitPattern.getPatternWidth(); col++) {
                drawStitch(canvas, knitPattern.stitches[row][col], pattern.getCurrentRow() > row && pattern.getNextStitchInRow() > col);
                canvas.translate(stitchSize+stitchPad, 0);
            }
            canvas.restore();
            canvas.translate(0, stitchSize+stitchPad);
        }
    }

    private void drawStitch(Canvas canvas, Stitch stitch, boolean isDone) {
        Bitmap b = stitchBitmaps.get(stitch);
        canvas.drawBitmap(b, 0, 0, isDone ? doneOverlayPaint : null);
    }

    // TODO: Fix so works beyond end of row
    // TODO: I think this breaks if stitches aren't uniform width. Fix that.
    // TODO: Just redo this function. There's too many problems. Break it down into "mark one stitch done" and "mark n stitches", make it use drawStitch() function. See SetColorFilter for the paint

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
            drawStitch(canvas, lastStitch, false);
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
