package com.SNAKEJAVAGAME;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Main {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Snake");
        config.setWindowedMode(640, 640);

        new Lwjgl3Application(new SnakeGame(), config);
    }
}