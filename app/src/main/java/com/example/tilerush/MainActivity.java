package com.example.tilerush;

import android.os.Bundle;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private MediaPlayer mediaPlayer;
    private LinearLayout menuLayout;

    private final int[] tracks = {
            R.raw.bg_music1,
            R.raw.bg_music2,
            R.raw.bg_music3
    };
    private final String[] trackNames = {
            "Track 1: Upbeat",
            "Track 2: Chill",
            "Track 3: Intense"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMenu();
    }

    private void showMenu() {
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setGravity(Gravity.CENTER);
        menuLayout.setBackgroundColor(0xFF0A0A22);

        for (int i = 0; i < tracks.length; i++) {
            Button b = new Button(this);
            b.setText(trackNames[i]);
            final int trackRes = tracks[i];
            b.setOnClickListener(v -> startGame(trackRes));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 24;
            menuLayout.addView(b, lp);
        }

        setContentView(menuLayout);
    }

    private void startGame(int trackRes) {
        mediaPlayer = MediaPlayer.create(this, trackRes);
        if (mediaPlayer == null) {
            android.widget.Toast.makeText(this, "Couldn't load that track", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayer.setLooping(true);
        gameView = new GameView(this, mediaPlayer);
        setContentView(gameView);
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