/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fastrendertest.buffer;

import com.almasb.fxgl.core.math.Vec2;
import javafx.scene.paint.Color;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class Particle {

    public static final Color COLOR = Color.rgb(155, 154, 40, 0.85);

    // for some reason setting this to 0.85 alpha makes AWT Graphics2D really slow, hence use 1.0 alpha
    public static final java.awt.Color AWT_COLOR = new java.awt.Color(155, 154, 40, 255);

    public static final int ARGB_COLOR = toARGB(COLOR);

    public final Vec2 position = new Vec2();

    public final Vec2 velocity = new Vec2();

    public final Vec2 acceleration = new Vec2();

    public final int index;

    public Particle(int index) {
        this.index = index;
    }

    public void update(double mouseX, double mouseY, int appWidth, int appHeight) {
        // inlined, but not sure if this actually affects performance
        position.x += velocity.x;
        position.y += velocity.y;

        velocity.x += acceleration.x;
        velocity.y += acceleration.y;

        double vx = mouseX - position.x;
        double vy = mouseY - position.y;

        double distance_squared = vx * vx + vy * vy;

        vx = vx * 55 / distance_squared;
        vy = vy * 55 / distance_squared;

        acceleration.x = (float) vx;
        acceleration.y = (float) vy;

        if (position.x < 0) {
            position.x = 0;
            velocity.x = 1;
        } else if (position.x + 1 > appWidth) {
            position.x = appWidth - 1;
            velocity.x = -1;
        }

        if (position.y < 0) {
            position.y = 0;
            velocity.y = 1;
        } else if (position.y + 1 > appHeight) {
            position.y = appHeight - 1;
            velocity.y = -1;
        }
    }

    public static int toARGB(Color color) {
        return (int) (color.getOpacity() * 255) << 24
                | (int) (color.getRed() * 255) << 16
                | (int) (color.getGreen() * 255) <<  8
                | (int) (color.getBlue() * 255);
    }

    public static int add(int colorA, int colorB) {
        int aA = (colorA >> 24) & 0xFF;
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = (colorA >> 0) & 0xFF;

        int aB = (colorB >> 24) & 0xFF;
        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = (colorB >> 0) & 0xFF;

        int aC = Math.min(aA + aB, 255);
        int rC = Math.min(rA + rB, 255);
        int gC = Math.min(gA + gB, 255);
        int bC = Math.min(bA + bB, 255);

        return (aC << 24) | (rC << 16) | (gC << 8) | bC;
    }
}