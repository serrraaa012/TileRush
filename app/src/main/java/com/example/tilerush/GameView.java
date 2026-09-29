package com.example.tilerush;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private SoundPool soundPool;
    private int tapSound;
    private MediaPlayer bgMusic;
    private static final int LANES = 4;
    private static final int CYAN = Color.rgb(0, 225, 255);
    private static final int MAGENTA = Color.rgb(255, 70, 200);
    private static final int AMBER = Color.rgb(255, 190, 40);

    private final Paint bgPaint = new Paint();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint();
    private final RectF rect = new RectF();
    private final List<Tile> tiles = new ArrayList<>();
    private final Random random = new Random();

    private float laneWidth, tileHeight, baseSpeed; // baseSpeed = pixels per second
    private long lastTime = 0;
    private float spawnDist = 0;
    private int score = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private long gameOverTime = 0;
    private Tile missedTile = null;

    public GameView(Context context, MediaPlayer bgMusic) {
        super(context);
        this.bgMusic = bgMusic;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5f);
        linePaint.setColor(Color.argb(60, 130, 210, 255));
        linePaint.setStrokeWidth(3f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        overlayPaint.setColor(Color.argb(215, 8, 6, 28));

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build();
        tapSound = soundPool.load(context, R.raw.tap, 1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        laneWidth = w / (float) LANES;
        tileHeight = h / 4f;
        baseSpeed = h * 0.6f;
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                Color.rgb(10, 10, 34), Color.rgb(56, 18, 112), Shader.TileMode.CLAMP));
    }

    public void resume() {
        running = true;
        lastTime = 0;
        if (bgMusic != null && !bgMusic.isPlaying()) bgMusic.start();
        postInvalidateOnAnimation();
    }

    public void pause() {
        running = false;
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void restart() {
        tiles.clear();
        score = 0;
        spawnDist = 0;
        gameOver = false;
        missedTile = null;
        lastTime = 0;
        if (bgMusic != null && !bgMusic.isPlaying()) bgMusic.start();
        postInvalidateOnAnimation();
    }

    private void endGame(Tile missed) {
        gameOver = true;
        missedTile = missed;
        gameOverTime = System.currentTimeMillis();
        if (bgMusic != null && bgMusic.isPlaying()) bgMusic.pause();
    }

    private void spawnTile() {
        int lane = random.nextInt(LANES);
        // hold tiles start appearing once the score reaches 5
        boolean hold = score >= 5 && random.nextFloat() < 0.25f;
        int rows = hold ? 2 + random.nextInt(2) : 1;   // hold tiles are 2 or 3 rows long
        float h = rows * tileHeight;
        tiles.add(new Tile(lane, -h + spawnDist, h, hold));
        spawnDist -= (rows - 1) * tileHeight;           // leave room so rows stay lined up
    }

    private void update(float dt) {
        // speed grows with score, up to 2x
        float speed = baseSpeed * (1f + Math.min(score, 100) * 0.01f);

        for (Tile t : tiles) {
            t.y += speed * dt;
            // hold finished when the tile's tail reaches the hold line
            if (t.holding && t.y >= t.touchY) {
                t.holding = false;
                t.completed = true;
                score += 2;   // bonus for finishing a hold
            }
        }

        // game over if an untapped tile reaches the bottom
        for (Tile t : tiles) {
            if (!t.tapped && t.y + t.height >= getHeight()) {
                endGame(t);
                return;
            }
        }

        // remove tiles that left the screen
        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            if (it.next().y > getHeight()) it.remove();
        }

        // spawn new tiles based on distance travelled
        spawnDist += speed * dt;
        while (spawnDist >= tileHeight) {
            spawnDist -= tileHeight;
            spawnTile();
        }
    }

    private int darken(int c) {
        return Color.rgb((int) (Color.red(c) * 0.35f), (int) (Color.green(c) * 0.35f),
                (int) (Color.blue(c) * 0.35f));
    }

    private void drawTile(Canvas canvas, Tile t) {
        float left = t.lane * laneWidth + 6;
        float right = t.lane * laneWidth + laneWidth - 6;
        float top = t.y + 5;
        float bottom = t.y + t.height - 5;
        float r = laneWidth * 0.12f;

        int color;
        if (t == missedTile) color = Color.RED;
        else if (t.isHold) color = AMBER;
        else color = (t.lane % 2 == 0) ? CYAN : MAGENTA;

        rect.set(left, top, right, bottom);

        // tapped normal tiles and finished hold tiles fade out
        if ((t.tapped && !t.isHold) || t.completed) {
            fillPaint.setColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            return;
        }

        if (t.isHold && !t.holding) {
            // untouched hold tile: solid glowing colour
            fillPaint.setColor(color);
            canvas.drawRoundRect(rect, r, r, fillPaint);
        } else {
            // normal tile, or a hold tile being held: dark body with bright outline
            fillPaint.setColor(darken(color));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            if (t.isHold && t.holding) {
                // bright part = the section that still has to be held
                float brightBottom = Math.min(bottom, t.touchY);
                if (brightBottom > top) {
                    fillPaint.setColor(color);
                    rect.set(left, top, right, brightBottom);
                    canvas.drawRoundRect(rect, r, r, fillPaint);
                    rect.set(left, top, right, bottom);
                }
            }
            strokePaint.setColor(color);
            canvas.drawRoundRect(rect, r, r, strokePaint);
        }

        // white bar down the middle of hold tiles
        if (t.isHold) {
            float cx = (left + right) / 2f;
            fillPaint.setColor(Color.argb(210, 255, 255, 255));
            rect.set(cx - 6, top + 30, cx + 6, bottom - 30);
            canvas.drawRoundRect(rect, 6f, 6f, fillPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        long now = System.nanoTime();
        float dt = (lastTime == 0) ? 0f : (now - lastTime) / 1_000_000_000f;
        if (dt > 0.05f) dt = 0.05f;
        lastTime = now;

        if (running && !gameOver && laneWidth > 0) update(dt);

        for (int i = 1; i < LANES; i++) {
            canvas.drawLine(i * laneWidth, 0, i * laneWidth, getHeight(), linePaint);
        }

        for (Tile t : tiles) drawTile(canvas, t);

        // score
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(96f);
        canvas.drawText(String.valueOf(score), getWidth() / 2f, 150f, textPaint);

        // game over screen
        if (gameOver) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(110f);
            canvas.drawText("Game Over", cx, cy - 80f, textPaint);
            textPaint.setColor(CYAN);
            textPaint.setTextSize(80f);
            canvas.drawText("Score: " + score, cx, cy + 40f, textPaint);
            textPaint.setColor(Color.LTGRAY);
            textPaint.setTextSize(55f);
            canvas.drawText("Tap to restart", cx, cy + 160f, textPaint);
        }

        if (running && !gameOver) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int idx = e.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            // on the game over screen, a tap restarts (small delay avoids accidental taps)
            if (gameOver) {
                if (System.currentTimeMillis() - gameOverTime > 600) restart();
                return true;
            }

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
                        soundPool.play(tapSound, 1f, 1f, 1, 0, 1f);
                        if (t.isHold) {
                            t.holding = true;
                            t.pointerId = e.getPointerId(idx);
                            // the tail must travel at least 80% of the tile's length,
                            // so tapping the top of a hold tile doesn't skip the hold
                            t.touchY = Math.max(y, t.y + t.height * 0.8f);
                        }
                    }
                    break;
                }
            }

            // tapping where there is no tile ends the game
            if (!hit) endGame(null);
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_CANCEL) {
            if (!gameOver) {
                int pid = e.getPointerId(idx);
                for (Tile t : tiles) {
                    // letting go of a hold tile too early ends the game
                    if (t.holding && (action == MotionEvent.ACTION_CANCEL || t.pointerId == pid)) {
                        endGame(t);
                        break;
                    }
                }
            }
            return true;
        }

        return true;
    }
}