package com.alexharman.stitchathon.KnitPackage;

import java.util.ArrayList;

public class KnitPattern {

    public Stitch[][] stitches;
    public String name;

    // Takes width of stitches into account
    private int currentDistanceInRow = 0;
    private int totalStitchesDone = 0;
    private int totalStitches = 0;
    private int currentRow = 0;
    private int nextStitchInRow = 0;
    private int patternWidth = 0;
    private int rows = 0;
    public ArrayList<Stitch> stitchTypes = new ArrayList<>();

    // Assuming doubleknit for now. Will be false for knitting on the round
    private boolean oddRowsOpposite = true;

    public KnitPattern(String[][] pattern, String name) {
        this(pattern);
        this.name = name;
    }

    public KnitPattern(ArrayList<ArrayList<String>> pattern, String name) {
        this(pattern);
        this.name = name;
    }

    KnitPattern(String[][] pattern) {
        buildPattern(pattern);
    }

    KnitPattern(ArrayList<ArrayList<String>> pattern) {
        String[][] arPattern = new String[pattern.size()][0];
        for (int i = 0; i < pattern.size(); i++) {
            arPattern[i] = pattern.get(i).toArray(new String[pattern.get(i).size()]);
        }
        buildPattern(arPattern);
    }

    private void buildPattern(String[][] pattern) {
        int rowWidth;
        ArrayList<String> stitchTypeStrings = new ArrayList<>();
        stitches = new Stitch[pattern.length][];
        for (int row = 0; row < pattern.length; row++) {
            rowWidth = 0;
            stitches[row] = new Stitch[pattern[row].length];
            for (int col = 0; col < pattern[row].length; col++) {
                addNewStitchType(stitchTypeStrings, pattern[row][col]);
                totalStitches++;
                stitches[row][col] = new Stitch(pattern[row][col]);
                rowWidth += stitches[row][col].getWidth();
            }
            patternWidth = Math.max(patternWidth, rowWidth);
        }
        rows = pattern.length;

        for (String stitchString: stitchTypeStrings) {
            stitchTypes.add(new Stitch(stitchString));
        }
    }

    private void addNewStitchType(ArrayList<String> stitchTypes, String stitch) {
        if (!stitchTypes.contains(stitch)) {
            if (stitch.contains("/")) {
                for (String s : stitch.split("/")) {
                    addNewStitchType(stitchTypes, s);
                }
            }
            stitchTypes.add(stitch);
        }
    }

    public void increment() {
        if (currentRow == stitches.length-1 && (nextStitchInRow < 0 || nextStitchInRow > stitches[stitches.length-1].length)) {
            return;
        }

        currentDistanceInRow += stitches[currentRow][nextStitchInRow].getWidth();
        totalStitchesDone++;
        stitches[currentRow][nextStitchInRow].setDone(true);
        nextStitchInRow += getRowDirection();
        if (isEndOfRow()) {
            currentRow++;
            currentDistanceInRow = 0;
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
            stitches[currentRow][i].setDone(true);
            newStitchesDone++;
        }

        totalStitchesDone += newStitchesDone;
        currentRow++;
        nextStitchInRow = getStartOfRow();
        currentDistanceInRow = 0;
        return newStitchesDone;
    }

    public void undoStitch() {
        if(currentRow == 0 && nextStitchInRow == getStartOfRow()) {
            return;
        }

        if (isStartOfRow()) {
            currentRow--;
            nextStitchInRow = getEndOfRow();
            currentDistanceInRow = 0;
            for (int c = Math.min(getStartOfRow(), getEndOfRow()); c < Math.max(getStartOfRow(), getEndOfRow()); c += 1) {
                currentDistanceInRow += stitches[currentRow][c].getWidth();
            }
        } else {
            currentDistanceInRow -= stitches[currentRow][nextStitchInRow].getWidth();
            nextStitchInRow -= getRowDirection();
        }
        stitches[currentRow][nextStitchInRow].setDone(false);
        totalStitchesDone--;
    }

    public int getTotalStitchesDone() {
        return totalStitchesDone;
    }

    public int getTotalStitches() {
        return totalStitches;
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

    public boolean isEndOfRow() {
        if (getRowDirection() == 1) {
            return nextStitchInRow == stitches[currentRow].length;
        }
        return nextStitchInRow == -1;
    }

    public boolean isStartOfRow() {
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

    public int getStitchesLeftInRow() {
        if (getRowDirection() == 1) {
            return stitches[currentRow].length - nextStitchInRow;
        }
        return nextStitchInRow + 1;
    }

    public int getStitchesDoneInRow() {
        if (getRowDirection() == 1) {
            return nextStitchInRow;
        }
        return stitches[currentRow].length - nextStitchInRow - 1;
    }

    public int getPatternWidth() {
        return patternWidth;
    }

    public int getRows() {
        return rows;
    }

    public int getCurrentDistanceInRow() {
        return currentDistanceInRow;
    }
}
