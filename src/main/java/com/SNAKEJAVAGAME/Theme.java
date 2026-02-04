package com.SNAKEJAVAGAME;

import com.badlogic.gdx.graphics.Color;

/**
 * Visual themes for the Snake game - AAA polish!
 */
public enum Theme {
    CLASSIC, NEON, DESERT;

    public Color getBackground() {
        return switch (this) {
            case CLASSIC -> new Color(0.06f, 0.1f, 0.06f, 1f);
            case NEON -> new Color(0.02f, 0.02f, 0.08f, 1f);
            case DESERT -> new Color(0.25f, 0.18f, 0.1f, 1f);
        };
    }

    public Color getSnakeBase() {
        return switch (this) {
            case CLASSIC -> new Color(0.18f, 0.58f, 0.25f, 1f);
            case NEON -> new Color(0.1f, 0.95f, 0.5f, 1f);
            case DESERT -> new Color(0.55f, 0.45f, 0.2f, 1f);
        };
    }

    public Color getSnakeHighlight() {
        return switch (this) {
            case CLASSIC -> new Color(0.35f, 0.8f, 0.45f, 0.5f);
            case NEON -> new Color(0.4f, 1f, 0.7f, 0.6f);
            case DESERT -> new Color(0.75f, 0.65f, 0.35f, 0.5f);
        };
    }

    public Color getSnakeShadow() {
        return switch (this) {
            case CLASSIC -> new Color(0.08f, 0.35f, 0.12f, 0.4f);
            case NEON -> new Color(0.05f, 0.6f, 0.3f, 0.5f);
            case DESERT -> new Color(0.35f, 0.25f, 0.1f, 0.4f);
        };
    }

    public Color getFood() {
        return switch (this) {
            case CLASSIC -> new Color(0.9f, 0.2f, 0.2f, 1f);
            case NEON -> new Color(1f, 0.2f, 0.8f, 1f);
            case DESERT -> new Color(0.9f, 0.4f, 0.1f, 1f);
        };
    }

    public Color getBorder() {
        return switch (this) {
            case CLASSIC -> new Color(0.3f, 0.9f, 0.4f, 1f);
            case NEON -> new Color(0.2f, 1f, 0.6f, 1f);
            case DESERT -> new Color(0.8f, 0.6f, 0.25f, 1f);
        };
    }

    public Color getOverlay() {
        return switch (this) {
            case CLASSIC -> new Color(0.05f, 0.08f, 0.05f, 0.75f);
            case NEON -> new Color(0.02f, 0.02f, 0.1f, 0.8f);
            case DESERT -> new Color(0.15f, 0.1f, 0.05f, 0.75f);
        };
    }
}
