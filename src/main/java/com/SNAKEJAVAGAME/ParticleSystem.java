package com.SNAKEJAVAGAME;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.Random;

/**
 * AAA particle effects - eat, crash, game over.
 */
public class ParticleSystem {

    private static class Particle implements Pool.Poolable {
        Vector2 pos = new Vector2();
        Vector2 vel = new Vector2();
        Color color = new Color();
        float life;
        float maxLife;
        float size;

        @Override
        public void reset() {
            pos.set(0, 0);
            vel.set(0, 0);
            life = 0;
            maxLife = 0;
            size = 0;
        }
    }

    private final Array<Particle> particles = new Array<>();
    private final Pool<Particle> pool = new Pool<Particle>() {
        @Override
        protected Particle newObject() {
            return new Particle();
        }
    };
    private final Random random = new Random();
    private TextureRegion particleTex;

    public void setParticleTexture(TextureRegion tex) {
        this.particleTex = tex;
    }

    public void emitEat(float x, float y, Color baseColor) {
        int count = 12;
        for (int i = 0; i < count; i++) {
            Particle p = pool.obtain();
            p.pos.set(x + 16, y + 16);
            float angle = random.nextFloat() * 6.28f;
            float speed = 40 + random.nextFloat() * 80;
            p.vel.set((float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed);
            p.color.set(baseColor);
            p.maxLife = 0.3f + random.nextFloat() * 0.2f;
            p.life = p.maxLife;
            p.size = 3 + random.nextFloat() * 4;
            particles.add(p);
        }
    }

    public void emitCrash(float x, float y) {
        int count = 20;
        Color c = new Color(0.9f, 0.2f, 0.2f, 1f);
        for (int i = 0; i < count; i++) {
            Particle p = pool.obtain();
            p.pos.set(x + 16, y + 16);
            float angle = random.nextFloat() * 6.28f;
            float speed = 60 + random.nextFloat() * 120;
            p.vel.set((float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed);
            p.color.set(c);
            p.maxLife = 0.4f + random.nextFloat() * 0.3f;
            p.life = p.maxLife;
            p.size = 4 + random.nextFloat() * 5;
            particles.add(p);
        }
    }

    public void emitGameOver(float x, float y) {
        int count = 35;
        for (int i = 0; i < count; i++) {
            Particle p = pool.obtain();
            p.pos.set(x + 16, y + 16);
            float angle = random.nextFloat() * 6.28f;
            float speed = 80 + random.nextFloat() * 150;
            p.vel.set((float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed);
            float r = random.nextFloat();
            if (r < 0.5f) p.color.set(0.9f, 0.2f, 0.2f, 1f);
            else if (r < 0.8f) p.color.set(0.3f, 0.9f, 0.4f, 1f);
            else p.color.set(0.9f, 0.9f, 0.3f, 1f);
            p.maxLife = 0.5f + random.nextFloat() * 0.4f;
            p.life = p.maxLife;
            p.size = 5 + random.nextFloat() * 6;
            particles.add(p);
        }
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.pos.add(p.vel.x * delta, p.vel.y * delta);
            p.vel.scl(0.92f); // drag
            p.life -= delta;
            if (p.life <= 0) {
                particles.removeIndex(i);
                pool.free(p);
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (particleTex == null) return;
        batch.setColor(1, 1, 1, 1);
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            batch.setColor(p.color.r, p.color.g, p.color.b, alpha);
            float half = p.size / 2;
            batch.draw(particleTex, p.pos.x - half, p.pos.y - half, p.size, p.size);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public void clear() {
        for (Particle p : particles) pool.free(p);
        particles.clear();
    }
}
