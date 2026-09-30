package com.example.tilerush;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int BG_TOP = Color.rgb(10, 10, 34);
    private static final int BG_BOTTOM = Color.rgb(56, 18, 112);
    private static final int CYAN = Color.rgb(0, 225, 255);
    private static final int MAGENTA = Color.rgb(255, 70, 200);
    private static final int AMBER = Color.rgb(255, 190, 40);
    private static final int GREEN = Color.rgb(0, 240, 160);
    private static final int PURPLE = Color.rgb(185, 80, 255);
    private static final int CORAL = Color.rgb(255, 95, 70);
    private static final String PREFS = "tilerush_prefs";

    private GameView gameView;
    private MediaPlayer mediaPlayer;
    private MediaPlayer menuMusic;
    private MediaPlayer previewPlayer;
    private int selectedTrackIdx = -1;
    private SoundPool soundPool;
    private int clickSound = 0;
    private FrameLayout root;
    private Typeface titleFont;
    private boolean muted = false;

    private final int[] tracks = {
            R.raw.bg_music1, R.raw.bg_music2, R.raw.bg_music3,
            R.raw.bg_music4, R.raw.bg_music5, R.raw.bg_music6
    };
    private final String[] trackNames = {
            "Don't Blame Me", "Chanel", "At My Worst", "Faded", "Midnight Kisses", "On The Dance Floor"
    };
    private final int[] trackColors = {
            CYAN, MAGENTA, AMBER, GREEN, PURPLE, CORAL
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleFont = Typeface.create("sans-serif-black", Typeface.BOLD);
        initAudio();
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
        tv.setLetterSpacing(0.08f);
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

    // ---------- audio & mute ----------

    private void initAudio() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();
        try {
            clickSound = soundPool.load(this, R.raw.button_click, 1);
        } catch (Exception ignored) {}
    }

    private void playClickSound() {
        if (!muted && soundPool != null && clickSound != 0) {
            try {
                soundPool.play(clickSound, 0.9f, 0.9f, 1, 0, 1f);
            } catch (Exception ignored) {}
        }
    }

    private void startMenuMusic() {
        try {
            if (menuMusic == null) {
                menuMusic = MediaPlayer.create(this, R.raw.tilerush_theme);
                if (menuMusic != null) {
                    menuMusic.setLooping(true);
                }
            }
            if (menuMusic != null) {
                float vol = muted ? 0f : 0.85f;
                menuMusic.setVolume(vol, vol);
                if (!menuMusic.isPlaying()) {
                    menuMusic.start();
                }
            }
        } catch (Exception ignored) {}
    }

    private void stopMenuMusic() {
        try {
            if (menuMusic != null && menuMusic.isPlaying()) {
                menuMusic.pause();
            }
        } catch (Exception ignored) {}
    }

    private void playTrackPreview(int trackIdx) {
        stopTrackPreview();
        stopMenuMusic();
        selectedTrackIdx = trackIdx;
        try {
            previewPlayer = MediaPlayer.create(this, tracks[trackIdx]);
            if (previewPlayer != null) {
                previewPlayer.setLooping(true);
                float vol = muted ? 0f : 1f;
                previewPlayer.setVolume(vol, vol);
                previewPlayer.start();
            }
        } catch (Exception ignored) {}
    }

    private void stopTrackPreview() {
        try {
            if (previewPlayer != null) {
                if (previewPlayer.isPlaying()) {
                    previewPlayer.stop();
                }
                previewPlayer.release();
                previewPlayer = null;
            }
        } catch (Exception ignored) {}
    }

    private void updateTrackCardState(TextView card, int trackIdx, boolean isSelected) {
        int color = trackColors[trackIdx];
        GradientDrawable gd = new GradientDrawable();
        if (isSelected) {
            card.setText("▶   " + trackNames[trackIdx].toUpperCase() + "  [PREVIEW]");
            card.setTextColor(Color.WHITE);
            gd.setColor(Color.argb(85, Color.red(color), Color.green(color), Color.blue(color)));
            gd.setStroke(dpToPx(3), Color.WHITE);
            gd.setCornerRadius(36f);
            card.setShadowLayer(22f, 0, 0, color);
        } else {
            card.setText("♫   " + trackNames[trackIdx].toUpperCase());
            card.setTextColor(Color.argb(235, 255, 255, 255));
            gd.setColor(Color.argb(35, Color.red(color), Color.green(color), Color.blue(color)));
            gd.setStroke(4, color);
            gd.setCornerRadius(36f);
            card.setShadowLayer(0, 0, 0, 0);
        }
        card.setBackground(gd);
        card.setPadding(dpToPx(22), dpToPx(10), dpToPx(22), dpToPx(10));
    }

    private void applyMuteState() {
        float gameVol = muted ? 0f : 1f;
        float menuVol = muted ? 0f : 0.85f;
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(gameVol, gameVol);
            }
            if (menuMusic != null) {
                menuMusic.setVolume(menuVol, menuVol);
            }
            if (previewPlayer != null) {
                previewPlayer.setVolume(gameVol, gameVol);
            }
            if (gameView != null) {
                gameView.setMuted(muted);
            }
        } catch (Exception ignored) {}
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
            playClickSound();
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
        stopTrackPreview();
        selectedTrackIdx = -1;
        startMenuMusic();

        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        addFloatingDecor(root);
        addMuteButton(root);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);

        // Two-tone vibrant neon title matching the app logo
        TextView title = new TextView(this);
        SpannableString span = new SpannableString("TILE RUSH");
        span.setSpan(new ForegroundColorSpan(CYAN), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(MAGENTA), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setText(span);
        title.setTextSize(52f);
        title.setTypeface(titleFont);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.08f);
        title.setShadowLayer(32f, 0, 0, Color.argb(200, 0, 225, 255));
        col.addView(title);
        fadeIn(title, 0);

        // Futuristic, clean uppercase rhythm HUD tagline
        TextView tagline = new TextView(this);
        tagline.setText("TAP FAST  •  HOLD STEADY  •  KEEP THE BEAT");
        tagline.setTextColor(Color.argb(210, 190, 235, 255));
        tagline.setTextSize(11f);
        tagline.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tagline.setLetterSpacing(0.18f);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagP.topMargin = dpToPx(14);
        col.addView(tagline, tagP);
        fadeIn(tagline, 100);

        // Sleek Arcade Golden Capsule Badge
        int hs = getHighScore();
        TextView bestBadge = new TextView(this);
        bestBadge.setText(hs > 0 ? "BEST SCORE: " + hs : "★  READY FOR YOUR FIRST RUN");
        bestBadge.setTextColor(Color.rgb(255, 205, 50));
        bestBadge.setTextSize(12f);
        bestBadge.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        bestBadge.setLetterSpacing(0.12f);
        bestBadge.setGravity(Gravity.CENTER);
        bestBadge.setPadding(dpToPx(18), dpToPx(7), dpToPx(18), dpToPx(7));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.argb(40, 255, 190, 40));
        badgeBg.setStroke(dpToPx(1), Color.argb(170, 255, 205, 50));
        badgeBg.setCornerRadius(dpToPx(18));
        bestBadge.setBackground(badgeBg);
        bestBadge.setElevation(dpToPx(3));

        LinearLayout.LayoutParams bestP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bestP.topMargin = dpToPx(16);
        bestP.bottomMargin = dpToPx(44);
        col.addView(bestBadge, bestP);
        fadeIn(bestBadge, 180);

        // Neon Cyan Play Button
        TextView play = neonButton("▶   PLAY", CYAN, 54, 16);
        play.setLetterSpacing(0.14f);
        play.setOnClickListener(v -> {
            playClickSound();
            showMusicSelect();
        });
        col.addView(play);
        fadeIn(play, 260);
        play.postDelayed(() -> pulse(play), 750);

        FrameLayout.LayoutParams colP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(col, colP);
    }

    private void showMusicSelect() {
        stopTrackPreview();
        selectedTrackIdx = -1;
        startMenuMusic();

        root.removeAllViews();
        root.setBackground(bgGradient());
        setContentView(root);

        addFloatingDecor(root);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        col.setPadding(dpToPx(24), dpToPx(32), dpToPx(24), dpToPx(32));

        TextView heading = glowText("SELECT TRACK", 30f, MAGENTA);
        heading.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        col.addView(heading, hp);
        fadeIn(heading, 0);

        TextView sub = new TextView(this);
        sub.setText("TAP TO PREVIEW  •  TAP AGAIN OR PLAY");
        sub.setTextColor(Color.argb(210, 190, 235, 255));
        sub.setTextSize(11f);
        sub.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        sub.setLetterSpacing(0.16f);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subP.topMargin = dpToPx(6);
        subP.bottomMargin = dpToPx(18);
        col.addView(sub, subP);
        fadeIn(sub, 80);

        TextView[] cardViews = new TextView[tracks.length];

        TextView startBtn = neonButton("▶   START GAME", CYAN, dpToPx(28), dpToPx(12));
        startBtn.setTextSize(16f);
        startBtn.setLetterSpacing(0.10f);
        startBtn.setVisibility(View.GONE);
        LinearLayout.LayoutParams startP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        startP.topMargin = dpToPx(16);

        startBtn.setOnClickListener(v -> {
            playClickSound();
            if (selectedTrackIdx >= 0 && selectedTrackIdx < tracks.length) {
                startGame(tracks[selectedTrackIdx]);
            }
        });

        for (int i = 0; i < tracks.length; i++) {
            int idx = i;
            TextView card = neonButton("♫   " + trackNames[i].toUpperCase(), trackColors[i], dpToPx(22), dpToPx(10));
            card.setTextSize(16f);
            card.setLetterSpacing(0.06f);
            cardViews[i] = card;

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.topMargin = dpToPx(9);

            card.setOnClickListener(v -> {
                playClickSound();
                if (selectedTrackIdx == idx) {
                    startGame(tracks[idx]);
                } else {
                    playTrackPreview(idx);
                    for (int k = 0; k < tracks.length; k++) {
                        updateTrackCardState(cardViews[k], k, k == idx);
                    }
                    startBtn.setText("▶   PLAY " + trackNames[idx].toUpperCase());
                    int c = trackColors[idx];
                    GradientDrawable startGd = new GradientDrawable();
                    startGd.setColor(Color.argb(80, Color.red(c), Color.green(c), Color.blue(c)));
                    startGd.setStroke(dpToPx(3), c);
                    startGd.setCornerRadius(36f);
                    startBtn.setBackground(startGd);
                    startBtn.setPadding(dpToPx(28), dpToPx(12), dpToPx(28), dpToPx(12));
                    if (startBtn.getVisibility() != View.VISIBLE) {
                        startBtn.setVisibility(View.VISIBLE);
                        fadeIn(startBtn, 0);
                        pulse(startBtn);
                    }
                }
            });
            col.addView(card, cp);
            fadeIn(card, 100 + i * 35);
        }

        col.addView(startBtn, startP);

        TextView back = new TextView(this);
        back.setText("←   BACK TO TITLE");
        back.setTextColor(Color.argb(220, 255, 255, 255));
        back.setTextSize(12f);
        back.setTypeface(Typeface.create("sans-serif-bold", Typeface.NORMAL));
        back.setLetterSpacing(0.14f);
        back.setGravity(Gravity.CENTER);
        back.setPadding(dpToPx(20), dpToPx(9), dpToPx(20), dpToPx(9));

        GradientDrawable backBg = new GradientDrawable();
        backBg.setColor(Color.argb(35, 255, 255, 255));
        backBg.setStroke(dpToPx(1), Color.argb(90, 255, 255, 255));
        backBg.setCornerRadius(dpToPx(20));
        back.setBackground(backBg);

        LinearLayout.LayoutParams backP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        backP.topMargin = dpToPx(16);
        back.setOnClickListener(v -> {
            playClickSound();
            stopTrackPreview();
            selectedTrackIdx = -1;
            showStartMenu();
        });
        col.addView(back, backP);
        fadeIn(back, 360);

        scroll.addView(col, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams scrollP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(scroll, scrollP);

        addMuteButton(root);
    }

    private void startGame(int trackRes) {
        stopTrackPreview();
        stopMenuMusic();
        selectedTrackIdx = -1;

        mediaPlayer = MediaPlayer.create(this, trackRes);
        if (mediaPlayer == null) {
            Toast.makeText(this, "Couldn't load that track", Toast.LENGTH_SHORT).show();
            startMenuMusic();
            return;
        }
        mediaPlayer.setLooping(true);
        applyMuteState();

        gameView = new GameView(this, mediaPlayer);
        gameView.setMuted(muted);

        FrameLayout gameFrame = new FrameLayout(this);
        gameFrame.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        addMuteButton(gameFrame);

        // --- Centered Unified Game Over Card ---
        LinearLayout gameOverCard = new LinearLayout(this);
        gameOverCard.setOrientation(LinearLayout.VERTICAL);
        gameOverCard.setGravity(Gravity.CENTER);
        gameOverCard.setVisibility(View.GONE);

        TextView gameOverTitle = glowText("GAME OVER", 40f, MAGENTA);
        gameOverTitle.setLetterSpacing(0.14f);
        LinearLayout.LayoutParams titleP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(gameOverTitle, titleP);

        TextView scoreDisplay = glowText("SCORE: 0", 26f, CYAN);
        scoreDisplay.setLetterSpacing(0.10f);
        LinearLayout.LayoutParams scP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scP.topMargin = dpToPx(14);
        scP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(scoreDisplay, scP);

        TextView statusText = new TextView(this);
        statusText.setTextSize(12f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        statusText.setLetterSpacing(0.12f);
        statusText.setPadding(dpToPx(18), dpToPx(7), dpToPx(18), dpToPx(7));
        LinearLayout.LayoutParams stP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stP.topMargin = dpToPx(12);
        stP.bottomMargin = dpToPx(32);
        stP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(statusText, stP);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);

        TextView restartBtn = neonButton("RESTART", CYAN, dpToPx(28), dpToPx(14));
        restartBtn.setLetterSpacing(0.10f);
        TextView menuBtn = neonButton("MENU", MAGENTA, dpToPx(28), dpToPx(14));
        menuBtn.setLetterSpacing(0.10f);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.rightMargin = dpToPx(16);
        buttonRow.addView(restartBtn, lp1);
        buttonRow.addView(menuBtn);

        LinearLayout.LayoutParams rowP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowP.gravity = Gravity.CENTER_HORIZONTAL;
        gameOverCard.addView(buttonRow, rowP);

        restartBtn.setOnClickListener(v -> {
            playClickSound();
            stopMenuMusic();
            gameView.restartFromOutside();
        });
        menuBtn.setOnClickListener(v -> {
            playClickSound();
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            } catch (Exception ignored) {}
            if (gameView != null) {
                gameView.release();
                gameView = null;
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
                // Allow error sound to punch through, then resume soft ambient menu theme
                gameFrame.postDelayed(() -> {
                    if (gameView != null && gameView.isGameOver()) {
                        startMenuMusic();
                    }
                }, 450);

                gameFrame.post(() -> {
                    scoreDisplay.setText("SCORE: " + score);
                    statusText.setText(isHigh ? "★  NEW HIGH SCORE!  ★" : "★  BEST SCORE: " + getHighScore() + "  ★");

                    GradientDrawable badgeBg = new GradientDrawable();
                    if (isHigh) {
                        statusText.setTextColor(Color.rgb(255, 215, 50));
                        badgeBg.setColor(Color.argb(45, 255, 190, 40));
                        badgeBg.setStroke(dpToPx(1), Color.rgb(255, 215, 50));
                    } else {
                        statusText.setTextColor(Color.argb(220, 190, 235, 255));
                        badgeBg.setColor(Color.argb(35, 0, 225, 255));
                        badgeBg.setStroke(dpToPx(1), Color.argb(120, 0, 225, 255));
                    }
                    badgeBg.setCornerRadius(dpToPx(16));
                    statusText.setBackground(badgeBg);

                    gameOverCard.setVisibility(View.VISIBLE);
                    gameOverCard.bringToFront();
                    fadeIn(gameOverCard, 0);
                });
            }

            @Override
            public void onRestart() {
                stopMenuMusic();
                gameFrame.post(() -> gameOverCard.setVisibility(View.GONE));
            }
        };

        setContentView(gameFrame);
        gameView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null && !gameView.isGameOver()) {
            gameView.resume();
        } else if (previewPlayer != null) {
            try {
                if (!previewPlayer.isPlaying() && !muted) {
                    previewPlayer.start();
                }
            } catch (Exception ignored) {}
        } else {
            startMenuMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            if (menuMusic != null && menuMusic.isPlaying()) menuMusic.pause();
            if (previewPlayer != null && previewPlayer.isPlaying()) previewPlayer.pause();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) gameView.release();
        stopTrackPreview();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (menuMusic != null) {
                menuMusic.release();
                menuMusic = null;
            }
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        } catch (Exception ignored) {}
    }
}