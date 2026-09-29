package com.example.tilerush;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int BG_TOP = Color.rgb(10, 10, 34);
    private static final int BG_BOTTOM = Color.rgb(56, 18, 112);
    private static final int CYAN = Color.rgb(0, 225, 255);
    private static final int MAGENTA = Color.rgb(255, 70, 200);
    private static final int AMBER = Color.rgb(255, 190, 40);
    private static final String PREFS = "tilerush_prefs";

    private GameView gameView;
    private MediaPlayer mediaPlayer;
    private FrameLayout root;
    private Typeface titleFont;
    private boolean muted = false;

    private final int[] tracks = { R.raw.bg_music1, R.raw.bg_music2, R.raw.bg_music3 };
    private final String[] trackNames = { "Upbeat", "Chill", "Intense" };
    private final int[] trackColors = { CYAN, MAGENTA, AMBER };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleFont = Typeface.create("sans-serif-black", Typeface.BOLD);
        root = new FrameLayout(this);
        setContentView(root);
        showStartMenu();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ---------- high score ----------

    private int getHighScore() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getInt("high_score", 0);
    }

    private boolean updateHighScore(int score) {
        int hs = getHighScore();
        if (score > hs) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("high_score", score).apply();
            return true;
        }
        return false;
    }

    // ---------- styling helpers ----------

    private GradientDrawable bgGradient() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{BG_TOP, BG_BOTTOM});
    }

    private TextView glowText(String text, float size, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setTypeface(titleFont);
        tv.setGravity(Gravity.CENTER);
        tv.setShadowLayer(28f, 0, 0, color);
        return tv;
    }

    private TextView neonButton(String text, int color) {
        return neonButton(text, color, 64, 30);
    }

    private TextView neonButton(String text, int color, int padX, int padY) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18f);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(titleFont);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(padX, padY, padX, padY);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.argb(35, Color.red(color), Color.green(color), Color.blue(color)));
        gd.setStroke(4, color);
        gd.setCornerRadius(36f);
        tv.setBackground(gd);
        tv.setElevation(8f);
        return tv;
    }

    private void pulse(View v) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.06f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.06f, 1f);
        sx.setRepeatCount(ValueAnimator.INFINITE);
        sy.setRepeatCount(ValueAnimator.INFINITE);
        sx.setDuration(1100);
        sy.setDuration(1100);
        sx.start();
        sy.start();
    }

    private void fadeIn(View v, int delay) {
        v.setAlpha(0f);
        v.setTranslationY(30f);
        v.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(450).start();
    }

    // Sleek, subtle ambient rhythm tiles in the background
    private void addFloatingDecor(FrameLayout parent) {
        int screenW = getResources().getDisplayMetrics().widthPixels;
        int screenH = getResources().getDisplayMetrics().heightPixels;

        // Structured across 4 clean vertical rhythm columns (matching 4 game lanes)
        // { xRatio, yRatio, widthDp, heightDp, colorIndex, alpha }
        float[][] decorData = {
            // Lane 1 (far left)
            { 0.10f, 0.08f, 20, 38, 0, 30 },  // Cyan
            { 0.10f, 0.42f, 22, 44, 1, 35 },  // Magenta
            { 0.10f, 0.76f, 20, 36, 2, 28 },  // Amber

            // Lane 2 (inner left - top and bottom only to keep center text clean)
            { 0.34f, 0.04f, 18, 32, 2, 25 },  // Amber
            { 0.34f, 0.84f, 22, 42, 0, 32 },  // Cyan

            // Lane 3 (inner right - top and bottom only)
            { 0.64f, 0.12f, 20, 36, 1, 30 },  // Magenta
            { 0.64f, 0.80f, 18, 34, 2, 26 },  // Amber

            // Lane 4 (far right)
            { 0.88f, 0.06f, 22, 40, 0, 32 },  // Cyan
            { 0.88f, 0.46f, 20, 38, 2, 28 },  // Amber
            { 0.88f, 0.72f, 22, 44, 1, 35 }   // Magenta
        };

        int[] colors = { CYAN, MAGENTA, AMBER };

        for (int i = 0; i < decorData.length; i++) {
            float[] d = decorData[i];
            int color = colors[(int) d[4]];
            int alpha = (int) d[5];

            View shape = new View(this);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
            gd.setStroke(dpToPx(1), Color.argb(alpha + 30, Color.red(color), Color.green(color), Color.blue(color)));
            gd.setCornerRadius(dpToPx(5));
            shape.setBackground(gd);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dpToPx((int) d[2]), dpToPx((int) d[3]));
            lp.leftMargin = (int) (d[0] * screenW);
            lp.topMargin = (int) (d[1] * screenH);
            parent.addView(shape, lp);

            float floatRange = dpToPx(8);
            ObjectAnimator anim = ObjectAnimator.ofFloat(shape, "translationY", -floatRange, floatRange);
            anim.setDuration(2400 + (i * 150));
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.start();
        }
    }

    // ---------- mute button ----------

    private void applyMuteState() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(muted ? 0f : 1f, muted ? 0f : 1f);
        }
    }

    private TextView buildMuteButton() {
        TextView btn = new TextView(this);
        btn.setText(muted ? "\uD83D\uDD07" : "\uD83D\uDD0A");
        btn.setTextSize(20f);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(24, 24, 24, 24);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.argb(50, 255, 255, 255));
        gd.setStroke(3, muted ? Color.argb(130, 255, 255, 255) : CYAN);
        gd.setShape(GradientDrawable.OVAL);
        btn.setBackground(gd);
        btn.setOnClickListener(v -> {
            muted = !muted;
            applyMuteState();
            btn.setText(muted ? "\uD83D\uDD07" : "\uD83D\uDD0A");
            GradientDrawable gd2 = new GradientDrawable();
            gd2.setColor(Color.argb(50, 255, 255, 255));
            gd2.setStroke(3, muted ? Color.argb(130, 255, 255, 255) : CYAN);
            gd2.setShape(GradientDrawable.OVAL);
            btn.setBackground(gd2);
        });
        return btn;
    }

    private void addMuteButton(FrameLayout parent) {
        FrameLayout.LayoutParams muteP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END);
        muteP.topMargin = 60;
        muteP.rightMargin = 40;
        parent.addView(buildMuteButton(), muteP);
    }

    // ---------- screens ----------

    private void showStartMenu() {
        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        addFloatingDecor(root);
        addMuteButton(root);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);

        TextView title = glowText("TILERUSH", 56f, CYAN);
        col.addView(title);
        fadeIn(title, 0);

        TextView tagline = new TextView(this);
        tagline.setText("Tap fast. Hold steady. Don't miss a beat.");
        tagline.setTextColor(Color.argb(220, 255, 255, 255));
        tagline.setTextSize(15f);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagP.topMargin = 24;
        col.addView(tagline, tagP);
        fadeIn(tagline, 100);

        int hs = getHighScore();
        TextView best = new TextView(this);
        best.setText(hs > 0 ? "\u2605 Best: " + hs : "Tap tiles. Hold the long ones.");
        best.setTextColor(AMBER);
        best.setTextSize(14f);
        best.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bestP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bestP.topMargin = 14;
        bestP.bottomMargin = 90;
        col.addView(best, bestP);
        fadeIn(best, 180);

        TextView play = neonButton("\u25B6  PLAY", CYAN);
        play.setOnClickListener(v -> showMusicSelect());
        col.addView(play);
        fadeIn(play, 260);
        play.postDelayed(() -> pulse(play), 750);

        FrameLayout.LayoutParams colP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(col, colP);
    }

    private void showMusicSelect() {
        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        addFloatingDecor(root);
        addMuteButton(root);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);

        TextView heading = glowText("CHOOSE YOUR TRACK", 24f, MAGENTA);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.bottomMargin = 60;
        col.addView(heading, hp);
        fadeIn(heading, 0);

        for (int i = 0; i < tracks.length; i++) {
            int idx = i;
            TextView card = neonButton("\u266A  " + trackNames[i].toUpperCase(), trackColors[i]);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.topMargin = 22;
            card.setOnClickListener(v -> startGame(tracks[idx]));
            col.addView(card, cp);
            fadeIn(card, 100 + i * 90);
        }

        TextView back = new TextView(this);
        back.setText("\u2190 Back");
        back.setTextColor(Color.argb(180, 255, 255, 255));
        back.setTextSize(15f);
        back.setPadding(20, 70, 20, 20);
        back.setOnClickListener(v -> showStartMenu());
        col.addView(back);

        FrameLayout.LayoutParams colP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(col, colP);
    }

    private void startGame(int trackRes) {
        mediaPlayer = MediaPlayer.create(this, trackRes);
        if (mediaPlayer == null) {
            Toast.makeText(this, "Couldn't load that track", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer.setLooping(true);
        applyMuteState();

        gameView = new GameView(this, mediaPlayer);

        FrameLayout gameFrame = new FrameLayout(this);
        gameFrame.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        addMuteButton(gameFrame);

        // --- Centered Unified Game Over Card ---
        LinearLayout gameOverCard = new LinearLayout(this);
        gameOverCard.setOrientation(LinearLayout.VERTICAL);
        gameOverCard.setGravity(Gravity.CENTER);
        gameOverCard.setVisibility(View.GONE);

        TextView gameOverTitle = glowText("GAME OVER", 38f, MAGENTA);
        LinearLayout.LayoutParams titleP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(gameOverTitle, titleP);

        TextView scoreDisplay = glowText("Score: 0", 24f, CYAN);
        LinearLayout.LayoutParams scP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scP.topMargin = 14;
        scP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(scoreDisplay, scP);

        TextView statusText = new TextView(this);
        statusText.setTextSize(15f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTypeface(titleFont);
        LinearLayout.LayoutParams stP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stP.topMargin = 8;
        stP.bottomMargin = 30;
        stP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(statusText, stP);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);

        TextView restartBtn = neonButton("RESTART", CYAN, 32, 16);
        TextView menuBtn = neonButton("MENU", MAGENTA, 32, 16);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.rightMargin = 16;
        buttonRow.addView(restartBtn, lp1);
        buttonRow.addView(menuBtn);

        LinearLayout.LayoutParams rowP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(buttonRow, rowP);

        restartBtn.setOnClickListener(v -> gameView.restartFromOutside());
        menuBtn.setOnClickListener(v -> {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            } catch (Exception ignored) {}
            if (gameView != null) {
                gameView.release();
            }
            showStartMenu();
        });

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        gameFrame.addView(gameOverCard, cardParams);

        gameView.listener = new GameView.Listener() {
            @Override
            public void onGameOver(int score) {
                boolean isHigh = updateHighScore(score);
                gameFrame.post(() -> {
                    scoreDisplay.setText("Score: " + score);
                    statusText.setText(isHigh ? "NEW HIGH SCORE!" : "Best: " + getHighScore());
                    statusText.setTextColor(isHigh ? AMBER : Color.argb(200, 255, 255, 255));

                    gameOverCard.setVisibility(View.VISIBLE);
                    gameOverCard.bringToFront();
                    fadeIn(gameOverCard, 0);
                });
            }

            @Override
            public void onRestart() {
                gameFrame.post(() -> gameOverCard.setVisibility(View.GONE));
            }
        };

        setContentView(gameFrame);
        gameView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) gameView.release();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}