package com.almasb.fastrendertest.buffer;

import com.aparapi.Kernel;
import javafx.scene.paint.Color;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class ParticleKernel extends Kernel {

    private static final int COLOR = Particle.toARGB(Color.BLUE);

    public final int[] BG_PIXELS;

    public int[] pixels1;
    public int[] pixels2;
    public int[] pixels3;

    private final float[] particlesData;

    private int[] gpuIndex = new int[1];
    private float mouseX;
    private float mouseY;
    private final int appWidth;
    private final int appHeight;

    private int[] kernelMode = new int[1];

    public ParticleKernel(int[] bgPixels, int[] pixels1, int[] pixels2, int[] pixels3, float[] particlesData, float mouseX, float mouseY, int appWidth, int appHeight) {
        BG_PIXELS = bgPixels;
        this.pixels1 = pixels1;
        this.pixels2 = pixels2;
        this.pixels3 = pixels3;
        this.particlesData = particlesData;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.appWidth = appWidth;
        this.appHeight = appHeight;

        // set explicit video ram management and send the data to GPU once
        setExplicit(true);
        put(BG_PIXELS);
        put(particlesData);
        put(pixels1);
        put(pixels2);
        put(pixels3);
    }

    public void set(int gpuIndex, float mouseX, float mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        this.gpuIndex[0] = gpuIndex;
        kernelMode[0] = 1;

        put(kernelMode);
        put(this.gpuIndex);
    }

    public void clear(int gpuIndex) {
        this.gpuIndex[0] = gpuIndex;
        kernelMode[0] = 0;

        put(kernelMode);
        put(this.gpuIndex);
    }

    @Override
    public void run() {
        if (kernelMode[0] == 0) {
            int gid = this.getGlobalId();

            if (gpuIndex[0] == 0) {
                pixels1[gid] = BG_PIXELS[gid];
            } else if (gpuIndex[0] == 1) {
                pixels2[gid] = BG_PIXELS[gid];
            } else {
                pixels3[gid] = BG_PIXELS[gid];
            }

            return;
        }

        int gid = this.getGlobalId() * 6;

        particlesData[gid + 0] += particlesData[gid + 2];
        particlesData[gid + 1] += particlesData[gid + 3];

        particlesData[gid + 2] += particlesData[gid + 4];
        particlesData[gid + 3] += particlesData[gid + 5];

        float vx = mouseX - particlesData[gid + 0];
        float vy = mouseY - particlesData[gid + 1];

        float distance_squared = vx * vx + vy * vy;

        vx = vx * 55 / distance_squared;
        vy = vy * 55 / distance_squared;

        particlesData[gid + 4] = vx;
        particlesData[gid + 5] = vy;

        if (particlesData[gid + 0] < 0) {
            particlesData[gid + 0] = 0;
            particlesData[gid + 2] = 1;
        } else if (particlesData[gid + 0] + 1 > appWidth) {
            particlesData[gid + 0] = appWidth - 1;
            particlesData[gid + 2] = -1;
        }

        if (particlesData[gid + 1] < 0) {
            particlesData[gid + 1] = 0;
            particlesData[gid + 3] = 1;
        } else if (particlesData[gid + 1] + 1 > appHeight) {
            particlesData[gid + 1] = appHeight - 1;
            particlesData[gid + 3] = -1;
        }

        int x = (int) particlesData[gid + 0];
        int y = (int) particlesData[gid + 1];

        if (gpuIndex[0] == 0) {
            pixels1[(x % appWidth) + (y * appWidth)] = COLOR;
        } else if (gpuIndex[0] == 1) {
            pixels2[(x % appWidth) + (y * appWidth)] = COLOR;
        } else {
            pixels3[(x % appWidth) + (y * appWidth)] = COLOR;
        }
    }
}
