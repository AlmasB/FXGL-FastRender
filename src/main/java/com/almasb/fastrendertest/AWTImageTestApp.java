/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fastrendertest;

import com.almasb.fastrendertest.buffer.AWTImage;
import com.almasb.fastrendertest.buffer.CanvasIntBuffer;
import com.almasb.fastrendertest.buffer.Particle;
import com.almasb.fastrendertest.stat.Stats;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.image.PixelFormat;
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
public class AWTImageTestApp extends GameApplication {

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
    private static final int NUM_PARTICLES = 1_000_000;

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

    private List<Particle> particles = new ArrayList<>();

    /**
     * Stores fully drawn images.
     */
    private BlockingQueue<AWTImage> fullBuffers = new ArrayBlockingQueue<>(BUFFER_SIZE);

    /**
     * Stores images that can be drawn into.
     */
    private BlockingQueue<AWTImage> emptyBuffers = new ArrayBlockingQueue<>(BUFFER_SIZE);

    /**
     * Current active (visible) buffer.
     */
    private AWTImage currentBuffer;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(W);
        settings.setHeight(H);
    }

    @Override
    protected void initGame() {
        // fill the bg array with color black
        Arrays.fill(BACKGROUND_COLOR_ARRAY, Particle.toARGB(Color.BLACK));

        for (int i = 0; i < BUFFER_SIZE; i++) {
            emptyBuffers.add(new AWTImage(W, H));
        }

        for (int i = 0; i < NUM_PARTICLES; i++) {
            var p = new Particle(i);
            p.position.set(random(200, 500), random(200, 400));
            p.velocity.set(random(-1, 1), random(-1, 1));
            p.acceleration.set(random(-1, 1) * 0.005f, -0.21f * random(0, 1));

            particles.add(p);
        }

        getExecutor().startAsync(() -> {
            loopInBackground();

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

                fullBuffers.add(buffer);

                if (currentFrame == NUM_FRAMES_TO_RUN) {
                    Stats.printStats(timings);

                    isRunning = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this is our callback that runs on a background thread
    private void updateAndDraw(AWTImage buffer) {
        var g = buffer.getGraphicsContext();

        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, W, H);

        g.setColor(Particle.AWT_COLOR);

        double mouseX = getInput().getMouseXWorld();
        double mouseY = getInput().getMouseYWorld();

        int appWidth = getAppWidth();
        int appHeight = getAppHeight();

        // update and draw in parallel, which isn't possible with Canvas
        particles.parallelStream().forEach(p -> {
            p.update(mouseX, mouseY, appWidth, appHeight);

            int x = (int) p.position.x;
            int y = (int) p.position.y;

            g.fillRect(x, y, 1, 1);
        });
    }

    // this is an FXGL callback that runs on JavaFX thread
    @Override
    protected void onUpdate(double tpf) {
        try {
            var buffer = fullBuffers.take();

            addUINode(buffer.getView());

            if (currentBuffer != null) {
                removeUINode(currentBuffer.getView());
                emptyBuffers.add(currentBuffer);
            }

            buffer.render();

            currentBuffer = buffer;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
