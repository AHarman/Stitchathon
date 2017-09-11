package com.alexharman.stitchathon.KnitPackage;

import java.util.ArrayList;


public class KnitPattern {

    public Stitch[][] stitches;
    public String name;

    // We assume that all prior stitches are done
    private int totalStitchesDone = 0;
    private int currentRow = 0;
    private int nextStitchInRow = 0;
    private int widestRow = 0;
    private String[] stitchTypes;

    // Assuming doubleknit for now. Will be false for knitting on the round
    private boolean oddRowsOpposite = true;

    public KnitPattern(String[][] pattern) {
        buildPattern(pattern);
    }

    public KnitPattern(ArrayList<ArrayList<String>> pattern) {
        String[][] arPattern = new String[pattern.size()][0];
        for (int i = 0; i < pattern.size(); i++) {
            arPattern[i] = pattern.get(i).toArray(new String[pattern.get(i).size()]);
        }
        buildPattern(arPattern);
    }

    private void buildPattern(String[][] pattern) {
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
        if (currentRow == stitches.length-1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.length-1].length)) {
            return;
        }

        totalStitchesDone++;
        stitches[currentRow][nextStitchInRow].done = true;
        nextStitchInRow += onReversedRow() ? -1 : 1;
        if (isEndOfRow()) {
            currentRow++;
            nextStitchInRow = getStartOfRow();
        }
    }

    public int incrementRow() {
        if (currentRow == stitches.length-1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.length-1].length)) {
            return 0;
        }
        int newStitchesDone = 0;
        int direction = onReversedRow() ? -1 : 1;

        for (int i = nextStitchInRow; i*direction <= getEndOfRow(); i += direction) {
            stitches[currentRow][i].done = true;
            newStitchesDone++;
        }

        totalStitchesDone += newStitchesDone;
        currentRow++;
        nextStitchInRow = getStartOfRow();
        return newStitchesDone;
    }

    public void undoStitch() {
        int direction = onReversedRow() ? -1 : 1;
        if(currentRow == 0 && nextStitchInRow == getStartOfRow()) {
            return;
        }

        if (isStartOfRow()) {
            currentRow--;
            nextStitchInRow = getEndOfRow();
        } else {
            nextStitchInRow -= direction;
        }
        stitches[currentRow][nextStitchInRow].done = false;
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

    private boolean onReversedRow() {
        return oddRowsOpposite && (currentRow % 2 == 1);
    }

    private boolean isEndOfRow() {
        if (onReversedRow()) {
            return nextStitchInRow == -1;
        }
        return nextStitchInRow == stitches[currentRow].length;
    }

    private boolean isStartOfRow() {
        if (onReversedRow()) {
            return nextStitchInRow == stitches[currentRow].length - 1;
        }
        return nextStitchInRow == 0;
    }

    private int getEndOfRow() {
        if (onReversedRow()) {
            return 0;
        }
        return stitches[currentRow].length - 1;
    }

    private int getStartOfRow() {
        if (onReversedRow()) {
            return stitches[currentRow].length - 1;
        }
        return 0;
    }

    public int getWidestRow() {
        return widestRow;
    }
}
