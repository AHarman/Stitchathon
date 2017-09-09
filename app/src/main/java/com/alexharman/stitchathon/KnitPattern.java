package com.alexharman.stitchathon;

import org.json.JSONObject;

import java.util.ArrayList;


public class KnitPattern {

    public Stitch[][] stitches;

    // We assume that all prior stitches are done
    private int stitchesDone = 0;
    private int currentRow = 0;
    private int currentStitchInRow = 0;
    private String[] stitchTypes;

    KnitPattern(String[][] pattern) {
        ArrayList<String> stitchTypes = new ArrayList<String>();
        stitches = new Stitch[pattern.length][];
        for (int row = 0; row < pattern.length; row++) {
            stitches[row] = new Stitch[pattern[row].length];
            for (int col = 0; col < pattern[row].length; col++) {
                if (!stitchTypes.contains(pattern[row][col])) {
                    stitchTypes.add(pattern[row][col]);
                }
                stitches[row][col] = new Stitch(pattern[row][col]);
            }
        }
        this.stitchTypes = (String[]) stitchTypes.toArray(new String[0]);
    }

    public void increment() {
        stitchesDone++;
        stitches[currentRow][currentStitchInRow].done = true;
        currentStitchInRow++;
        if (currentStitchInRow >= stitches[currentRow].length){
            currentRow++;
            currentStitchInRow = 0;
        }
    }

    public void incrementRow() {
        stitchesDone += stitches[currentRow].length - currentStitchInRow;
        currentRow++;
        currentStitchInRow = 0;
    }

    public int getStitchesDone() {
        return stitchesDone;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int getCurrentStitchInRow() {
        return currentStitchInRow;
    }

}
