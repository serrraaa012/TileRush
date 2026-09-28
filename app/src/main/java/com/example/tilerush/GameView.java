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
    private final Paint overlayPaint = new Paint();
    private final List<Tile> tiles = new ArrayList<>();
    private final Random random = new Random();

    private float laneWidth, tileHeight, speed; // speed = pixels per second
    private long lastTime = 0;
    private float spawnTimer = 0;
    private int score = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private long gameOverTime = 0;
    private Tile missedTile = null;

    public GameView(Context context) {
        super(context);
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(3f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        overlayPaint.setColor(Color.argb(210, 255, 255, 255));
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

    private void restart() {
        tiles.clear();
        score = 0;
        spawnTimer = 0;
        gameOver = false;
        missedTile = null;
        lastTime = 0;
        postInvalidateOnAnimation();
    }

    private void endGame(Tile missed) {
        gameOver = true;
        missedTile = missed;
        gameOverTime = System.currentTimeMillis();
    }

    private void update(float dt) {
        // move tiles down
        for (Tile t : tiles) {
            t.y += speed * dt;
        }

        // game over if an untapped tile reaches the bottom
        for (Tile t : tiles) {
            if (!t.tapped && t.y + t.height >= getHeight()) {
                endGame(t);
                return;
            }
        }

        // remove tapped tiles that left the screen
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            Tile t = it.next();
            if (t.y > getHeight()) it.remove();
        }

        // spawn new tiles
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
        if (dt > 0.05f) dt = 0.05f;
        lastTime = now;

        if (running && !gameOver && laneWidth > 0) update(dt);

        // tiles (the missed one is shown in red)
        for (Tile t : tiles) {
            if (t == missedTile) tilePaint.setColor(Color.RED);
            else if (t.tapped) tilePaint.setColor(Color.LTGRAY);
            else tilePaint.setColor(Color.BLACK);
            float left = t.lane * laneWidth;
            canvas.drawRect(left + 4, t.y + 4, left + laneWidth - 4, t.y + t.height - 4, tilePaint);
        }

        // lane dividers
        for (int i = 1; i < LANES; i++) {
            canvas.drawLine(i * laneWidth, 0, i * laneWidth, getHeight(), linePaint);
        }

        // score
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(90f);
        canvas.drawText(String.valueOf(score), getWidth() / 2f, 130f, textPaint);

        // game over screen
        if (gameOver) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(110f);
            canvas.drawText("Game Over", cx, cy - 80f, textPaint);
            textPaint.setTextSize(80f);
            canvas.drawText("Score: " + score, cx, cy + 40f, textPaint);
            textPaint.setTextSize(55f);
            canvas.drawText("Tap to restart", cx, cy + 160f, textPaint);
        }

        if (running && !gameOver) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) {
            return true;
        }

        // on the game over screen, a tap restarts (small delay avoids accidental taps)
        if (gameOver) {
            if (System.currentTimeMillis() - gameOverTime > 600) restart();
            return true;
        }

        int idx = e.getActionIndex();
        float x = e.getX(idx);
        float y = e.getY(idx);
        int lane = Math.min(LANES - 1, Math.max(0, (int) (x / laneWidth)));

        boolean hit = false;
        for (Tile t : tiles) {
            if (t.lane == lane && y >= t.y && y <= t.y + t.height) {
                hit = true;
                if (!t.tapped) {
                    t.tapped = true;
                    score++;
                }
                break;
            }
        }

        // tapping where there is no tile ends the game
        if (!hit) endGame(null);
        return true;
    }
}