package com.example.retrosnake;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {


    private final SurfaceHolder holder;
    private final SnakeGameEngine engine;
    private boolean running = true;

    public GameThread(SurfaceHolder holder, SnakeGameEngine engine) {
        this.holder = holder;
        this.engine = engine;
    }

    public void setRunning(boolean r) {
        running = r;
    }

    @Override
    public void run() {
        while (running) {
            if (engine.isGameOver()) {
                try { sleep(120); } catch (Exception ignored) {}
                continue;
            }

            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    engine.update();
                    engine.draw(canvas);
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            try { sleep(180); } catch (Exception ignored) {}
        }
    }


}

