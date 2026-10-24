package com.stickynotev2.model;

import java.util.ArrayList;
import java.util.List;

public class InkStroke {
    private List<int[]> points = new ArrayList<>();
    private String colourHex;
    private int width;
    private boolean behindText;

    public void addPoint(int x, int y){
        int[] point = {x,y};
        points.add(point);
    }

    public InkStroke() {

    }

    public List<int[]> getPoints() {
        return points;
    }

    public void setPoints(List<int[]> points) {
        this.points = points;
    }

    public String getColourHex() {
        return colourHex;
    }

    public void setColourHex(String colourHex) {
        this.colourHex = colourHex;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isBehindText() {
        return behindText;
    }

    public void setBehindText(boolean behindText) {
        this.behindText = behindText;
    }
}
