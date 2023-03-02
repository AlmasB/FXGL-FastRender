/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fastrendertest;

import com.almasb.fastrendertest.buffer.Particle;
import com.almasb.fastrendertest.buffer.ParticleKernel;
import com.almasb.fastrendertest.buffer.WritableImageView;
import com.almasb.fastrendertest.stat.Stats;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.aparapi.Range;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import kotlin.Unit;
import kotlin.system.TimingKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PixelBufferGPUTestApp extends GameApplication {

    private static final int W = 1280;
    private static final int H = 720;

    /**
     * Number of buffers to draw into. Must be at least 2.
     * This should increase performance when the number of particles is not high.
     */
    private static final int BUFFER_SIZE = 3;

    /**
     * The total number of particles in the demo.
     * You can safely change this without the need to change anything else.
     */
    private static final int NUM_PARTICLES_GPU = 10_000_000;

    /**
     * Number of frames to run for benchmark before shutting down.
     */
    private static final int NUM_FRAMES_TO_RUN = 1000;

    /**
     * Stores background pixels in ARGB format.
     */
    private static final int[] BACKGROUND_COLOR_ARRAY = new int[W * H];

    private boolean isProfilingStarted = false;
    private int currentFrame = 0;

    // 2float + 2float + 2float
    private final float[] particlesData = new float[NUM_PARTICLES_GPU * 6];

    /**
     * Stores fully drawn images.
     */
    private BlockingQueue<WritableImageView> fullBuffers = new ArrayBlockingQueue<>(BUFFER_SIZE);

    /**
     * Stores images that can be drawn into.
     */
    private BlockingQueue<WritableImageView> emptyBuffers = new ArrayBlockingQueue<>(BUFFER_SIZE);

    /**
     * Current active (visible) buffer.
     */
    private WritableImageView currentBuffer;

    private List<WritableImageView> gpuBuffers = new ArrayList<>();

    private ParticleKernel kernel;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(W);
        settings.setHeight(H);
    }

    @Override
    protected void initGame() {
        System.out.println("Running with num particles: "+NUM_PARTICLES_GPU);

        // fill the bg array with color black
        Arrays.fill(BACKGROUND_COLOR_ARRAY, Particle.toARGB(Color.BLACK));

        for (int i = 0; i < BUFFER_SIZE; i++) {
            var buffer = new WritableImageView(W, H);
            buffer.gpuIndex = i;
            emptyBuffers.add(buffer);

            gpuBuffers.add(buffer);
        }

        kernel = new ParticleKernel(
                BACKGROUND_COLOR_ARRAY, gpuBuffers.get(0).getPixels(), gpuBuffers.get(1).getPixels(), gpuBuffers.get(2).getPixels(),
                particlesData, 0, 0, W, H);

        for (int i = 0; i < NUM_PARTICLES_GPU; i++) {
            var p = new Particle(i);
            p.position.set(random(200, 500), random(200, 400));
            p.velocity.set(random(-1, 1), random(-1, 1));
            p.acceleration.set(random(-1, 1) * 0.005f, -0.21f * random(0, 1));

            particlesData[i*6+0] = p.position.x;
            particlesData[i*6+1] = p.position.y;

            particlesData[i*6+2] = p.velocity.x;
            particlesData[i*6+3] = p.velocity.y;

            particlesData[i*6+4] = p.acceleration.x;
            particlesData[i*6+5] = p.acceleration.y;
        }

        getExecutor().startAsync(() -> {
            loopInBackground();

            try {
                if (kernel != null) {
                    kernel.dispose();
                }
            } catch (Exception e) {
                System.out.println("Dispose e: " + e);
            }

            getExecutor().startAsyncFX(() -> getGameController().exit());
        });

        runOnce(() -> {
            isProfilingStarted = true;
        }, Duration.seconds(1.5));
    }

    private void loopInBackground() {
        double[] timings = new double[NUM_FRAMES_TO_RUN];

        boolean isRunning = true;

        try {
            while (isRunning) {
                var buffer = emptyBuffers.take();

                var nanos = TimingKt.measureNanoTime(() -> {
                    updateAndDraw(buffer);

                    return Unit.INSTANCE;
                });

                double ms = nanos / 1000000.0;

                if (isProfilingStarted) {
                    timings[currentFrame++] = ms;
                }

                if (currentFrame == NUM_FRAMES_TO_RUN) {
                    Stats.printStats(timings);

                    isRunning = false;
                }

                getExecutor().startAsyncFX(() -> {
                    addUINode(buffer);

                    if (currentBuffer != null) {
                        removeUINode(currentBuffer);
                        emptyBuffers.add(currentBuffer);
                    }

                    currentBuffer = buffer;

                    buffer.updateBuffer();
                });

                // the above asyncFX takes some time to execute on FX thread, which in turn (probably) updates dirty regions on render thread
                // I don't know how to measure that time on render thread, so use arbitrary(-ish) 4.5 to give CPU some time to sleep
                double sleepTime = 3.5 - ms;

                if (sleepTime > 0)
                    Thread.sleep((long) sleepTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this is our callback that runs on a background thread
    private void updateAndDraw(WritableImageView buffer) {
        float mouseX = (float) getInput().getMouseXWorld();
        float mouseY = (float) getInput().getMouseYWorld();

        // CPU-GPU communication code
        kernel.clear(buffer.gpuIndex);
        kernel.execute(Range.create(W*H));

        kernel.set(buffer.gpuIndex, mouseX, mouseY);
        kernel.execute(Range.create(NUM_PARTICLES_GPU));

        if (buffer.gpuIndex == 0) {
            kernel.get(kernel.pixels1);
        } else if (buffer.gpuIndex == 2) {
            kernel.get(kernel.pixels2);
        } else {
            kernel.get(kernel.pixels3);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
