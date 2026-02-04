package com.SNAKEJAVAGAME;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Game-style font rendering with shadow for a polished look.
 */
public class GameFont {

    private final BitmapFont font;
    private final GlyphLayout layout;
    private static final float SHADOW_OFFSET = 2f;

    public GameFont(BitmapFont font) {
        this.font = font;
        this.layout = new GlyphLayout();
    }

    public void setScale(float scale) {
        font.getData().setScale(scale);
    }

    /** Draw text with a subtle shadow for depth. */
    public void drawWithShadow(SpriteBatch batch, CharSequence str, float x, float y) {
        layout.setText(font, str);
        font.setColor(0.05f, 0.05f, 0.08f, 0.8f);
        font.draw(batch, str, x + SHADOW_OFFSET, y - SHADOW_OFFSET);
        font.setColor(Color.WHITE);
        font.draw(batch, str, x, y);
    }

    /** Draw text with shadow and custom color. */
    public void drawWithShadow(SpriteBatch batch, CharSequence str, float x, float y, Color color) {
        layout.setText(font, str);
        font.setColor(color.r * 0.3f, color.g * 0.3f, color.b * 0.3f, 0.8f);
        font.draw(batch, str, x + SHADOW_OFFSET, y - SHADOW_OFFSET);
        font.setColor(color);
        font.draw(batch, str, x, y);
    }

    /** Draw centered text with shadow. */
    public void drawCenteredWithShadow(SpriteBatch batch, CharSequence str, float centerX, float y) {
        layout.setText(font, str);
        float x = centerX - layout.width / 2;
        drawWithShadow(batch, str, x, y);
    }

    /** Draw centered text with shadow and color. */
    public void drawCenteredWithShadow(SpriteBatch batch, CharSequence str, float centerX, float y, Color color) {
        layout.setText(font, str);
        float x = centerX - layout.width / 2;
        drawWithShadow(batch, str, x, y, color);
    }

    public GlyphLayout getLayout() {
        return layout;
    }

    public BitmapFont getFont() {
        return font;
    }
}
