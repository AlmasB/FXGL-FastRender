package com.almasb.fastrendertest.buffer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class CanvasIntBuffer {
    public Canvas canvas;
    public GraphicsContext g;

    public int[] rawInts;

    public int gpuIndex = 0;

    public CanvasIntBuffer(int width, int height) {
        canvas = new Canvas(width, height);
        g = canvas.getGraphicsContext2D();

        rawInts = new int[width * height];
    }

    public int[] getPixels() {
        return rawInts;
    }
}
