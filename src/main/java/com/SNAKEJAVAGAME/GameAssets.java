package com.SNAKEJAVAGAME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Loads custom sprites from assets/ folder when present, otherwise generates them.
 * Put PNG files (32x32) in the assets/ folder: food.png, snake_body.png, etc.
 */
public class GameAssets {

    public static final int TILE_SIZE = 32;

    public Texture gridCell;
    public Texture snakeBody;
    public Texture[] snakeCorner; // 0=UP_RIGHT, 1=RIGHT_DOWN, 2=DOWN_LEFT, 3=LEFT_UP
    public Texture[] snakeHead;  // 0=UP, 1=RIGHT, 2=DOWN, 3=LEFT
    public Texture[] snakeTail;  // 0=UP, 1=RIGHT, 2=DOWN, 3=LEFT (direction tail points)
    public Texture food;
    public Texture overlay;  // Semi-transparent for game over
    public Texture buttonBg;  // For menu buttons
    public Texture borderLine;  // Bright green line for play area border
    public Texture particleTex;  // For particle effects
    public Texture glowTex;  // Soft glow for Neon theme

    public void load() {
        load(Theme.CLASSIC);
    }

    public void load(Theme theme) {
        disposeGameTextures();
        gridCell = createGridCell();
        snakeBody = createSnakeBody(theme);
        snakeCorner = createSnakeCorners(theme);
        snakeHead = createSnakeHeads(theme);
        snakeTail = createSnakeTails(theme);
        food = loadTexture("food.png", () -> createFood(theme));
        overlay = createOverlay();
        buttonBg = createButtonBg();
        borderLine = createBorderLine();
        particleTex = createParticleTexture();
        glowTex = createGlowTexture();
    }

    private void disposeGameTextures() {
        if (snakeBody != null) { snakeBody.dispose(); snakeBody = null; }
        if (snakeCorner != null) { for (Texture t : snakeCorner) t.dispose(); snakeCorner = null; }
        if (snakeHead != null) { for (Texture t : snakeHead) t.dispose(); snakeHead = null; }
        if (snakeTail != null) { for (Texture t : snakeTail) t.dispose(); snakeTail = null; }
        if (food != null) { food.dispose(); food = null; }
    }

    /** Load from assets/ if file exists, otherwise use generator. */
    private Texture loadTexture(String path, java.util.function.Supplier<Texture> fallback) {
        if (Gdx.files.internal(path).exists()) {
            Texture tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return tex;
        }
        return fallback.get();
    }

    public void dispose() {
        if (gridCell != null) gridCell.dispose();
        if (snakeBody != null) snakeBody.dispose();
        if (snakeHead != null) {
            for (Texture t : snakeHead) t.dispose();
        }
        if (snakeTail != null) {
            for (Texture t : snakeTail) t.dispose();
        }
        if (food != null) food.dispose();
        if (overlay != null) overlay.dispose();
        if (buttonBg != null) buttonBg.dispose();
        if (borderLine != null) borderLine.dispose();
        if (particleTex != null) particleTex.dispose();
        if (glowTex != null) glowTex.dispose();
    }

    private Texture createGlowTexture() {
        int size = 64;
        int cx = size / 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();
        for (int r = cx; r >= 0; r--) {
            float alpha = 0.4f * (1f - (float) r / cx) * (1f - (float) r / cx);
            pix.setColor(1f, 1f, 1f, alpha);
            pix.fillCircle(cx, cx, r);
        }
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createParticleTexture() {
        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(1f, 1f, 1f, 1f));
        pix.fill();
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createBorderLine() {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(1f, 1f, 1f, 1f));  // White - tinted by theme
        pix.fill();
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createButtonBg() {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0.2f, 0.5f, 0.3f, 1f));
        pix.fill();
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createOverlay() {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0.05f, 0.08f, 0.05f, 0.75f));
        pix.fill();
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createGridCell() {
        int size = TILE_SIZE;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Dark green base
        pix.setColor(new Color(0.12f, 0.18f, 0.12f, 1f));
        pix.fill();

        // Subtle lighter border (top-right for depth)
        pix.setColor(new Color(0.18f, 0.25f, 0.18f, 1f));
        pix.drawRectangle(0, 0, size, 1);
        pix.drawRectangle(size - 1, 0, 1, size);

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    /** Draws a rounded rectangle (scale-like segment shape). */
    private void fillRoundedRect(Pixmap pix, int x, int y, int w, int h, int r, Color c) {
        pix.setColor(c);
        pix.fillRectangle(x + r, y, w - 2 * r, h);
        pix.fillRectangle(x, y + r, w, h - 2 * r);
        pix.fillCircle(x + r, y + r, r);
        pix.fillCircle(x + w - r - 1, y + r, r);
        pix.fillCircle(x + r, y + h - r - 1, r);
        pix.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    private Texture createSnakeBody(Theme theme) {
        int size = TILE_SIZE;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0));
        pix.fill();

        // Full fill - no outline, edge-to-edge for seamless connection
        pix.setColor(theme.getSnakeBase());
        pix.fillRectangle(0, 0, size, size);

        // 3D shading - light from top-left, shadow bottom-right (cylinder/tube effect)
        pix.setColor(theme.getSnakeHighlight());
        pix.fillRectangle(0, 14, 18, 18);
        pix.setColor(theme.getSnakeShadow());
        pix.fillRectangle(14, 0, 18, 18);

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture[] createSnakeCorners(Theme theme) {
        Texture[] corners = new Texture[4];
        for (int i = 0; i < 4; i++) corners[i] = createSnakeCorner(i, theme);
        return corners;
    }

    /**
     * Corner indices:
     * 0 = UP+RIGHT, 1 = RIGHT+DOWN, 2 = DOWN+LEFT, 3 = LEFT+UP
     */
    private Texture createSnakeCorner(int cornerIndex, Theme theme) {
        int size = TILE_SIZE;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0);
        pix.fill();

        // Base fill (keeps the segment seamless)
        pix.setColor(theme.getSnakeBase());
        pix.fillRectangle(0, 0, size, size);

        // Add a "bent tube" cue: bright in the inner corner, shadow in the opposite corner
        int r = 10;
        int innerX = switch (cornerIndex) {
            case 0, 1 -> size - 1; // right
            default -> 0;          // left
        };
        int innerY = switch (cornerIndex) {
            case 0, 3 -> size - 1; // up
            default -> 0;          // down
        };
        int outerX = size - 1 - innerX;
        int outerY = size - 1 - innerY;

        pix.setColor(theme.getSnakeHighlight());
        pix.fillCircle(innerX, innerY, r);
        pix.setColor(theme.getSnakeShadow());
        pix.fillCircle(outerX, outerY, r);

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture[] createSnakeHeads(Theme theme) {
        Texture[] heads = new Texture[4];
        for (int dir = 0; dir < 4; dir++) {
            heads[dir] = createSnakeHead(dir, theme);
        }
        return heads;
    }

    private Texture createSnakeHead(int direction, Theme theme) {
        int size = TILE_SIZE;
        int cx = size / 2, cy = size / 2;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0));
        pix.fill();

        Color base = theme.getSnakeBase();
        pix.setColor(base.r * 1.3f, base.g * 1.3f, base.b * 1.3f, 1f);
        pix.fillRectangle(0, 0, size, size);
        pix.setColor(theme.getSnakeHighlight());
        pix.fillRectangle(0, 14, 20, 18);
        pix.setColor(theme.getSnakeShadow());
        pix.fillRectangle(12, 0, 20, 18);

        // Eyes
        int eyeW = 5, eyeH = 4;
        pix.setColor(new Color(1f, 1f, 1f, 1f));
        switch (direction) {
            case 0 -> { pix.fillRectangle(cx - 8, cy + 6, eyeW, eyeH); pix.fillRectangle(cx + 3, cy + 6, eyeW, eyeH); }
            case 1 -> { pix.fillRectangle(cx + 6, cy + 3, eyeW, eyeH); pix.fillRectangle(cx + 6, cy - 8, eyeW, eyeH); }
            case 2 -> { pix.fillRectangle(cx - 8, cy - 10, eyeW, eyeH); pix.fillRectangle(cx + 3, cy - 10, eyeW, eyeH); }
            case 3 -> { pix.fillRectangle(cx - 11, cy + 3, eyeW, eyeH); pix.fillRectangle(cx - 11, cy - 8, eyeW, eyeH); }
        }
        pix.setColor(new Color(0.05f, 0.05f, 0.08f, 1f));
        switch (direction) {
            case 0 -> { pix.fillRectangle(cx - 6, cy + 7, 2, 2); pix.fillRectangle(cx + 5, cy + 7, 2, 2); }
            case 1 -> { pix.fillRectangle(cx + 8, cy + 4, 2, 2); pix.fillRectangle(cx + 8, cy - 6, 2, 2); }
            case 2 -> { pix.fillRectangle(cx - 6, cy - 8, 2, 2); pix.fillRectangle(cx + 5, cy - 8, 2, 2); }
            case 3 -> { pix.fillRectangle(cx - 8, cy + 4, 2, 2); pix.fillRectangle(cx - 8, cy - 6, 2, 2); }
        }

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture[] createSnakeTails(Theme theme) {
        Texture[] tails = new Texture[4];
        for (int dir = 0; dir < 4; dir++) {
            tails[dir] = createSnakeTail(dir, theme);
        }
        return tails;
    }

    private Texture createSnakeTail(int direction, Theme theme) {
        int size = TILE_SIZE;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0));
        pix.fill();

        // Full fill - seamless with body (slightly darker than base)
        Color base = theme.getSnakeBase();
        pix.setColor(base.r * 0.85f, base.g * 0.9f, base.b * 0.85f, 1f);
        pix.fillRectangle(0, 0, size, size);
        // 3D shading
        pix.setColor(theme.getSnakeHighlight());
        pix.fillRectangle(0, 14, 16, 18);
        pix.setColor(theme.getSnakeShadow());
        pix.fillRectangle(16, 0, 16, 18);

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private Texture createFood(Theme theme) {
        int size = TILE_SIZE;
        int pad = 3;
        int r = 8;
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0));
        pix.fill();

        Color foodColor = theme.getFood();
        fillRoundedRect(pix, pad, pad, size - 2 * pad, size - 2 * pad, r,
                new Color(foodColor.r * 0.6f, foodColor.g * 0.6f, foodColor.b * 0.6f, 1f));
        fillRoundedRect(pix, pad + 1, pad + 1, size - 2 * pad - 2, size - 2 * pad - 2, r - 1, foodColor);
        pix.setColor(new Color(foodColor.r + 0.3f, foodColor.g + 0.3f, foodColor.b + 0.3f, 0.6f));
        pix.fillRectangle(6, 8, 10, 6);

        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }
}
