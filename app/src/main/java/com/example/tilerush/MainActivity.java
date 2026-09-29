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
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20f);
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(titleFont);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(70, 34, 70, 34);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.argb(35, Color.red(color), Color.green(color), Color.blue(color)));
        gd.setStroke(5, color);
        gd.setCornerRadius(44f);
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

    // a few small floating tile shapes for visual flair on the menu screens
    private void addFloatingDecor(FrameLayout parent) {
        int[] colors = { CYAN, MAGENTA, AMBER, CYAN };
        float[][] pos = { {0.06f, 0.14f}, {0.86f, 0.20f}, {0.10f, 0.78f}, {0.88f, 0.74f} };
        for (int i = 0; i < colors.length; i++) {
            View shape = new View(this);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.argb(70, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])));
            gd.setCornerRadius(18f);
            shape.setBackground(gd);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(60, 110);
            lp.leftMargin = (int) (pos[i][0] * 1000);
            lp.topMargin = (int) (pos[i][1] * 1800);
            parent.addView(shape, lp);

            ObjectAnimator anim = ObjectAnimator.ofFloat(shape, "translationY", -20f, 20f);
            anim.setDuration(2200 + i * 300);
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

        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setVisibility(View.GONE);

        TextView statusText = new TextView(this);
        statusText.setTextSize(16f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTypeface(titleFont);
        LinearLayout.LayoutParams stP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stP.bottomMargin = 24;
        overlay.addView(statusText, stP);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);

        TextView restartBtn = neonButton("RESTART", CYAN);
        TextView menuBtn = neonButton("MENU", MAGENTA);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.rightMargin = 24;
        buttonRow.addView(restartBtn, lp1);
        buttonRow.addView(menuBtn);
        overlay.addView(buttonRow);

        restartBtn.setOnClickListener(v -> gameView.restartFromOutside());
        menuBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            gameView.release();
            showStartMenu();
        });

        FrameLayout.LayoutParams obP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        obP.bottomMargin = 160;
        gameFrame.addView(overlay, obP);

        gameView.listener = new GameView.Listener() {
            @Override
            public void onGameOver(int score) {
                boolean isHigh = updateHighScore(score);
                runOnUiThread(() -> {
                    statusText.setText(isHigh ? "\u2605 NEW HIGH SCORE!" : "Best: " + getHighScore());
                    statusText.setTextColor(isHigh ? AMBER : Color.argb(200, 255, 255, 255));
                    overlay.setVisibility(View.VISIBLE);
                    fadeIn(overlay, 0);
                });
            }

            @Override
            public void onRestart() {
                runOnUiThread(() -> overlay.setVisibility(View.GONE));
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