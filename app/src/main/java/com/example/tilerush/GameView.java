package com.example.tilerush;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    public interface Listener {
        void onGameOver(int score);
        void onRestart();
    }
    public Listener listener;

    private static final int LANES = 4;
    private static final int CYAN = Color.rgb(0, 225, 255);
    private static final int MAGENTA = Color.rgb(255, 70, 200);
    private static final int AMBER = Color.rgb(255, 190, 40);

    private final Paint bgPaint = new Paint();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint();
    private final RectF rect = new RectF();
    private final List<Tile> tiles = new ArrayList<>();
    private final Random random = new Random();

    private float laneWidth, tileHeight, baseSpeed;
    private long lastTime = 0;
    private float spawnDist = 0;
    private int score = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private Tile missedTile = null;

    private SoundPool soundPool;
    private int tapSound;
    private MediaPlayer bgMusic;

    public GameView(Context context, MediaPlayer bgMusic) {
        super(context);
        this.bgMusic = bgMusic;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); // needed for the glow effect below

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5f);
        linePaint.setColor(Color.argb(60, 130, 210, 255));
        linePaint.setStrokeWidth(3f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        glowPaint.setTextAlign(Paint.Align.CENTER);
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

    public void restartFromOutside() {
        restart();
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
        if (listener != null) listener.onRestart();
    }

    private void endGame(Tile missed) {
        gameOver = true;
        missedTile = missed;
        if (bgMusic != null && bgMusic.isPlaying()) bgMusic.pause();
        if (listener != null) listener.onGameOver(score);
    }

    private void spawnTile() {
        int lane = random.nextInt(LANES);
        boolean hold = score >= 5 && random.nextFloat() < 0.25f;
        int rows = hold ? 2 + random.nextInt(2) : 1;
        float h = rows * tileHeight;
        tiles.add(new Tile(lane, -h + spawnDist, h, hold));
        spawnDist -= (rows - 1) * tileHeight;
    }

    private void update(float dt) {
        float speed = baseSpeed * (1f + Math.min(score, 100) * 0.01f);

        for (Tile t : tiles) {
            t.y += speed * dt;
            if (t.holding && t.y >= t.touchY) {
                t.holding = false;
                t.completed = true;
                score += 2;
            }
        }

        for (Tile t : tiles) {
            if (!t.tapped && t.y + t.height >= getHeight()) {
                endGame(t);
                return;
            }
        }

        Iterator<Tile> it = tiles.iterator();
        while (it.hasNext()) {
            if (it.next().y > getHeight()) it.remove();
        }

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

        if ((t.tapped && !t.isHold) || t.completed) {
            fillPaint.setColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            return;
        }

        if (t.isHold && !t.holding) {
            fillPaint.setColor(color);
            canvas.drawRoundRect(rect, r, r, fillPaint);
        } else {
            fillPaint.setColor(darken(color));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            if (t.isHold && t.holding) {
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

        if (t.isHold) {
            float cx = (left + right) / 2f;
            fillPaint.setColor(Color.argb(210, 255, 255, 255));
            rect.set(cx - 6, top + 30, cx + 6, bottom - 30);
            canvas.drawRoundRect(rect, 6f, 6f, fillPaint);
        }
    }

    // glowing text: a blurred colored pass behind a crisp white pass
    private void glowText(Canvas canvas, String text, float x, float y, float size, int color) {
        textPaint.setTextSize(size);
        glowPaint.setTextSize(size);
        glowPaint.setColor(color);
        glowPaint.setMaskFilter(new BlurMaskFilter(size * 0.35f, BlurMaskFilter.Blur.NORMAL));
        canvas.drawText(text, x, y, glowPaint);
        canvas.drawText(text, x, y, textPaint);
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

        glowText(canvas, String.valueOf(score), getWidth() / 2f, 150f, 90f, CYAN);

        if (gameOver) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            glowText(canvas, "GAME OVER", cx, cy - 60f, 100f, MAGENTA);
            glowText(canvas, "Score: " + score, cx, cy + 60f, 64f, CYAN);
        }

        if (running && !gameOver) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int idx = e.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (gameOver) {
                // tap-to-restart removed on purpose — the Restart button handles this now
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
                            t.touchY = Math.max(y, t.y + t.height * 0.8f);
                        }
                    }
                    break;
                }
            }

            if (!hit) endGame(null);
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_CANCEL) {
            if (!gameOver) {
                int pid = e.getPointerId(idx);
                for (Tile t : tiles) {
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

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}