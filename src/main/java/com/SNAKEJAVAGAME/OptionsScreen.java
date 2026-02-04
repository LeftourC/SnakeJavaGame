package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Options menu - Window mode (Fullscreen/Windowed), Size selection, Back.
 */
public class OptionsScreen implements Screen {

    private final SnakeGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private GameFont gameFont;
    private GlyphLayout layout;

    private float viewWidth;
    private float viewHeight;

    private static final float BUTTON_WIDTH = 280;
    private static final float BUTTON_HEIGHT = 50;
    private static final float BUTTON_SPACING = 16;
    private static final float SMALL_BUTTON_WIDTH = 140;
    private static final float TRANSITION_DURATION = 0.35f;

    private boolean backHovered;
    private boolean backPressed;

    public OptionsScreen(SnakeGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewWidth = Gdx.graphics.getWidth();
        viewHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        gameFont = new GameFont(font);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(game.getTheme().getBackground());

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        gameFont.setScale(2.5f);
        gameFont.drawCenteredWithShadow(batch, "OPTIONS", viewWidth / 2, viewHeight - 60,
                new Color(0.3f, 0.9f, 0.4f, 1f));

        font.getData().setScale(1.5f);

        float startY = viewHeight / 2 + 60;

        // Window Mode section
        font.draw(batch, "Window Mode:", 60, startY);
        float modeY = startY - 40;
        boolean fullscreen = game.getPreferences().getBoolean(SnakeGame.PREF_FULLSCREEN, false);
        drawSmallButton("Fullscreen", 60, modeY - BUTTON_HEIGHT, fullscreen);
        drawSmallButton("Windowed", 220, modeY - BUTTON_HEIGHT, !fullscreen);

        // Theme section
        float themeY = modeY - 80;
        font.draw(batch, "Theme:", 60, themeY);
        float themeButtonY = themeY - 40;
        Theme currentTheme = game.getTheme();
        drawSmallButton("Classic", 60, themeButtonY, currentTheme == Theme.CLASSIC);
        drawSmallButton("Neon", 220, themeButtonY, currentTheme == Theme.NEON);
        drawSmallButton("Desert", 380, themeButtonY, currentTheme == Theme.DESERT);

        // Size section
        float sizeY = themeY - 80;
        font.draw(batch, "Window Size:", 60, sizeY);
        float sizeButtonY = sizeY - 40;

        int prefWidth = game.getPreferences().getInteger(SnakeGame.PREF_WIDTH, 640);
        int prefHeight = game.getPreferences().getInteger(SnakeGame.PREF_HEIGHT, 640);
        int selectedSize = findSizeIndex(prefWidth, prefHeight);

        for (int i = 0; i < SnakeGame.WINDOW_SIZES.length; i++) {
            float x = 60 + (i % 3) * (SMALL_BUTTON_WIDTH + 20);
            int row = i / 3;
            float y = sizeButtonY - row * (BUTTON_HEIGHT + BUTTON_SPACING);
            String label = SnakeGame.WINDOW_SIZES[i][0] + "x" + SnakeGame.WINDOW_SIZES[i][1];
            drawSmallButton(label, x, y, selectedSize == i);
        }

        // Back button
        float backY = 80;
        float backLeft = (viewWidth - BUTTON_WIDTH) / 2;
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        backHovered = touchPos.x >= backLeft && touchPos.x <= backLeft + BUTTON_WIDTH &&
                touchPos.y >= backY && touchPos.y <= backY + BUTTON_HEIGHT;
        backPressed = backHovered && Gdx.input.isTouched();
        drawButton("Back", backY, backHovered, backPressed);

        batch.end();

        // Handle click
        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);

            // Theme buttons
            if (touch.y >= themeButtonY && touch.y <= themeButtonY + BUTTON_HEIGHT) {
                if (touch.x >= 60 && touch.x <= 60 + SMALL_BUTTON_WIDTH) {
                    game.getPreferences().putString(SnakeGame.PREF_THEME, Theme.CLASSIC.name());
                    game.getPreferences().flush();
                    game.getAssets().load(Theme.CLASSIC);
                } else if (touch.x >= 220 && touch.x <= 220 + SMALL_BUTTON_WIDTH) {
                    game.getPreferences().putString(SnakeGame.PREF_THEME, Theme.NEON.name());
                    game.getPreferences().flush();
                    game.getAssets().load(Theme.NEON);
                } else if (touch.x >= 380 && touch.x <= 380 + SMALL_BUTTON_WIDTH) {
                    game.getPreferences().putString(SnakeGame.PREF_THEME, Theme.DESERT.name());
                    game.getPreferences().flush();
                    game.getAssets().load(Theme.DESERT);
                }
            }

            // Fullscreen / Windowed buttons
            if (touch.y >= modeY - BUTTON_HEIGHT - 10 && touch.y <= modeY + 10) {
                if (touch.x >= 60 && touch.x <= 60 + SMALL_BUTTON_WIDTH) {
                    game.getPreferences().putBoolean(SnakeGame.PREF_FULLSCREEN, true);
                    game.getPreferences().flush();
                    game.applyDisplaySettings();
                } else if (touch.x >= 220 && touch.x <= 220 + SMALL_BUTTON_WIDTH) {
                    game.getPreferences().putBoolean(SnakeGame.PREF_FULLSCREEN, false);
                    game.getPreferences().flush();
                    game.applyDisplaySettings();
                }
            }

            // Size buttons
            for (int i = 0; i < SnakeGame.WINDOW_SIZES.length; i++) {
                float x = 60 + (i % 3) * (SMALL_BUTTON_WIDTH + 20);
                int row = i / 3;
                float y = sizeButtonY - row * (BUTTON_HEIGHT + BUTTON_SPACING);
                float top = y + BUTTON_HEIGHT;
                float bottom = y;

                if (touch.x >= x && touch.x <= x + SMALL_BUTTON_WIDTH &&
                        touch.y >= bottom && touch.y <= top) {
                    game.getPreferences().putInteger(SnakeGame.PREF_WIDTH, SnakeGame.WINDOW_SIZES[i][0]);
                    game.getPreferences().putInteger(SnakeGame.PREF_HEIGHT, SnakeGame.WINDOW_SIZES[i][1]);
                    game.getPreferences().putBoolean(SnakeGame.PREF_FULLSCREEN, false);
                    game.getPreferences().flush();
                    game.applyDisplaySettings();
                    break;
                }
            }

            // Back button
            if (backHovered) {
                game.setScreen(new FadeInScreen(game, new MenuScreen(game), TRANSITION_DURATION));
            }
        }
    }

    private int findSizeIndex(int width, int height) {
        for (int i = 0; i < SnakeGame.WINDOW_SIZES.length; i++) {
            if (SnakeGame.WINDOW_SIZES[i][0] == width && SnakeGame.WINDOW_SIZES[i][1] == height) {
                return i;
            }
        }
        return 0;
    }

    private void drawSmallButton(String text, float x, float y, boolean selected) {
        batch.setColor(selected ? 1f : 0.75f, selected ? 1f : 0.85f, selected ? 0.9f : 0.7f, 1f);
        batch.draw(game.getAssets().buttonBg, x, y, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        font.setColor(Color.WHITE);
        layout.setText(font, text);
        font.draw(batch, text, x + (SMALL_BUTTON_WIDTH - layout.width) / 2, y + (BUTTON_HEIGHT + layout.height) / 2 - 4);
    }

    private void drawButton(String text, float topY, boolean hovered, boolean pressed) {
        float left = (viewWidth - BUTTON_WIDTH) / 2;
        float bottomY = topY;

        float scale = pressed ? 0.96f : (hovered ? 1.02f : 1f);
        float r = pressed ? 0.85f : (hovered ? 1f : 1f);
        float g = pressed ? 0.9f : (hovered ? 1f : 1f);
        float b = pressed ? 0.8f : (hovered ? 1f : 0.9f);
        float w = BUTTON_WIDTH * scale;
        float h = BUTTON_HEIGHT * scale;
        float ox = (BUTTON_WIDTH - w) / 2;
        float oy = (BUTTON_HEIGHT - h) / 2;

        batch.setColor(r, g, b, 1f);
        batch.draw(game.getAssets().buttonBg, left + ox, bottomY + oy, w, h);
        batch.setColor(1, 1, 1, 1);

        layout.setText(font, text);
        gameFont.drawCenteredWithShadow(batch, text, viewWidth / 2, bottomY + (BUTTON_HEIGHT + layout.height) / 2 - 4);
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
        font.dispose();
    }
}
