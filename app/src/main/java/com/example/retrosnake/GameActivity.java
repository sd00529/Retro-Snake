package com.example.retrosnake;

import android.media.SoundPool;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    private SnakeGameEngine engine;
    private GameThread thread;

    private GameView gameSurface;
    private Button btnRestart, btnExit;
    private ImageView gameOverImage;

    private SoundPool buttonSoundPool;
    private int btnSoundId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_game);

        gameSurface = findViewById(R.id.gameSurface);
        gameSurface.getHolder().addCallback(this);

        btnRestart = findViewById(R.id.btnRestart);
        btnExit = findViewById(R.id.btnExit);
        gameOverImage = findViewById(R.id.gameOverImage);

        btnRestart.setVisibility(View.INVISIBLE);
        btnExit.setVisibility(View.INVISIBLE);
        gameOverImage.setVisibility(View.INVISIBLE);

        buttonSoundPool = new SoundPool.Builder().setMaxStreams(2).build();
        btnSoundId = buttonSoundPool.load(this, R.raw.btn_click, 1);

        btnRestart.setOnClickListener(v -> {
            buttonSoundPool.play(btnSoundId, 1, 1, 1, 0, 1);
            recreate();
        });

        btnExit.setOnClickListener(v -> {
            buttonSoundPool.play(btnSoundId, 1, 1, 1, 0, 1);
            finish();
        });

        ImageButton up = findViewById(R.id.btnUp);
        ImageButton down = findViewById(R.id.btnDown);
        ImageButton left = findViewById(R.id.btnLeft);
        ImageButton right = findViewById(R.id.btnRight);

        View.OnClickListener dirClick = v -> buttonSoundPool.play(btnSoundId, 1, 1, 1, 0, 1);

        up.setOnClickListener(v -> { dirClick.onClick(v); engine.setDirection(0); });
        right.setOnClickListener(v -> { dirClick.onClick(v); engine.setDirection(1); });
        down.setOnClickListener(v -> { dirClick.onClick(v); engine.setDirection(2); });
        left.setOnClickListener(v -> { dirClick.onClick(v); engine.setDirection(3); });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int w = gameSurface.getWidth();
        int h = gameSurface.getHeight();

        engine = new SnakeGameEngine(this, w, h,
                newScore -> {},
                this::showGameOverUI
        );

        thread = new GameThread(holder, engine);
        thread.setRunning(true);
        thread.start();
    }

    private void showGameOverUI() {
        runOnUiThread(() -> {
            btnRestart.setVisibility(View.VISIBLE);
            btnExit.setVisibility(View.VISIBLE);
            gameOverImage.setVisibility(View.VISIBLE);

            btnRestart.bringToFront();
            btnExit.bringToFront();
            gameOverImage.bringToFront();
        });
    }

    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int i) {}
    @Override public void surfaceDestroyed(SurfaceHolder h) {
        if (thread != null) thread.setRunning(false);
    }


}
