package com.example.tilerush;

public class Tile {
    public int lane;
    public float y;        // top edge of the tile
    public float height;   // will be longer for hold tiles later
    public boolean tapped;

    public Tile(int lane, float y, float height) {
        this.lane = lane;
        this.y = y;
        this.height = height;
    }
}