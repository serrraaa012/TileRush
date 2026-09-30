package com.example.tilerush;

public class Tile {
    public int lane;
    public float y;            // top edge of the tile
    public float height;
    public boolean tapped;
    public boolean isHold;     // long tile you must press and hold
    public boolean holding;    // finger is currently down on this hold tile
    public boolean completed;  // hold finished successfully
    public int pointerId = -1; // which finger is holding it
    public float touchY;       // screen line the tile's tail must reach
    public float tapAnim = 0f; // animation progress after tap: 0 to 1

    public Tile(int lane, float y, float height, boolean isHold) {
        this.lane = lane;
        this.y = y;
        this.height = height;
        this.isHold = isHold;
    }
}