package com.alexharman.stitchathon;

import android.util.Log;

import java.util.ArrayList;


public class KnitPattern {

    public Stitch[][] stitches;

    // We assume that all prior stitches are done
    private int totalStitchesDone = 0;
    private int currentRow = 0;
    private int nextStitchInRow = 0;
    private int widestRow = 0;
    private String[] stitchTypes;

    // Assuming doubleknit for now. Will be false for knitting on the round
    private boolean oddRowsOpposite = true;

    KnitPattern(String[][] pattern) {
        ArrayList<String> stitchTypes = new ArrayList<String>();
        stitches = new Stitch[pattern.length][];
        for (int row = 0; row < pattern.length; row++) {
            stitches[row] = new Stitch[pattern[row].length];
            widestRow = Math.max(widestRow, stitches[row].length);
            for (int col = 0; col < pattern[row].length; col++) {
                if (!stitchTypes.contains(pattern[row][col])) {
                    stitchTypes.add(pattern[row][col]);
                }
                stitches[row][col] = new Stitch(pattern[row][col]);
            }
        }
        this.stitchTypes = stitchTypes.toArray(new String[0]);
    }

    public void increment() {
        totalStitchesDone++;

        stitches[currentRow][nextStitchInRow].done = true;

        nextStitchInRow += oddRowsOpposite && currentRow % 2 == 1 ? -1 : 1;
        if (isEndOfRow()) {
            Log.d("row row", "in the end of row bit");
            currentRow++;
            nextStitchInRow = oddRowsOpposite && currentRow % 2 == 1 ? stitches[currentRow].length - 1 : 0;
        }
    }

    private boolean isEndOfRow() {
        if (oddRowsOpposite && currentRow % 2 == 1) {
            return nextStitchInRow == -1;
        }
        return nextStitchInRow == stitches[currentRow].length;
    }

    public void incrementRow() {
        totalStitchesDone += stitches[currentRow].length - nextStitchInRow;
        currentRow++;
        nextStitchInRow = 0;
    }

    public int getTotalStitchesDone() {
        return totalStitchesDone;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int getNextStitchInRow() {
        return nextStitchInRow;
    }

    public int getWidestRow() {
        return widestRow;
    }
}
