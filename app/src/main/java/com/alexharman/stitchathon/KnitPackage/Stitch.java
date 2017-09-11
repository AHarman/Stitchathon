package com.alexharman.stitchathon.KnitPackage;

public class Stitch {
    private int width;
    private String type;
    public boolean done;

    Stitch(String type, int width) {
        this.width = width;
        this.type = type;
    }

    Stitch(String type) {
        this(type, 1);
    }

    public int getWidth() {
        return width;
    }

    public String getType() {
        return type;
    }
}
