package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * Main menu - Start Game, Options, Exit.
 * With transitions, button feedback, and game-style font.
 */
public class MenuScreen implements Screen {

    private final SnakeGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private GameFont gameFont;
    private GlyphLayout layout;

    private float viewWidth;
    private float viewHeight;

    private static final float BUTTON_WIDTH = 280;
    private static final float BUTTON_HEIGHT = 50;
    private static final float BUTTON_SPACING = 20;
    private static final float TRANSITION_DURATION = 0.35f;

    private int hoveredButton = -1;
    private int pressedButton = -1;

    public MenuScreen(SnakeGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewWidth = Gdx.graphics.getWidth();
        viewHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);

        batch = new SpriteBatch();
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        gameFont = new GameFont(font);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        Color bg = game.getTheme().getBackground();
        com.badlogic.gdx.utils.ScreenUtils.clear(bg.r, bg.g, bg.b, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        gameFont.setScale(3f);
        gameFont.drawCenteredWithShadow(batch, "SNAKE", viewWidth / 2, viewHeight - 80,
                new Color(0.3f, 0.9f, 0.4f, 1f));

        gameFont.setScale(1.5f);
        gameFont.drawCenteredWithShadow(batch, "High Score: " + game.getHighScore(), viewWidth / 2, viewHeight - 130,
                new Color(0.6f, 0.8f, 0.6f, 1f));

        gameFont.setScale(2f);

        // Buttons - centered
        float startY = viewHeight / 2 + 40;
        float buttonLeft = (viewWidth - BUTTON_WIDTH) / 2;

        // Update hover/press state
        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touch);
        hoveredButton = -1;
        for (int i = 0; i < 3; i++) {
            float buttonTop = startY - i * (BUTTON_HEIGHT + BUTTON_SPACING);
            float buttonBottom = buttonTop - BUTTON_HEIGHT;
            if (touch.x >= buttonLeft && touch.x <= buttonLeft + BUTTON_WIDTH &&
                    touch.y <= buttonTop && touch.y >= buttonBottom) {
                hoveredButton = i;
                break;
            }
        }
        if (Gdx.input.isTouched()) {
            pressedButton = hoveredButton;
        } else {
            pressedButton = -1;
        }

        for (int i = 0; i < 3; i++) {
            float buttonTop = startY - i * (BUTTON_HEIGHT + BUTTON_SPACING);
            drawButton("Start Game", "Options", "Exit", i, buttonTop, buttonLeft);
        }

        batch.end();

        // Handle click (on touch down)
        if (Gdx.input.justTouched()) {
            if (hoveredButton >= 0) {
                handleClick(hoveredButton);
            }
        }
    }

    private void drawButton(String startText, String optionsText, String exitText, int index, float topY, float left) {
        String text = switch (index) {
            case 0 -> startText;
            case 1 -> optionsText;
            default -> exitText;
        };
        float bottomY = topY - BUTTON_HEIGHT;

        // Button feedback: scale and color
        float scale = 1f;
        float r = 1f, g = 1f, b = 0.9f;
        if (pressedButton == index) {
            scale = 0.96f;
            r = 0.85f; g = 0.9f; b = 0.8f;
        } else if (hoveredButton == index) {
            scale = 1.02f;
            r = 1f; g = 1f; b = 1f;
        }

        float w = BUTTON_WIDTH * scale;
        float h = BUTTON_HEIGHT * scale;
        float ox = (BUTTON_WIDTH - w) / 2;
        float oy = (BUTTON_HEIGHT - h) / 2;

        batch.setColor(r, g, b, 1f);
        batch.draw(game.getAssets().buttonBg, left + ox, bottomY + oy, w, h);
        batch.setColor(1, 1, 1, 1);

        layout.setText(gameFont.getFont(), text);
        gameFont.drawCenteredWithShadow(batch, text, viewWidth / 2, bottomY + (BUTTON_HEIGHT + layout.height) / 2 - 4);
    }

    private void handleClick(int button) {
        switch (button) {
            case 0 -> game.setScreen(new FadeInScreen(game, new GameScreen(game), TRANSITION_DURATION));
            case 1 -> game.setScreen(new FadeInScreen(game, new OptionsScreen(game), TRANSITION_DURATION));
            case 2 -> Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewWidth = width;
        viewHeight = height;
        camera.setToOrtho(false, width, height);
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
        gameFont.getFont().dispose();
    }
}
