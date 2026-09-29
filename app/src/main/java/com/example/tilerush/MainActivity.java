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

    private GameView gameView;
    private MediaPlayer mediaPlayer;
    private FrameLayout root;
    private Typeface titleFont;

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

    // ---------- screens ----------

    private void showStartMenu() {
        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);

        col.addView(glowText("TILERUSH", 56f, CYAN));

        TextView tagline = new TextView(this);
        tagline.setText("Tap fast. Hold steady. Don't miss a beat.");
        tagline.setTextColor(Color.argb(220, 255, 255, 255));
        tagline.setTextSize(15f);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagP.topMargin = 24;
        tagP.bottomMargin = 100;
        col.addView(tagline, tagP);

        TextView play = neonButton("\u25B6  PLAY", CYAN);
        play.setOnClickListener(v -> showMusicSelect());
        col.addView(play);
        pulse(play);

        TextView how = new TextView(this);
        how.setText("4 lanes. Tap the tiles. Hold the long ones.");
        how.setTextColor(Color.argb(160, 255, 255, 255));
        how.setTextSize(13f);
        how.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams howP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        howP.topMargin = 44;
        col.addView(how, howP);

        FrameLayout.LayoutParams colP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(col, colP);
    }

    private void showMusicSelect() {
        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);

        TextView heading = glowText("CHOOSE YOUR TRACK", 24f, MAGENTA);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.bottomMargin = 60;
        col.addView(heading, hp);

        for (int i = 0; i < tracks.length; i++) {
            int idx = i;
            TextView card = neonButton("\u266A  " + trackNames[i].toUpperCase(), trackColors[i]);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.topMargin = 22;
            card.setOnClickListener(v -> startGame(tracks[idx]));
            col.addView(card, cp);
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

        root.removeAllViews();
        gameView = new GameView(this, mediaPlayer);

        FrameLayout gameFrame = new FrameLayout(this);
        gameFrame.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout overlayButtons = new LinearLayout(this);
        overlayButtons.setOrientation(LinearLayout.HORIZONTAL);
        overlayButtons.setGravity(Gravity.CENTER);
        overlayButtons.setVisibility(View.GONE);

        TextView restartBtn = neonButton("RESTART", CYAN);
        TextView menuBtn = neonButton("MENU", MAGENTA);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.rightMargin = 24;
        overlayButtons.addView(restartBtn, lp1);
        overlayButtons.addView(menuBtn);

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
        gameFrame.addView(overlayButtons, obP);

        gameView.listener = new GameView.Listener() {
            @Override
            public void onGameOver(int score) {
                runOnUiThread(() -> overlayButtons.setVisibility(View.VISIBLE));
            }

            @Override
            public void onRestart() {
                runOnUiThread(() -> overlayButtons.setVisibility(View.GONE));
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