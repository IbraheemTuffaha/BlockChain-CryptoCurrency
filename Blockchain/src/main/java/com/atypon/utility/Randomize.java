package com.atypon.utility;

import java.util.Random;

/**
 * A utility class to help with testing the code.
 */
public final class Randomize {
    // Get a random positive double
    public static double randDouble() {
        return Math.abs(new Random().nextDouble());
    }

    // Get a random percentage number (positive and less than or equal one)
    public static double randRatio() {
        return Math.abs(Math.abs(new Random().nextInt()) % 101 / 100.0);
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private Randomize() {
    }
}
