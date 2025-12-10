package com.example.retrosnake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Random;

public class SnakeGameEngine {


    private final ArrayList<Point> snake = new ArrayList<>();
    private final Paint paint = new Paint();
    private final Random random = new Random();
    private final Context context;

    private final int snakeSize = 60;
    private int direction = -1;

    private int foodX, foodY;
    private int score = 0;
    private boolean gameOver = false;

    private final ScoreListener scoreListener;
    private final GameOverListener gameOverListener;

    private final int controlPadHeight;
    private final int gameWidth;
    private final int gameHeight;

    private Bitmap headBitmap;
    private Bitmap bodyStraightBitmap;
    private Bitmap bodyCurveBitmap;
    private Bitmap tailBitmap;
    private Bitmap foodBitmap;

    private SoundPool soundPool;
    private int eatSoundId;

    public interface ScoreListener { void onScoreChanged(int newScore); }
    public interface GameOverListener { void onGameOver(); }

    public SnakeGameEngine(Context ctx, int screenWidth, int screenHeight,
                           ScoreListener scoreCallback,
                           GameOverListener gameOverCallback) {

        this.context = ctx;
        this.scoreListener = scoreCallback;
        this.gameOverListener = gameOverCallback;

        float scale = ctx.getResources().getDisplayMetrics().density;
        controlPadHeight = (int) (220 * scale);

        gameWidth = screenWidth;
        gameHeight = screenHeight - controlPadHeight;

        snake.add(new Point(screenWidth / 2, gameHeight / 2));

        headBitmap = load(ctx, R.drawable.snake_head);
        bodyStraightBitmap = load(ctx, R.drawable.snake_body_straight);
        bodyCurveBitmap = load(ctx, R.drawable.snake_body_curve);
        tailBitmap = load(ctx, R.drawable.snake_tail);
        foodBitmap = load(ctx, R.drawable.food);

        soundPool = new SoundPool.Builder().setMaxStreams(4).build();
        eatSoundId = soundPool.load(ctx, R.raw.points, 1);

        generateFood();
    }

    private Bitmap load(Context ctx, int id) {
        Bitmap bmp = BitmapFactory.decodeResource(ctx.getResources(), id);
        return Bitmap.createScaledBitmap(bmp, snakeSize, snakeSize, false);
    }

    public void setDirection(int newDir) {
        if ((direction == 0 && newDir == 2) || (direction == 2 && newDir == 0)
                || (direction == 1 && newDir == 3) || (direction == 3 && newDir == 1)) return;
        direction = newDir;
    }

    public boolean isGameOver() { return gameOver; }

    public void update() {
        if (gameOver || direction == -1) return;

        Point head = snake.get(0);
        int newX = head.x;
        int newY = head.y;

        switch (direction) {
            case 0: newY -= snakeSize; break;
            case 1: newX += snakeSize; break;
            case 2: newY += snakeSize; break;
            case 3: newX -= snakeSize; break;
        }

        // Wall collision
        if (newX < 0 || newX >= gameWidth || newY < 0 || newY >= gameHeight) {
            gameOver = true;
            if (context instanceof GameActivity) {
                ((GameActivity) context).runOnUiThread(() -> gameOverListener.onGameOver());
            }
            return;
        }

        // Self collision
        for (int i = 1; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (p.x == newX && p.y == newY) {
                gameOver = true;
                if (context instanceof GameActivity) {
                    ((GameActivity) context).runOnUiThread(() -> gameOverListener.onGameOver());
                }
                return;
            }
        }

        snake.add(0, new Point(newX, newY));

        if ((newX / snakeSize == foodX / snakeSize) &&
                (newY / snakeSize == foodY / snakeSize)) {
            score++;
            soundPool.play(eatSoundId,1,1,1,0,1);
            generateFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(0xFF000000);

        for (int i = 0; i < snake.size(); i++) {
            Bitmap bmp;
            float angle;

            if (i == 0) {
                bmp = headBitmap;
                angle = headAngle();
            } else if (i == snake.size() - 1) {
                bmp = tailBitmap;
                angle = tailAngle();
            } else if (snake.size() >= 3) {
                Point prev = snake.get(i - 1);
                Point curr = snake.get(i);
                Point next = snake.get(i + 1);

                int dx1 = prev.x - curr.x;
                int dy1 = prev.y - curr.y;
                int dx2 = next.x - curr.x;
                int dy2 = next.y - curr.y;

                if (dx1 == dx2 || dy1 == dy2) {
                    bmp = bodyStraightBitmap;
                    angle = bodyStraightAngle(prev, curr);
                } else {
                    bmp = bodyCurveBitmap;
                    angle = bodyCurveAngle(prev, curr, next);
                }
            } else {
                bmp = bodyStraightBitmap;
                angle = bodyStraightAngle(snake.get(i - 1), snake.get(i));
            }

            drawRotated(canvas, bmp, snake.get(i).x, snake.get(i).y, angle);
        }

        canvas.drawBitmap(foodBitmap, foodX, foodY, null);

        // Draw score once
        paint.setColor(0xFFFFFFFF);
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        paint.setTextSize(11 * scale);  // small score
        canvas.drawText("Score: " + score, 40, 80, paint);
    }

    private void drawRotated(Canvas canvas, Bitmap bmp, int x, int y, float angle) {
        Matrix m = new Matrix();
        m.postTranslate(-snakeSize / 2f, -snakeSize / 2f);
        m.postRotate(angle);
        m.postTranslate(x + snakeSize / 2f, y + snakeSize / 2f);
        canvas.drawBitmap(bmp, m, null);
    }

    private float headAngle() {
        switch (direction) {
            case 0: return 0;
            case 1: return 90;
            case 2: return 180;
            case 3: return 270;
        }
        return 0;
    }

    private float tailAngle() {
        Point t = snake.get(snake.size() - 1);
        Point prev = snake.get(snake.size() - 2);

        if (prev.x < t.x) return 90;
        if (prev.x > t.x) return 270;
        if (prev.y < t.y) return 180;
        return 0;
    }

    private float bodyStraightAngle(Point prev, Point curr) {
        return (prev.x != curr.x) ? 90 : 0;
    }

    private float bodyCurveAngle(Point prev, Point curr, Point next) {
        if (prev.x < curr.x && next.y < curr.y) return 0;
        if (prev.y < curr.y && next.x > curr.x) return 90;
        if (prev.x > curr.x && next.y > curr.y) return 180;
        return 270;
    }

    private void generateFood() {
        int maxX = gameWidth / snakeSize;
        int maxY = gameHeight / snakeSize;

        foodX = random.nextInt(maxX) * snakeSize;
        foodY = random.nextInt(maxY) * snakeSize;
    }


}
