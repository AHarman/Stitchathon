package com.alexharman.stitchathon.KnitPackage;

import java.util.ArrayList;


public class KnitPattern {

    public Stitch[][] stitches;
    public String name;

    // Takes width of stitches into account
    private int currentStitchDistance = 0;
    // We assume that all prior stitches are done
    private int totalStitchesDone = 0;
    private int currentRow = 0;
    private int nextStitchInRow = 0;
    private int width = 0;
    private int rows = 0;
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
            width = Math.max(width, stitches[row].length);
            for (int col = 0; col < pattern[row].length; col++) {
                if (!stitchTypes.contains(pattern[row][col])) {
                    stitchTypes.add(pattern[row][col]);
                }
                stitches[row][col] = new Stitch(pattern[row][col]);
            }
        }
        rows = pattern.length;
        this.stitchTypes = stitchTypes.toArray(new String[0]);
    }

    public void increment() {
        if (currentRow == stitches.length-1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.length-1].length)) {
            return;
        }

        currentStitchDistance += stitches[currentRow][nextStitchInRow].getWidth();
        totalStitchesDone++;
        stitches[currentRow][nextStitchInRow].done = true;
        nextStitchInRow += getRowDirection();
        if (isEndOfRow()) {
            currentRow++;
            currentStitchDistance = 0;
            nextStitchInRow = getStartOfRow();
        }
    }

    public int incrementRow() {
        if (currentRow == stitches.length-1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.length-1].length)) {
            return 0;
        }
        int newStitchesDone = 0;
        int direction = getRowDirection();

        for (int i = nextStitchInRow; i*direction <= getEndOfRow(); i += direction) {
            stitches[currentRow][i].done = true;
            newStitchesDone++;
        }

        totalStitchesDone += newStitchesDone;
        currentRow++;
        nextStitchInRow = getStartOfRow();
        currentStitchDistance = 0;
        return newStitchesDone;
    }

    public void undoStitch() {
        if(currentRow == 0 && nextStitchInRow == getStartOfRow()) {
            return;
        }

        if (isStartOfRow()) {
            currentRow--;
            nextStitchInRow = getEndOfRow();
        } else {
            nextStitchInRow -= getRowDirection();
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

    public int getRowDirection() {
        return oddRowsOpposite && (currentRow % 2 == 1) ? -1 : 1;
    }

    private boolean isEndOfRow() {
        if (getRowDirection() == 1) {
            return nextStitchInRow == stitches[currentRow].length;
        }
        return nextStitchInRow == -1;
    }

    private boolean isStartOfRow() {
        if (getRowDirection() == 1) {
            return nextStitchInRow == 0;
        }
        return nextStitchInRow == stitches[currentRow].length - 1;
    }

    private int getEndOfRow() {
        if (getRowDirection() == 1) {
            return stitches[currentRow].length - 1;
        }
        return 0;
    }

    private int getStartOfRow() {
        if (getRowDirection() == 1) {
            return 0;
        }
        return stitches[currentRow].length - 1;
    }

    public int getWidth() {
        return width;
    }

    public int getRows() {
        return rows;
    }

    public int getCurrentStitchDistance() {
        return currentStitchDistance;
    }
}
