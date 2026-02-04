package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Wraps a screen with a fade-in overlay. Use when transitioning TO a screen.
 */
public class FadeInScreen implements Screen {

    private final SnakeGame game;
    private final Screen inner;
    private final float duration;
    private float alpha;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    public FadeInScreen(SnakeGame game, Screen inner, float duration) {
        this.game = game;
        this.inner = inner;
        this.duration = duration;
        this.alpha = 1f;
    }

    @Override
    public void show() {
        inner.show();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        inner.render(delta);

        alpha -= delta / duration;
        if (alpha > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, MathUtils.clamp(alpha, 0, 1));
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        inner.resize(width, height);
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() { inner.pause(); }

    @Override
    public void resume() { inner.resume(); }

    @Override
    public void hide() { inner.hide(); }

    @Override
    public void dispose() {
        inner.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
