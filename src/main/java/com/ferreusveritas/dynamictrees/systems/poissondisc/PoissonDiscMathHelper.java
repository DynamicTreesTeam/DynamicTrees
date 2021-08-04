package com.ferreusveritas.dynamictrees.systems.poissondisc;

public class PoissonDiscMathHelper {

    /**
     * Wrap between 0(inclusive) and max(exclusive)
     *
     * @param i   The parameter to wrap
     * @param max The value(must be positive) that if met or exceeded will wrap around
     * @return The wrapped value
     */
    public static int wrap(int i, int max) {
        while (i < 0) {
            i += max;
        }
        return i % max;
    }

    public static double wrapAngle(double angle) {
        final double TwoPi = Math.PI * 2;
        angle %= TwoPi;//Wrap angle
        return angle + (angle < 0 ? TwoPi : 0);//Convert negative angle to positive
    }

    /**
     * Convert Range [0, PI * 2] to [0, 1]
     *
     * @param angle The angle to convert [0, PI * 2] (angle will be wrapped to this range)
     * @return range [0, 1]
     */
    public static float radiansToTurns(double angle) {
        return (float) (wrapAngle(angle) / (Math.PI * 2));
    }

    /**
     * Length (angular) of a shortest way between two angles.
     *
     * @param alpha First angle in range [0, PI * 2] (input will be wrapped to range)
     * @param beta  Second angle in range [0, PI * 2] (input will be wrapped to range)
     * @return Shorted Delta angle in range [0, PI]
     */
    public static double deltaAngle(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % (Math.PI * 2);// This is either the distance or 360 - distance
        return phi > Math.PI ? (Math.PI * 2) - phi : phi;
    }

}
