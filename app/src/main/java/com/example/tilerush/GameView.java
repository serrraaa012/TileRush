package com.example.tilerush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private static final int LANES = 4;

    private final Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Tile> tiles = new ArrayList<>();
    private final Random random = new Random();

    private float laneWidth, tileHeight, speed; // speed = pixels per second
    private long lastTime = 0;
    private float spawnTimer = 0;
    private int score = 0;
    private boolean running = false;

    public GameView(Context context) {
        super(context);
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(3f);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(90f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        laneWidth = w / (float) LANES;
        tileHeight = h / 4f;
        speed = h * 0.6f;
    }

    public void resume() {
        running = true;
        lastTime = 0;
        postInvalidateOnAnimation();
    }

    public void pause() {
        running = false;
    }

    private void update(float dt) {
        // move tiles down and remove the ones that left the screen
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile t = it.next();
            t.y += speed * dt;
            if (t.y > getHeight()) it.remove();
        }

        // spawn a new tile each time the previous row has moved one tile down
        spawnTimer += dt;
        float interval = tileHeight / speed;
        while (spawnTimer >= interval) {
            spawnTimer -= interval;
            tiles.add(new Tile(random.nextInt(LANES), -tileHeight, tileHeight));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        long now = System.nanoTime();
        float dt = (lastTime == 0) ? 0f : (now - lastTime) / 1_000_000_000f;
        if (dt > 0.05f) dt = 0.05f;   // avoids big jumps after lag
        lastTime = now;

        if (running && laneWidth > 0) update(dt);

        // tiles
        for (Tile t : tiles) {
            tilePaint.setColor(t.tapped ? Color.LTGRAY : Color.BLACK);
            float left = t.lane * laneWidth;
            canvas.drawRect(left + 4, t.y + 4, left + laneWidth - 4, t.y + t.height - 4, tilePaint);
        }

        // lane dividers
        for (int i = 1; i < LANES; i++) {
            canvas.drawLine(i * laneWidth, 0, i * laneWidth, getHeight(), linePaint);
        }

        // score
        canvas.drawText(String.valueOf(score), getWidth() / 2f, 130f, textPaint);

        if (running) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int idx = e.getActionIndex();
            float x = e.getX(idx);
            float y = e.getY(idx);
            int lane = Math.min(LANES - 1, Math.max(0, (int) (x / laneWidth)));

            for (Tile t : tiles) {
                if (!t.tapped && t.lane == lane && y >= t.y && y <= t.y + t.height) {
                    t.tapped = true;
                    score++;
                    break;
                }
            }
        }
        return true;
    }
}