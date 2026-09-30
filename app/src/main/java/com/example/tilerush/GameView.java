package com.example.tilerush;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
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
    private static final int PURPLE = Color.rgb(185, 80, 255);
    private static final int AMBER = Color.rgb(255, 195, 45);
    private static final int RED = Color.rgb(255, 45, 75);

    private static class HitRipple {
        float x, y;
        float radius = 10f;
        float maxRadius = 85f;
        int color;
        float alpha = 1f;
    }

    private static class HitParticle {
        float x, y;
        float vx, vy;
        float size;
        int color;
        float alpha = 1f;
    }

    private static class HitPopup {
        float x, y;
        String text;
        int color;
        float alpha = 1f;
        float vy = -110f;
    }

    private final Paint bgPaint = new Paint();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint();
    private final Paint popupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint laneFlashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint judgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimSpecularPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint levelBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bannerTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bannerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bannerSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint = new Paint();

    private final RectF rect = new RectF();
    private final RectF rectInner = new RectF();
    private final Matrix shaderMatrix = new Matrix();
    private final Path diamondPath = new Path();

    private LinearGradient cyanShader;
    private LinearGradient magentaShader;
    private LinearGradient purpleShader;
    private LinearGradient amberShader;
    private LinearGradient redShader;
    private LinearGradient holdTrackShader;

    private final List<Tile> tiles = new ArrayList<>();
    private final List<HitRipple> ripples = new ArrayList<>();
    private final List<HitParticle> particles = new ArrayList<>();
    private final List<HitPopup> popups = new ArrayList<>();
    private final List<Integer> zigzagQueue = new ArrayList<>();
    private final float[] lanePress = new float[LANES];
    private final Random random = new Random();

    private float laneWidth, tileHeight, baseSpeed;
    private long lastTime = 0;
    private float spawnDist = 0;
    private int score = 0;
    private boolean running = false;
    private boolean gameOver = false;
    private Tile missedTile = null;

    // Levels & transitions
    private int currentLevel = 1; // 1 = Normal, 2 = Rush, 3 = Hard (Zigzag)
    private String bannerTitle = null;
    private String bannerSub = null;
    private int bannerColor = CYAN;
    private float bannerTimer = 0f;
    private float screenFlashAlpha = 0f;

    private SoundPool soundPool;
    private int tapSound;
    private int errorSound;
    private MediaPlayer bgMusic;
    private boolean muted = false;

    public GameView(Context context, MediaPlayer bgMusic) {
        super(context);
        this.bgMusic = bgMusic;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        strokePaint.setStyle(Paint.Style.STROKE);
        innerStrokePaint.setStyle(Paint.Style.STROKE);
        rimGlowPaint.setStyle(Paint.Style.STROKE);
        rimSpecularPaint.setStyle(Paint.Style.STROKE);
        rimSpecularPaint.setStrokeCap(Paint.Cap.ROUND);
        rimSpecularPaint.setColor(Color.WHITE);

        linePaint.setColor(Color.argb(45, 120, 200, 255));
        linePaint.setStrokeWidth(2f);

        judgePaint.setStyle(Paint.Style.STROKE);
        judgePaint.setStrokeWidth(2.5f);
        judgePaint.setColor(Color.argb(75, 0, 225, 255));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setColor(Color.argb(180, 160, 220, 255));
        subTextPaint.setTextSize(dpToPx(11));
        subTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        subTextPaint.setLetterSpacing(0.18f);

        glowPaint.setTextAlign(Paint.Align.CENTER);
        glowPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        popupPaint.setTextAlign(Paint.Align.CENTER);
        popupPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));

        levelBadgePaint.setTextAlign(Paint.Align.CENTER);
        levelBadgePaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        levelBadgePaint.setLetterSpacing(0.12f);

        bannerTitlePaint.setTextAlign(Paint.Align.CENTER);
        bannerTitlePaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        bannerTitlePaint.setLetterSpacing(0.12f);

        bannerGlowPaint.setTextAlign(Paint.Align.CENTER);
        bannerGlowPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        bannerGlowPaint.setLetterSpacing(0.12f);

        bannerSubPaint.setTextAlign(Paint.Align.CENTER);
        bannerSubPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        bannerSubPaint.setLetterSpacing(0.14f);

        flashPaint.setStyle(Paint.Style.FILL);
        overlayPaint.setColor(Color.argb(215, 8, 6, 28));

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build();
        try {
            tapSound = soundPool.load(context, R.raw.tap, 1);
            errorSound = soundPool.load(context, R.raw.error, 1);
        } catch (Exception ignored) {}
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    private void playSound(int soundId) {
        if (!muted && soundPool != null && soundId != 0) {
            try {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
            } catch (Exception ignored) {}
        }
    }

    private LinearGradient createTileShader(int color, float height) {
        int cr = Color.red(color);
        int cg = Color.green(color);
        int cb = Color.blue(color);

        int cTop = Color.argb(240,
                Math.min(255, (int)(cr * 0.15f) + 10),
                Math.min(255, (int)(cg * 0.15f) + 10),
                Math.min(255, (int)(cb * 0.15f) + 24));

        int cMid = Color.argb(235,
                Math.min(255, (int)(cr * 0.55f) + 8),
                Math.min(255, (int)(cg * 0.55f) + 8),
                Math.min(255, (int)(cb * 0.55f) + 16));

        int cBottom = Color.argb(255, cr, cg, cb);

        return new LinearGradient(0, 0, 0, height,
                new int[]{cTop, cMid, cBottom},
                new float[]{0f, 0.65f, 1.0f},
                Shader.TileMode.CLAMP);
    }

    private LinearGradient createHoldTrackShader(int color, float height) {
        int cr = Color.red(color);
        int cg = Color.green(color);
        int cb = Color.blue(color);

        int cTop = Color.argb(55, cr, cg, cb);
        int cMid = Color.argb(105, cr, cg, cb);
        int cBottom = Color.argb(175, cr, cg, cb);

        return new LinearGradient(0, 0, 0, height,
                new int[]{cTop, cMid, cBottom},
                new float[]{0f, 0.70f, 1.0f},
                Shader.TileMode.CLAMP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        laneWidth = w / (float) LANES;
        tileHeight = h / 4f;
        baseSpeed = h * 0.58f;

        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                Color.rgb(10, 10, 34), Color.rgb(56, 18, 112), Shader.TileMode.CLAMP));

        cyanShader = createTileShader(CYAN, tileHeight);
        magentaShader = createTileShader(MAGENTA, tileHeight);
        purpleShader = createTileShader(PURPLE, tileHeight);
        amberShader = createTileShader(AMBER, tileHeight);
        redShader = createTileShader(RED, tileHeight);
        holdTrackShader = createHoldTrackShader(AMBER, h);
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

    public boolean isGameOver() {
        return gameOver;
    }

    private void restart() {
        tiles.clear();
        ripples.clear();
        particles.clear();
        popups.clear();
        zigzagQueue.clear();
        for (int i = 0; i < LANES; i++) lanePress[i] = 0f;
        score = 0;
        spawnDist = 0;
        gameOver = false;
        missedTile = null;
        lastTime = 0;
        currentLevel = 1;
        bannerTitle = null;
        bannerTimer = 0f;
        screenFlashAlpha = 0f;

        try {
            if (bgMusic != null) {
                bgMusic.seekTo(0);
                if (!bgMusic.isPlaying()) bgMusic.start();
            }
        } catch (Exception ignored) {}
        postInvalidateOnAnimation();
        if (listener != null) listener.onRestart();
    }

    private void endGame(Tile missed) {
        if (gameOver) return;
        gameOver = true;
        missedTile = missed;

        try {
            if (bgMusic != null && bgMusic.isPlaying()) {
                bgMusic.pause();
            }
        } catch (Exception ignored) {}

        playSound(errorSound);

        if (listener != null) {
            listener.onGameOver(score);
        }
    }

    private void triggerLevelTransition(String title, String subtitle, int color) {
        bannerTitle = title;
        bannerSub = subtitle;
        bannerColor = color;
        bannerTimer = 1.9f;
        screenFlashAlpha = 0.40f;
        playSound(tapSound);
    }

    private void spawnTile() {
        int lane;
        boolean hold = false;

        // Level 3 (Hard Mode): Generate fluid zigzag staircase waves
        if (currentLevel >= 3) {
            if (zigzagQueue.isEmpty() && random.nextFloat() < 0.65f) {
                boolean leftToRight = random.nextBoolean();
                if (leftToRight) {
                    int[] pattern = {0, 1, 2, 3, 2, 1, 0};
                    for (int p : pattern) zigzagQueue.add(p);
                } else {
                    int[] pattern = {3, 2, 1, 0, 1, 2, 3};
                    for (int p : pattern) zigzagQueue.add(p);
                }
            }

            if (!zigzagQueue.isEmpty()) {
                lane = zigzagQueue.remove(0);
                hold = false; // Fast single-tap cascading staircase
            } else {
                lane = random.nextInt(LANES);
                hold = random.nextFloat() < 0.20f;
            }
        } else {
            // Level 1 or 2
            lane = random.nextInt(LANES);
            hold = (score >= 5) && (random.nextFloat() < (currentLevel == 2 ? 0.30f : 0.22f));
        }

        int rows = hold ? 2 + random.nextInt(2) : 1;
        float h = rows * tileHeight;
        tiles.add(new Tile(lane, -h + spawnDist, h, hold));
        spawnDist -= (rows - 1) * tileHeight;
    }

    private void spawnBubblePopEffects(float x, float y, int color, boolean isHold) {
        HitRipple ripple = new HitRipple();
        ripple.x = x;
        ripple.y = y;
        ripple.color = color;
        ripple.radius = 10f;
        ripple.maxRadius = isHold ? 95f : 78f;
        ripples.add(ripple);

        int count = isHold ? 14 : 9;
        for (int i = 0; i < count; i++) {
            HitParticle p = new HitParticle();
            p.x = x + (random.nextFloat() - 0.5f) * 26f;
            p.y = y + (random.nextFloat() - 0.5f) * 16f;
            float angle = (float) (random.nextFloat() * Math.PI * 2.0);
            float speed = 120f + random.nextFloat() * 260f;
            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed - 50f;
            p.size = 4f + random.nextFloat() * 4.5f;
            p.color = (random.nextBoolean()) ? Color.WHITE : color;
            particles.add(p);
        }

        HitPopup pop = new HitPopup();
        pop.x = x;
        pop.y = y - 18f;
        pop.text = isHold ? "+2 PERFECT!" : "+1";
        pop.color = isHold ? AMBER : color;
        popups.add(pop);

        if (particles.size() > 60) particles.subList(0, particles.size() - 60).clear();
        if (ripples.size() > 15) ripples.subList(0, ripples.size() - 15).clear();
        if (popups.size() > 10) popups.subList(0, popups.size() - 10).clear();
    }

    private void update(float dt) {
        // Detect round / level progression
        int newLevel;
        if (score < 30) {
            newLevel = 1;
        } else if (score < 70) {
            newLevel = 2;
        } else {
            newLevel = 3;
        }

        if (newLevel != currentLevel) {
            if (newLevel == 2) {
                triggerLevelTransition("SPEED UP!", "LEVEL 2 • TURBO RUSH", CYAN);
            } else if (newLevel == 3) {
                triggerLevelTransition("OVERDRIVE!", "HARD LEVEL • ZIGZAG MANIA", Color.rgb(255, 65, 95));
            }
            currentLevel = newLevel;
        }

        // Speed scaling by mode
        float speed;
        if (currentLevel == 1) {
            speed = baseSpeed * (1f + score * 0.008f);
        } else if (currentLevel == 2) {
            speed = baseSpeed * (1.38f + (score - 30) * 0.008f);
        } else {
            speed = baseSpeed * (1.75f + Math.min(score - 70, 80) * 0.006f);
        }

        // Update banner and flash timers
        if (bannerTimer > 0f) {
            bannerTimer -= dt;
            if (bannerTimer <= 0f) {
                bannerTitle = null;
            }
        }
        if (screenFlashAlpha > 0f) {
            screenFlashAlpha = Math.max(0f, screenFlashAlpha - dt * 2.2f);
        }

        for (Tile t : tiles) {
            t.y += speed * dt;
            if (t.tapped && !t.isHold) {
                t.tapAnim += dt * 5.5f;
            }
            if (t.holding) {
                t.tapAnim += dt * 3.0f;
                if (random.nextFloat() < 0.38f) {
                    HitParticle p = new HitParticle();
                    float cx = t.lane * laneWidth + laneWidth / 2f;
                    p.x = cx + (random.nextFloat() - 0.5f) * 34f;
                    p.y = t.touchY + (random.nextFloat() - 0.5f) * 16f;
                    p.vx = (random.nextFloat() - 0.5f) * 100f;
                    p.vy = -60f - random.nextFloat() * 100f;
                    p.size = 3.5f + random.nextFloat() * 4f;
                    p.color = (random.nextBoolean()) ? Color.WHITE : AMBER;
                    particles.add(p);
                }

                // When the tail reaches the player's finger, hold is complete!
                if (t.y >= t.touchY) {
                    t.holding = false;
                    t.completed = true;
                    score += 2;
                    float cx = t.lane * laneWidth + laneWidth / 2f;
                    spawnBubblePopEffects(cx, t.touchY, AMBER, true);
                }
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
            Tile t = it.next();
            if (t.y > getHeight() || (t.tapped && !t.isHold && t.tapAnim >= 1f) || (t.completed && t.tapAnim >= 1f)) {
                it.remove();
            }
        }

        spawnDist += speed * dt;
        while (spawnDist >= tileHeight) {
            spawnDist -= tileHeight;
            spawnTile();
        }

        Iterator<HitRipple> ripIt = ripples.iterator();
        while (ripIt.hasNext()) {
            HitRipple r = ripIt.next();
            r.radius += dt * 260f;
            r.alpha -= dt * 3.6f;
            if (r.alpha <= 0f || r.radius >= r.maxRadius) ripIt.remove();
        }

        Iterator<HitParticle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            HitParticle p = partIt.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy += 320f * dt;
            p.alpha -= dt * 2.8f;
            if (p.alpha <= 0f) partIt.remove();
        }

        Iterator<HitPopup> popIt = popups.iterator();
        while (popIt.hasNext()) {
            HitPopup pop = popIt.next();
            pop.y += pop.vy * dt;
            pop.alpha -= dt * 2.4f;
            if (pop.alpha <= 0f) popIt.remove();
        }

        for (int i = 0; i < LANES; i++) {
            if (lanePress[i] > 0f) {
                lanePress[i] = Math.max(0f, lanePress[i] - dt * 4.5f);
            }
        }
    }

    private int getTileColor(Tile t) {
        if (t == missedTile) return RED;
        if (t.isHold) return AMBER;
        switch (t.lane) {
            case 0: return CYAN;
            case 1: return MAGENTA;
            case 2: return PURPLE;
            default: return CYAN;
        }
    }

    private LinearGradient getShaderForColor(int color) {
        if (color == RED) return redShader;
        if (color == AMBER) return amberShader;
        if (color == MAGENTA) return magentaShader;
        if (color == PURPLE) return purpleShader;
        return cyanShader;
    }

    private void drawTile(Canvas canvas, Tile t) {
        float left = t.lane * laneWidth + dpToPx(4.5f);
        float right = t.lane * laneWidth + laneWidth - dpToPx(4.5f);
        float top = t.y + dpToPx(2.5f);
        float bottom = t.y + t.height - dpToPx(2.5f);
        float r = dpToPx(13f);

        int color = getTileColor(t);

        float alphaMult = 1f;
        if (t.tapped && !t.isHold) {
            alphaMult = Math.max(0f, 1f - t.tapAnim);
            if (alphaMult <= 0.01f) return;

            // Bubble pop scale expansion
            float popScale = t.tapAnim * dpToPx(5);
            left -= popScale;
            right += popScale;
            top -= popScale;
            bottom += popScale;
        }

        rect.set(left, top, right, bottom);

        if (t.isHold) {
            drawHoldTile(canvas, t, left, top, right, bottom, r, color);
        } else {
            drawTapTile(canvas, t, left, top, right, bottom, r, color, alphaMult);
        }
    }

    private void drawTapTile(Canvas canvas, Tile t, float left, float top, float right, float bottom, float r, int color, float alphaMult) {
        boolean isFlashing = t.tapped && t.tapAnim < 0.20f;

        // 1. Sleek Gradient Crystal Body
        if (isFlashing) {
            fillPaint.setColor(Color.WHITE);
            fillPaint.setAlpha((int)(250 * alphaMult));
            canvas.drawRoundRect(rect, r, r, fillPaint);
        } else {
            LinearGradient shader = getShaderForColor(color);
            if (shader != null) {
                shaderMatrix.setTranslate(0, top);
                shader.setLocalMatrix(shaderMatrix);
                fillPaint.setShader(shader);
            } else {
                fillPaint.setColor(color);
            }
            fillPaint.setAlpha((int)(245 * alphaMult));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            fillPaint.setShader(null);
        }

        // 2. Outer Soft Neon Glow Rim
        rimGlowPaint.setColor(isFlashing ? Color.WHITE : color);
        rimGlowPaint.setAlpha((int)(95 * alphaMult));
        rimGlowPaint.setStrokeWidth(dpToPx(5f));
        canvas.drawRoundRect(rect, r, r, rimGlowPaint);

        // 3. Crisp Primary Neon Rim Frame
        strokePaint.setColor(isFlashing ? Color.WHITE : color);
        strokePaint.setAlpha((int)(255 * alphaMult));
        strokePaint.setStrokeWidth(isFlashing ? dpToPx(4f) : dpToPx(2.8f));
        canvas.drawRoundRect(rect, r, r, strokePaint);

        // 4. Inner Luminous Specular Chamfer
        float inset = dpToPx(2.2f);
        rectInner.set(left + inset, top + inset, right - inset, bottom - inset);
        innerStrokePaint.setColor(Color.WHITE);
        innerStrokePaint.setAlpha((int)(115 * alphaMult));
        innerStrokePaint.setStrokeWidth(dpToPx(1.3f));
        canvas.drawRoundRect(rectInner, Math.max(2f, r - inset), Math.max(2f, r - inset), innerStrokePaint);

        // 5. Polished Top Rim Glisten
        rimSpecularPaint.setAlpha((int)(225 * alphaMult));
        rimSpecularPaint.setStrokeWidth(dpToPx(2.2f));
        canvas.drawLine(left + r * 0.75f, top + dpToPx(1.5f), right - r * 0.75f, top + dpToPx(1.5f), rimSpecularPaint);

        // 6. Radiant Leading Contact Bar (at bottom strike edge)
        float barMargin = dpToPx(10f);
        rectInner.set(left + barMargin, bottom - dpToPx(7f), right - barMargin, bottom - dpToPx(2.5f));
        fillPaint.setColor(color);
        fillPaint.setAlpha((int)(230 * alphaMult));
        canvas.drawRoundRect(rectInner, dpToPx(2.5f), dpToPx(2.5f), fillPaint);

        rectInner.set(left + barMargin + dpToPx(6f), bottom - dpToPx(6.5f), right - barMargin - dpToPx(6f), bottom - dpToPx(3f));
        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha((int)(245 * alphaMult));
        canvas.drawRoundRect(rectInner, dpToPx(1.5f), dpToPx(1.5f), fillPaint);

        // 7. Minimalist Rhythm Diamond Accent
        float cx = (left + right) / 2f;
        float cy = (top + bottom) / 2f;
        float dSize = dpToPx(7f);

        diamondPath.reset();
        diamondPath.moveTo(cx, cy - dSize);
        diamondPath.lineTo(cx + dSize, cy);
        diamondPath.lineTo(cx, cy + dSize);
        diamondPath.lineTo(cx - dSize, cy);
        diamondPath.close();

        fillPaint.setColor(color);
        fillPaint.setAlpha((int)(200 * alphaMult));
        canvas.drawPath(diamondPath, fillPaint);

        diamondPath.reset();
        diamondPath.moveTo(cx, cy - (dSize - dpToPx(2.2f)));
        diamondPath.lineTo(cx + (dSize - dpToPx(2.2f)), cy);
        diamondPath.lineTo(cx, cy + (dSize - dpToPx(2.2f)));
        diamondPath.lineTo(cx - (dSize - dpToPx(2.2f)), cy);
        diamondPath.close();

        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha((int)(240 * alphaMult));
        canvas.drawPath(diamondPath, fillPaint);
    }

    private void drawHoldTile(Canvas canvas, Tile t, float left, float top, float right, float bottom, float r, int color) {
        float cx = (left + right) / 2f;
        float headH = tileHeight * 0.70f;
        float inset = dpToPx(2.2f);

        if (t.completed) {
            float fade = Math.max(0f, 1f - t.tapAnim);
            if (fade <= 0.01f) return;
            fillPaint.setColor(Color.argb((int)(35 * fade), Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawRoundRect(rect, r, r, fillPaint);
            return;
        }

        // Active bottom edge of unplayed tile:
        // When holding, the tile below the finger is consumed, so unplayed track stops at t.touchY!
        float activeBottom = bottom;
        if (t.holding) {
            activeBottom = Math.min(bottom, t.touchY);
        }

        if (activeBottom <= top) return;

        rect.set(left, top, right, activeBottom);

        // 1. Sustain Ribbon Track (gradient translucent body)
        if (holdTrackShader != null) {
            shaderMatrix.setTranslate(0, top);
            holdTrackShader.setLocalMatrix(shaderMatrix);
            fillPaint.setShader(holdTrackShader);
        } else {
            fillPaint.setColor(Color.argb(80, 255, 195, 45));
        }
        canvas.drawRoundRect(rect, r, r, fillPaint);
        fillPaint.setShader(null);

        // 2. Track Outer Glow Rim
        rimGlowPaint.setColor(color);
        rimGlowPaint.setAlpha(85);
        rimGlowPaint.setStrokeWidth(dpToPx(5f));
        canvas.drawRoundRect(rect, r, r, rimGlowPaint);

        // 3. Track Primary Neon Rim Frame
        strokePaint.setColor(color);
        strokePaint.setAlpha(245);
        strokePaint.setStrokeWidth(dpToPx(2.8f));
        canvas.drawRoundRect(rect, r, r, strokePaint);

        // 4. Track Inner Chamfer
        rectInner.set(left + inset, top + inset, right - inset, activeBottom - inset);
        innerStrokePaint.setColor(Color.WHITE);
        innerStrokePaint.setAlpha(85);
        innerStrokePaint.setStrokeWidth(dpToPx(1.3f));
        canvas.drawRoundRect(rectInner, Math.max(2f, r - inset), Math.max(2f, r - inset), innerStrokePaint);

        // 5. Central luminous laser conduit
        rectInner.set(cx - dpToPx(7f), top + dpToPx(10f), cx + dpToPx(7f), activeBottom - dpToPx(10f));
        if (rectInner.bottom > rectInner.top) {
            fillPaint.setColor(Color.argb(85, 255, 215, 80));
            canvas.drawRoundRect(rectInner, dpToPx(7f), dpToPx(7f), fillPaint);

            rectInner.set(cx - dpToPx(2.5f), top + dpToPx(12f), cx + dpToPx(2.5f), activeBottom - dpToPx(12f));
            fillPaint.setColor(Color.argb(190, 255, 255, 240));
            canvas.drawRoundRect(rectInner, dpToPx(2.5f), dpToPx(2.5f), fillPaint);
        }

        // 6. Subtle rhythm node diamonds along the unplayed ribbon
        float nodeY = top + dpToPx(35f);
        while (nodeY < activeBottom - headH - dpToPx(15f)) {
            diamondPath.reset();
            diamondPath.moveTo(cx, nodeY - dpToPx(4.5f));
            diamondPath.lineTo(cx + dpToPx(4.5f), nodeY);
            diamondPath.lineTo(cx, nodeY + dpToPx(4.5f));
            diamondPath.lineTo(cx - dpToPx(4.5f), nodeY);
            diamondPath.close();

            fillPaint.setColor(Color.argb(190, 255, 245, 190));
            canvas.drawPath(diamondPath, fillPaint);
            nodeY += dpToPx(50f);
        }

        // 7. Dynamic Head Note Block
        // When not holding: head note is at [bottom - headH, bottom]
        // When holding: head note is locked right to the player's finger [activeBottom - headH, activeBottom]!
        float headTop = Math.max(top, activeBottom - headH);
        rectInner.set(left, headTop, right, activeBottom);

        if (amberShader != null) {
            shaderMatrix.setTranslate(0, headTop);
            amberShader.setLocalMatrix(shaderMatrix);
            fillPaint.setShader(amberShader);
        } else {
            fillPaint.setColor(color);
        }
        canvas.drawRoundRect(rectInner, r, r, fillPaint);
        fillPaint.setShader(null);

        // Head Outer Glow Rim
        rimGlowPaint.setColor(color);
        rimGlowPaint.setAlpha(t.holding ? 255 : 240);
        rimGlowPaint.setStrokeWidth(dpToPx(t.holding ? 6f : 5f));
        canvas.drawRoundRect(rectInner, r, r, rimGlowPaint);

        // Head Primary Neon Rim Frame
        strokePaint.setColor(t.holding ? Color.WHITE : color);
        strokePaint.setAlpha(255);
        strokePaint.setStrokeWidth(dpToPx(2.8f));
        canvas.drawRoundRect(rectInner, r, r, strokePaint);

        // Head Inner Chamfer
        rect.set(left + inset, headTop + inset, right - inset, activeBottom - inset);
        innerStrokePaint.setColor(Color.WHITE);
        innerStrokePaint.setAlpha(125);
        innerStrokePaint.setStrokeWidth(dpToPx(1.3f));
        canvas.drawRoundRect(rect, Math.max(2f, r - inset), Math.max(2f, r - inset), innerStrokePaint);

        // Head Polished Top Rim Glisten
        rimSpecularPaint.setAlpha(225);
        rimSpecularPaint.setStrokeWidth(dpToPx(2.2f));
        canvas.drawLine(left + r * 0.75f, headTop + dpToPx(1.5f), right - r * 0.75f, headTop + dpToPx(1.5f), rimSpecularPaint);

        // Head Leading Hit Bar
        float barMargin = dpToPx(10f);
        rect.set(left + barMargin, activeBottom - dpToPx(7f), right - barMargin, activeBottom - dpToPx(2.5f));
        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha(240);
        canvas.drawRoundRect(rect, dpToPx(2f), dpToPx(2f), fillPaint);

        // Head Center Diamond
        float headCy = (headTop + activeBottom) / 2f;
        float dSize = dpToPx(7.5f);
        diamondPath.reset();
        diamondPath.moveTo(cx, headCy - dSize);
        diamondPath.lineTo(cx + dSize, headCy);
        diamondPath.lineTo(cx, headCy + dSize);
        diamondPath.lineTo(cx - dSize, headCy);
        diamondPath.close();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha(240);
        canvas.drawPath(diamondPath, fillPaint);

        // 8. Electric Contact Plasma Orb under the finger when actively holding
        if (t.holding) {
            fillPaint.setColor(Color.argb(90, 255, 255, 255));
            canvas.drawCircle(cx, activeBottom, dpToPx(18f), fillPaint);
            fillPaint.setColor(Color.WHITE);
            canvas.drawCircle(cx, activeBottom, dpToPx(10f), fillPaint);
            fillPaint.setColor(color);
            canvas.drawCircle(cx, activeBottom, dpToPx(6f), fillPaint);
        }
    }

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

        // 1. Ambient Screen Flash on Level Up
        if (screenFlashAlpha > 0f) {
            flashPaint.setColor(bannerColor);
            flashPaint.setAlpha((int)(screenFlashAlpha * 255));
            canvas.drawRect(0, 0, getWidth(), getHeight(), flashPaint);
        }

        // 2. Lane Press Illumination
        for (int i = 0; i < LANES; i++) {
            if (lanePress[i] > 0f) {
                int c = (i == 0 || i == 3) ? CYAN : (i == 1 ? MAGENTA : PURPLE);
                laneFlashPaint.setColor(Color.argb((int)(lanePress[i] * 38), Color.red(c), Color.green(c), Color.blue(c)));
                canvas.drawRect(i * laneWidth, 0, (i + 1) * laneWidth, getHeight(), laneFlashPaint);
            }
        }

        // 3. Lane Dividers
        for (int i = 1; i < LANES; i++) {
            canvas.drawLine(i * laneWidth, 0, i * laneWidth, getHeight(), linePaint);
        }

        // 4. Judgment Baseline & Target Pads
        float judgeY = getHeight() - tileHeight * 0.85f;
        canvas.drawLine(0, judgeY, getWidth(), judgeY, judgePaint);

        for (int i = 0; i < LANES; i++) {
            float l = i * laneWidth + 14f;
            float r = (i + 1) * laneWidth - 14f;
            rectInner.set(l, judgeY - 5.5f, r, judgeY + 5.5f);
            int c = (i == 0 || i == 3) ? CYAN : (i == 1 ? MAGENTA : PURPLE);
            strokePaint.setColor(c);
            strokePaint.setAlpha(lanePress[i] > 0f ? 220 : 65);
            strokePaint.setStrokeWidth(2f);
            canvas.drawRoundRect(rectInner, 6f, 6f, strokePaint);
        }

        // 5. Tiles
        for (Tile t : tiles) drawTile(canvas, t);

        // 6. Dual Bubble Pop Ripples
        for (HitRipple rip : ripples) {
            float a = Math.max(0f, Math.min(1f, rip.alpha));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setColor(rip.color);
            strokePaint.setAlpha((int) (a * 255));
            strokePaint.setStrokeWidth(3.8f * a);
            canvas.drawCircle(rip.x, rip.y, rip.radius, strokePaint);

            strokePaint.setColor(Color.WHITE);
            strokePaint.setAlpha((int) (a * 150));
            strokePaint.setStrokeWidth(1.8f * a);
            canvas.drawCircle(rip.x, rip.y, Math.max(1f, rip.radius - 4f), strokePaint);
        }

        // 7. Sparkling Bubble Droplets
        fillPaint.setStyle(Paint.Style.FILL);
        for (HitParticle p : particles) {
            float a = Math.max(0f, Math.min(1f, p.alpha));
            fillPaint.setColor(p.color);
            fillPaint.setAlpha((int) (a * 240));
            canvas.drawCircle(p.x, p.y, p.size * a, fillPaint);

            fillPaint.setColor(Color.WHITE);
            fillPaint.setAlpha((int) (a * 210));
            canvas.drawCircle(p.x - p.size * 0.25f, p.y - p.size * 0.25f, Math.max(1f, p.size * 0.45f * a), fillPaint);
        }

        // 8. Floating Score Popups
        for (HitPopup pop : popups) {
            popupPaint.setColor(pop.color);
            popupPaint.setAlpha((int) (Math.max(0f, Math.min(1f, pop.alpha)) * 255));
            popupPaint.setTextSize(pop.text.contains("PERFECT") ? dpToPx(16) : dpToPx(19));
            canvas.drawText(pop.text, pop.x, pop.y, popupPaint);
        }

        // 9. Score HUD & Mode Badge
        canvas.drawText("SCORE", getWidth() / 2f, dpToPx(28f), subTextPaint);
        glowText(canvas, String.valueOf(score), getWidth() / 2f, dpToPx(72f), dpToPx(40f), CYAN);

        // Level indicator pill badge below score
        float badgeW = dpToPx(98f);
        float badgeH = dpToPx(20f);
        float badgeY = dpToPx(86f);
        rectInner.set(getWidth() / 2f - badgeW / 2f, badgeY, getWidth() / 2f + badgeW / 2f, badgeY + badgeH);

        int lvlColor = (currentLevel == 1) ? CYAN : (currentLevel == 2 ? AMBER : Color.rgb(255, 65, 95));
        String lvlText = (currentLevel == 1) ? "LV.1 NORMAL" : (currentLevel == 2 ? "LV.2 RUSH ⚡" : "LV.3 HARD 🔥");

        fillPaint.setColor(Color.argb(45, Color.red(lvlColor), Color.green(lvlColor), Color.blue(lvlColor)));
        canvas.drawRoundRect(rectInner, dpToPx(10f), dpToPx(10f), fillPaint);

        strokePaint.setColor(lvlColor);
        strokePaint.setAlpha(170);
        strokePaint.setStrokeWidth(dpToPx(1.2f));
        canvas.drawRoundRect(rectInner, dpToPx(10f), dpToPx(10f), strokePaint);

        levelBadgePaint.setColor(lvlColor);
        levelBadgePaint.setTextSize(dpToPx(10.5f));
        canvas.drawText(lvlText, getWidth() / 2f, badgeY + dpToPx(14f), levelBadgePaint);

        // 10. Level Transition Announcement Banner ("SPEED UP!", "OVERDRIVE!")
        if (bannerTitle != null && bannerTimer > 0f) {
            float alpha = Math.min(1f, bannerTimer * 2.2f);
            float bannerY = getHeight() * 0.35f;

            float cardW = getWidth() * 0.84f;
            float cardH = dpToPx(76f);
            rectInner.set(getWidth() / 2f - cardW / 2f, bannerY - dpToPx(38f), getWidth() / 2f + cardW / 2f, bannerY + cardH - dpToPx(38f));

            fillPaint.setColor(Color.argb((int)(175 * alpha), 8, 8, 28));
            canvas.drawRoundRect(rectInner, dpToPx(16f), dpToPx(16f), fillPaint);

            strokePaint.setColor(bannerColor);
            strokePaint.setAlpha((int)(230 * alpha));
            strokePaint.setStrokeWidth(dpToPx(2.5f));
            canvas.drawRoundRect(rectInner, dpToPx(16f), dpToPx(16f), strokePaint);

            // Glowing title
            bannerTitlePaint.setColor(bannerColor);
            bannerTitlePaint.setAlpha((int)(255 * alpha));
            bannerTitlePaint.setTextSize(dpToPx(28f));

            bannerGlowPaint.setColor(bannerColor);
            bannerGlowPaint.setAlpha((int)(190 * alpha));
            bannerGlowPaint.setTextSize(dpToPx(28f));
            bannerGlowPaint.setMaskFilter(new BlurMaskFilter(dpToPx(12f), BlurMaskFilter.Blur.NORMAL));

            canvas.drawText(bannerTitle, getWidth() / 2f, bannerY, bannerGlowPaint);
            canvas.drawText(bannerTitle, getWidth() / 2f, bannerY, bannerTitlePaint);

            // Subtitle
            bannerSubPaint.setColor(Color.WHITE);
            bannerSubPaint.setAlpha((int)(230 * alpha));
            bannerSubPaint.setTextSize(dpToPx(12f));
            canvas.drawText(bannerSub, getWidth() / 2f, bannerY + dpToPx(24f), bannerSubPaint);
        }

        if (gameOver) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        }

        if (running && !gameOver) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        int idx = e.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (gameOver) return true;

            float x = e.getX(idx);
            float y = e.getY(idx);
            int lane = Math.min(LANES - 1, Math.max(0, (int) (x / laneWidth)));

            lanePress[lane] = 1.0f;

            boolean hit = false;
            for (Tile t : tiles) {
                if (t.lane == lane && y >= t.y && y <= t.y + t.height) {
                    hit = true;
                    if (!t.tapped) {
                        t.tapped = true;
                        score++;
                        playSound(tapSound);

                        float cx = lane * laneWidth + laneWidth / 2f;
                        int c = getTileColor(t);
                        spawnBubblePopEffects(cx, y, c, false);

                        if (t.isHold) {
                            t.holding = true;
                            t.pointerId = e.getPointerId(idx);
                            t.touchY = y;
                        }
                    }
                    break;
                }
            }

            if (!hit) endGame(null);
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (!gameOver) {
                int count = e.getPointerCount();
                for (int i = 0; i < count; i++) {
                    int pid = e.getPointerId(i);
                    float py = e.getY(i);
                    for (Tile t : tiles) {
                        if (t.holding && t.pointerId == pid) {
                            t.touchY = py;
                        }
                    }
                }
            }
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
        running = false;
        try {
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        } catch (Exception ignored) {}
    }
}
