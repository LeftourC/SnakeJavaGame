package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

/**
 * Main game class - manages screens (Menu, Options, Game).
 */
public class SnakeGame extends Game {

    public static final String PREFS_NAME = "snake_prefs";
    public static final String PREF_FULLSCREEN = "fullscreen";
    public static final String PREF_WIDTH = "width";
    public static final String PREF_HEIGHT = "height";
    public static final String PREF_THEME = "theme";
     public static final String PREF_HIGH_SCORE = "high_score";

    public static final int[][] WINDOW_SIZES = {
            {640, 640},
            {800, 800},
            {960, 960},
            {1280, 720},
            {1920, 1080}
    };

    private Preferences prefs;
    private GameAssets assets;

    @Override
    public void create() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        assets = new GameAssets();
        assets.load(getTheme());

        applyDisplaySettings();
        setScreen(new FadeInScreen(this, new MenuScreen(this), 0.35f));
    }

    @Override
    public void setScreen(Screen screen) {
        Screen old = getScreen();
        if (old != null) {
            old.dispose();
        }
        super.setScreen(screen);
    }

    public Preferences getPreferences() {
        return prefs;
    }

    public GameAssets getAssets() {
        return assets;
    }

    public Theme getTheme() {
        String name = prefs.getString(PREF_THEME, "CLASSIC");
        try {
            return Theme.valueOf(name);
        } catch (Exception e) {
            return Theme.CLASSIC;
        }
    }

    public int getHighScore() {
        return prefs.getInteger(PREF_HIGH_SCORE, 0);
    }

    public void saveHighScore(int score) {
        if (score > getHighScore()) {
            prefs.putInteger(PREF_HIGH_SCORE, score);
            prefs.flush();
        }
    }

    public void applyDisplaySettings() {
        boolean fullscreen = prefs.getBoolean(PREF_FULLSCREEN, false);
        int width = prefs.getInteger(PREF_WIDTH, 640);
        int height = prefs.getInteger(PREF_HEIGHT, 640);

        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

    @Override
    public void render() {
        // Clear full screen with background color (fills letterboxing areas)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Theme theme = getTheme();
        Gdx.gl.glClearColor(theme.getBackground().r, theme.getBackground().g, theme.getBackground().b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }

    @Override
    public void dispose() {
        if (assets != null) assets.dispose();
    }
}
