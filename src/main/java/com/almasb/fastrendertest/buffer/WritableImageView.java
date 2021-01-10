/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fastrendertest.buffer;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.nio.IntBuffer;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class WritableImageView extends ImageView {
    private int[] rawInts;

    private int width;
    private int height;

    private IntBuffer buffer;
    private PixelBuffer<IntBuffer> pixelBuffer;

    public int gpuIndex = 0;

    public WritableImageView(int width, int height) {
        this.width = width;
        this.height = height;

        buffer = IntBuffer.allocate(width * height);
        rawInts = buffer.array();

        pixelBuffer = new PixelBuffer<>(width, height, buffer, PixelFormat.getIntArgbPreInstance());

        setImage(new WritableImage(pixelBuffer));
    }

    public int[] getPixels() {
        return rawInts;
    }

    /**
     * Set all pixels from given buffer into this image's buffer.
     */
    public void setPixels(int[] rawPixels) {
        System.arraycopy(rawPixels, 0, rawInts, 0, rawPixels.length);
    }

    /**
     * Set a single pixel of this image's buffer at x, y to given ARGB color.
     */
    public void setArgb(int x, int y, int colorARGB) {
        rawInts[(x % width) + (y * width)] = colorARGB;
    }

    /**
     * Draw all of the ARGB pixels into this image.
     */
    public void updateBuffer() {
        pixelBuffer.updateBuffer(b -> null);
    }
}