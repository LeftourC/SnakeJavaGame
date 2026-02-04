package com.SNAKEJAVAGAME;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

/**
 * AAA screen shake - eat (light), crash (heavy), game over (intense).
 */
public class ScreenShake {

    private final OrthographicCamera camera;
    private final Vector2 basePosition = new Vector2();
    private final Vector2 offset = new Vector2();
    private final Random random = new Random();

    private float intensity;
    private float decay = 8f;

    public ScreenShake(OrthographicCamera camera) {
        this.camera = camera;
        basePosition.set(camera.position.x, camera.position.y);
    }

    public void setBasePosition(float x, float y) {
        basePosition.set(x, y);
    }

    public void shake(float intensity) {
        this.intensity = Math.max(this.intensity, intensity);
    }

    public void shakeEat() {
        shake(2f);
    }

    public void shakeCrash() {
        shake(5f);
    }

    public void shakeGameOver() {
        shake(8f);
    }

    public void update(float delta) {
        if (intensity > 0) {
            offset.x = (random.nextFloat() - 0.5f) * 2 * intensity;
            offset.y = (random.nextFloat() - 0.5f) * 2 * intensity;
            camera.position.set(basePosition.x + offset.x, basePosition.y + offset.y, 0);
            camera.update();
            intensity -= decay * delta;
            if (intensity < 0) intensity = 0;
        } else {
            camera.position.set(basePosition.x, basePosition.y, 0);
            camera.update();
        }
    }
}
