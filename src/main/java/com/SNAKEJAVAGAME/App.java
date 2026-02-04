package com.SNAKEJAVAGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class App extends ApplicationAdapter {

    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int CELL_SIZE = 32;
    private static final float MOVE_INTERVAL = 0.12f;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private GameAssets assets;

    private Array<Vector2> snake;
    private Vector2 food;
    private int direction; // 0=UP, 1=RIGHT, 2=DOWN, 3=LEFT
    private int nextDirection;
    private float moveTimer;
    private int score;
    private boolean gameOver;
    private Random random;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        assets = new GameAssets();
        assets.load();

        random = new Random();
        resetGame();
    }

    private void resetGame() {
        snake = new Array<>();
        snake.add(new Vector2(GRID_WIDTH / 2, GRID_HEIGHT / 2));
        snake.add(new Vector2(GRID_WIDTH / 2 - 1, GRID_HEIGHT / 2));
        snake.add(new Vector2(GRID_WIDTH / 2 - 2, GRID_HEIGHT / 2));

        direction = 1; // Start moving right
        nextDirection = 1;
        moveTimer = 0;
        score = 0;
        gameOver = false;

        spawnFood();
    }

    private void spawnFood() {
        do {
            int x = random.nextInt(GRID_WIDTH);
            int y = random.nextInt(GRID_HEIGHT);
            food = new Vector2(x, y);
        } while (isSnakeAt(food.x, food.y));
    }

    private boolean isSnakeAt(float x, float y) {
        for (Vector2 segment : snake) {
            if (segment.x == x && segment.y == y) return true;
        }
        return false;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (gameOver) {
            handleGameOverInput();
        } else {
            handleInput();
            moveTimer += delta;
            if (moveTimer >= MOVE_INTERVAL) {
                moveTimer = 0;
                moveSnake();
            }
        }

        draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && direction != 2) {
            nextDirection = 0;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && direction != 3) {
            nextDirection = 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && direction != 0) {
            nextDirection = 2;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && direction != 1) {
            nextDirection = 3;
        }
    }

    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetGame();
        }
    }

    private void moveSnake() {
        direction = nextDirection;

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
            gameOver = true;
            return;
        }

        // Self collision
        if (isSnakeAt(newX, newY)) {
            gameOver = true;
            return;
        }

        snake.insert(0, new Vector2(newX, newY));

        // Eat food
        if (newX == food.x && newY == food.y) {
            score += 10;
            spawnFood();
        } else {
            snake.removeIndex(snake.size - 1);
        }
    }

    private void draw() {
        // Dark green background
        ScreenUtils.clear(0.06f, 0.1f, 0.06f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw snake body first (so head renders on top)
        for (int i = snake.size - 1; i >= 0; i--) {
            Vector2 segment = snake.get(i);
            float px = segment.x * CELL_SIZE;
            float py = segment.y * CELL_SIZE;

            if (i == 0) {
                batch.draw(assets.snakeHead[direction], px, py, CELL_SIZE, CELL_SIZE);
            } else {
                batch.draw(assets.snakeBody, px, py, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw food
        batch.draw(assets.food, food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // UI - Score with subtle background
        font.setColor(new Color(0.95f, 0.95f, 0.9f, 1f));
        font.draw(batch, "Score: " + score, 12, GRID_HEIGHT * CELL_SIZE - 12);

        if (gameOver) {
            // Semi-transparent overlay
            batch.setColor(1, 1, 1, 1);
            batch.draw(assets.overlay, 0, 0, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

            font.setColor(new Color(0.95f, 0.2f, 0.2f, 1f));
            String gameOverText = "GAME OVER!";
            GlyphLayout layout = new GlyphLayout(font, gameOverText);
            font.draw(batch, gameOverText, (GRID_WIDTH * CELL_SIZE - layout.width) / 2,
                    GRID_HEIGHT * CELL_SIZE / 2 + 24);
            font.setColor(new Color(0.9f, 0.9f, 0.85f, 1f));
            String restartText = "Press SPACE to restart";
            GlyphLayout restartLayout = new GlyphLayout(font, restartText);
            font.draw(batch, restartText, (GRID_WIDTH * CELL_SIZE - restartLayout.width) / 2,
                    GRID_HEIGHT * CELL_SIZE / 2 - 24);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        assets.dispose();
        batch.dispose();
        font.dispose();
    }
}
