package com.almasb.fastrendertest.stat;

import java.util.Arrays;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public final class Stats {

    public static void printStats(double[] timeData) {
        Arrays.sort(timeData);

        System.out.println("Min: " + timeData[0]);
        System.out.println("Avg: " + Arrays.stream(timeData).average().getAsDouble());
        System.out.println("95%: " + timeData[(int)(timeData.length * 0.95)]);
        System.out.println("99%: " + timeData[(int)(timeData.length * 0.99)]);
        System.out.println("Max: " + timeData[timeData.length - 1]);
    }
}
