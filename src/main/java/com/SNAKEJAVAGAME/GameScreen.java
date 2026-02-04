package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

/**
 * The actual Snake game screen - AAA polish edition!
 */
public class GameScreen implements Screen {

    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int CELL_SIZE = 32;
    private static final float MOVE_INTERVAL = 0.12f;
    private static final float SMOOTH_LERP = 18f;  // Smooth movement speed
    private static final float EAT_PULSE_DURATION = 0.15f;
    private static final float TRANSITION_DURATION = 0.35f;

    private final SnakeGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private GameFont gameFont;

    private Array<Vector2> snake;
    private Array<Vector2> displayPositions;  // Smooth interpolated positions
    private Vector2 food;
    private int direction;
    private int nextDirection;
    private int queuedDirection; // second buffered turn (for fast combos)
    private float moveTimer;
    private int score;
    private boolean gameOver;
    private boolean paused;
    private float eatPulseTimer;  // Head squash/stretch when eating
    private float playTime;  // Seconds played this round
    private boolean newHighScore;  // Set when game over and score beats high
    private float slitherTime; // Time accumulator for sine-wave slither
    private Random random;

    private ParticleSystem particles;
    private ScreenShake screenShake;

    public GameScreen(SnakeGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.position.set(GRID_WIDTH * CELL_SIZE / 2f, GRID_HEIGHT * CELL_SIZE / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        gameFont = new GameFont(font);

        particles = new ParticleSystem();
        particles.setParticleTexture(new TextureRegion(game.getAssets().particleTex));
        screenShake = new ScreenShake(camera);

        random = new Random();
        resetGame();
    }

    private void resetGame() {
        snake = new Array<>();
        snake.add(new Vector2(GRID_WIDTH / 2, GRID_HEIGHT / 2));
        snake.add(new Vector2(GRID_WIDTH / 2 - 1, GRID_HEIGHT / 2));
        snake.add(new Vector2(GRID_WIDTH / 2 - 2, GRID_HEIGHT / 2));

        displayPositions = new Array<>();
        for (Vector2 seg : snake) {
            displayPositions.add(new Vector2(seg.x * CELL_SIZE, seg.y * CELL_SIZE));
        }

        direction = 1;
        nextDirection = 1;
        queuedDirection = -1;
        moveTimer = 0;
        score = 0;
        gameOver = false;
        paused = false;
        eatPulseTimer = 0;
        playTime = 0;
        slitherTime = 0;
        newHighScore = false;
        particles.clear();

        spawnFood();
    }

    private void spawnFood() {
        do {
            int x = random.nextInt(GRID_WIDTH);
            int y = random.nextInt(GRID_HEIGHT);
            food = new Vector2(x, y);
        } while (isSnakeAt(food.x, food.y));
    }

    private int getTailDirection(int tailIndex) {
        Vector2 tail = snake.get(tailIndex);
        Vector2 prev = snake.get(tailIndex - 1);
        if (tail.x > prev.x) return 1;
        if (tail.x < prev.x) return 3;
        if (tail.y > prev.y) return 0;
        return 2;
    }

    private boolean isSnakeAt(float x, float y) {
        for (Vector2 segment : snake) {
            if (segment.x == x && segment.y == y) return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        Theme theme = game.getTheme();

        if (gameOver) {
            handleGameOverInput();
        } else if (paused) {
            handlePausedInput();
        } else {
            handleInput();
            playTime += delta;
            slitherTime += delta;
            moveTimer += delta;
            if (moveTimer >= MOVE_INTERVAL) {
                moveTimer = 0;
                moveSnake();
            }
        }

        // Smooth movement - lerp display positions toward logical positions
        updateSmoothPositions(delta);

        // Update effects
        eatPulseTimer -= delta;
        if (eatPulseTimer < 0) eatPulseTimer = 0;
        particles.update(delta);
        screenShake.setBasePosition(GRID_WIDTH * CELL_SIZE / 2f, GRID_HEIGHT * CELL_SIZE / 2f);
        screenShake.update(delta);

        viewport.apply();
        ScreenUtils.clear(theme.getBackground().r, theme.getBackground().g, theme.getBackground().b, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float overlap = 2f;
        float drawSize = CELL_SIZE + overlap * 2;
        float drawOffset = -overlap;
        boolean isNeon = theme == Theme.NEON;
        float glowSize = CELL_SIZE * 2.5f;

        // Neon glow under food (before food)
        if (isNeon && game.getAssets().glowTex != null) {
            batch.setColor(theme.getFood().r, theme.getFood().g, theme.getFood().b, 0.5f);
            batch.draw(game.getAssets().glowTex, food.x * CELL_SIZE - (glowSize - CELL_SIZE) / 2,
                    food.y * CELL_SIZE - (glowSize - CELL_SIZE) / 2, glowSize, glowSize);
            batch.setColor(1, 1, 1, 1);
        }

        // Draw snake with smooth positions
        for (int i = snake.size - 1; i >= 0; i--) {
            Vector2 pos = displayPositions.get(i);
            float px = pos.x + drawOffset;
            float py = pos.y + drawOffset;

            // Sine-wave slither: offset each segment slightly perpendicular to its direction
            int localDir;
            if (i == 0) {
                localDir = direction;
            } else if (i == snake.size - 1) {
                Vector2 tailPrev = snake.get(i - 1);
                Vector2 tail = snake.get(i);
                localDir = dirFromTo(tail, tailPrev);
            } else {
                Vector2 cur = snake.get(i);
                Vector2 prev = snake.get(i - 1);
                localDir = dirFromTo(cur, prev);
            }

            float dx = 0, dy = 0;
            switch (localDir) {
                case 0 -> { dx = 0; dy = 1; }   // up
                case 1 -> { dx = 1; dy = 0; }   // right
                case 2 -> { dx = 0; dy = -1; }  // down
                case 3 -> { dx = -1; dy = 0; }  // left
            }
            // Perpendicular vector
            float pxPerp = -dy;
            float pyPerp = dx;
            float wave = (float) Math.sin(slitherTime * 6f + i * 0.6f);
            float amplitude = 2.5f;
            px += pxPerp * wave * amplitude;
            py += pyPerp * wave * amplitude;

            float scale = 1f;
            if (i == 0 && eatPulseTimer > 0) {
                // Pulse animation - squash/stretch when eating
                float t = 1f - (eatPulseTimer / EAT_PULSE_DURATION);
                scale = 1f + 0.25f * (float) Math.sin(t * Math.PI);
            }

            float w = drawSize * scale;
            float h = drawSize * scale;
            float ox = (drawSize - w) / 2;
            float oy = (drawSize - h) / 2;

            if (i == 0) {
                batch.draw(game.getAssets().snakeHead[direction], px + ox, py + oy, w, h);
            } else if (i == snake.size - 1) {
                batch.draw(game.getAssets().snakeTail[getTailDirection(i)], px + ox, py + oy, w, h);
            } else {
                int cornerIndex = getCornerIndex(i);
                if (cornerIndex != -1 && game.getAssets().snakeCorner != null) {
                    batch.draw(game.getAssets().snakeCorner[cornerIndex], px + ox, py + oy, w, h);
                } else {
                    batch.draw(game.getAssets().snakeBody, px + ox, py + oy, w, h);
                }
            }
        }

        // Draw food
        batch.draw(game.getAssets().food, food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Draw particles (on top of snake)
        particles.draw(batch);

        // Border - theme colored (Neon gets a subtle glow)
        float w = GRID_WIDTH * CELL_SIZE;
        float h = GRID_HEIGHT * CELL_SIZE;
        float border = isNeon ? 6f : 4f;
        float glow = isNeon ? 0.3f : 0f;
        if (isNeon && glow > 0) {
            batch.setColor(theme.getBorder().r, theme.getBorder().g, theme.getBorder().b, glow);
            float gb = border + 4;
            batch.draw(game.getAssets().borderLine, -2, h - gb - 2, w + 4, gb + 4);
            batch.draw(game.getAssets().borderLine, -2, -2, w + 4, gb + 4);
            batch.draw(game.getAssets().borderLine, -2, -2, gb + 4, h + 4);
            batch.draw(game.getAssets().borderLine, w - gb - 2, -2, gb + 4, h + 4);
        }
        batch.setColor(theme.getBorder());
        batch.draw(game.getAssets().borderLine, 0, h - border, w, border);
        batch.draw(game.getAssets().borderLine, 0, 0, w, border);
        batch.draw(game.getAssets().borderLine, 0, 0, border, h);
        batch.draw(game.getAssets().borderLine, w - border, 0, border, h);
        batch.setColor(1, 1, 1, 1);

        // UI
        gameFont.setScale(2f);
        gameFont.drawWithShadow(batch, "Score: " + score, 12, GRID_HEIGHT * CELL_SIZE - 12,
                new Color(0.95f, 0.95f, 0.9f, 1f));

        if (paused) {
            batch.setColor(theme.getOverlay());
            batch.draw(game.getAssets().overlay, 0, 0, w, h);
            batch.setColor(1, 1, 1, 1);
            gameFont.setScale(2.5f);
            gameFont.drawCenteredWithShadow(batch, "PAUSED", w / 2, h / 2 + 24,
                    new Color(0.3f, 0.9f, 0.4f, 1f));
            gameFont.drawCenteredWithShadow(batch, "Press ESC to resume | M for menu", w / 2, h / 2 - 24,
                    new Color(0.9f, 0.9f, 0.85f, 1f));
        } else if (gameOver) {
            batch.setColor(theme.getOverlay());
            batch.draw(game.getAssets().overlay, 0, 0, w, h);
            batch.setColor(1, 1, 1, 1);
            gameFont.setScale(2.5f);
            gameFont.drawCenteredWithShadow(batch, "GAME OVER!", w / 2, h / 2 + 80,
                    new Color(0.95f, 0.2f, 0.2f, 1f));
            if (newHighScore) {
                gameFont.drawCenteredWithShadow(batch, "NEW HIGH SCORE!", w / 2, h / 2 + 40,
                        new Color(1f, 0.85f, 0.2f, 1f));
            }
            gameFont.setScale(1.8f);
            int length = snake.size;
            int secs = (int) playTime;
            String stats = "Score: " + score + "  |  Length: " + length + "  |  Time: " + secs + "s";
            gameFont.drawCenteredWithShadow(batch, stats, w / 2, h / 2,
                    new Color(0.9f, 0.9f, 0.85f, 1f));
            gameFont.drawCenteredWithShadow(batch, "Press SPACE to restart | ESC for menu", w / 2, h / 2 - 40,
                    new Color(0.8f, 0.8f, 0.75f, 1f));
        }
        batch.end();
    }

    private void updateSmoothPositions(float delta) {
        float alpha = Math.min(1f, SMOOTH_LERP * delta);
        for (int i = 0; i < snake.size; i++) {
            Vector2 target = new Vector2(snake.get(i).x * CELL_SIZE, snake.get(i).y * CELL_SIZE);
            displayPositions.get(i).lerp(target, alpha);
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = true;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) requestTurn(0);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) requestTurn(1);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) requestTurn(2);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) requestTurn(3);
    }

    private void requestTurn(int dir) {
        // Use nextDirection (planned direction) so fast combos like UP then LEFT work.
        if (dir == nextDirection) return;
        if (isOpposite(dir, nextDirection)) return;

        if (nextDirection == direction) {
            nextDirection = dir;
        } else {
            queuedDirection = dir;
        }
    }

    private boolean isOpposite(int a, int b) {
        return (a == 0 && b == 2) || (a == 2 && b == 0) || (a == 1 && b == 3) || (a == 3 && b == 1);
    }

    private int dirFromTo(Vector2 from, Vector2 to) {
        if (to.x > from.x) return 1;
        if (to.x < from.x) return 3;
        if (to.y > from.y) return 0;
        return 2;
    }

    /**
     * @return -1 for straight, otherwise corner index:
     * 0 = UP_RIGHT, 1 = RIGHT_DOWN, 2 = DOWN_LEFT, 3 = LEFT_UP
     */
    private int getCornerIndex(int bodyIndex) {
        Vector2 prev = snake.get(bodyIndex - 1);
        Vector2 cur = snake.get(bodyIndex);
        Vector2 next = snake.get(bodyIndex + 1);

        int a = dirFromTo(cur, prev);
        int b = dirFromTo(cur, next);

        if (a == b || isOpposite(a, b)) return -1;

        int min = Math.min(a, b);
        int max = Math.max(a, b);
        if (min == 0 && max == 1) return 0; // UP+RIGHT
        if (min == 1 && max == 2) return 1; // RIGHT+DOWN
        if (min == 2 && max == 3) return 2; // DOWN+LEFT
        return 3; // LEFT+UP (wrap)
    }

    private void handlePausedInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) paused = false;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new FadeInScreen(game, new MenuScreen(game), TRANSITION_DURATION));
        }
    }

    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetGame();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new FadeInScreen(game, new MenuScreen(game), TRANSITION_DURATION));
        }
    }

    private void moveSnake() {
        direction = nextDirection;
        // Apply second buffered turn after we commit the first.
        if (queuedDirection != -1 && !isOpposite(queuedDirection, direction)) {
            nextDirection = queuedDirection;
        }
        queuedDirection = -1;

        Vector2 head = snake.first();
        float newX = head.x;
        float newY = head.y;

        switch (direction) {
            case 0 -> newY += 1;
            case 1 -> newX += 1;
            case 2 -> newY -= 1;
            case 3 -> newX -= 1;
        }

        // Wall collision
        if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) {
            float headX = head.x * CELL_SIZE;
            float headY = head.y * CELL_SIZE;
            particles.emitCrash(headX, headY);
            particles.emitGameOver(headX, headY);
            screenShake.shakeGameOver();
            newHighScore = score > game.getHighScore();
            game.saveHighScore(score);
            gameOver = true;
            return;
        }

        // Self collision
        if (isSnakeAt(newX, newY)) {
            float headX = head.x * CELL_SIZE;
            float headY = head.y * CELL_SIZE;
            particles.emitCrash(headX, headY);
            particles.emitGameOver(headX, headY);
            screenShake.shakeGameOver();
            newHighScore = score > game.getHighScore();
            game.saveHighScore(score);
            gameOver = true;
            return;
        }

        snake.insert(0, new Vector2(newX, newY));
        // Start display at OLD head position so it smoothly slides to new cell
        displayPositions.insert(0, new Vector2(head.x * CELL_SIZE, head.y * CELL_SIZE));

        if (newX == food.x && newY == food.y) {
            // Eat food - effects!
            float foodX = food.x * CELL_SIZE;
            float foodY = food.y * CELL_SIZE;
            particles.emitEat(foodX, foodY, game.getTheme().getFood());
            screenShake.shakeEat();
            eatPulseTimer = EAT_PULSE_DURATION;

            score += 10;
            spawnFood();
        } else {
            snake.removeIndex(snake.size - 1);
            displayPositions.removeIndex(displayPositions.size - 1);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(GRID_WIDTH * CELL_SIZE / 2f, GRID_HEIGHT * CELL_SIZE / 2f, 0);
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
