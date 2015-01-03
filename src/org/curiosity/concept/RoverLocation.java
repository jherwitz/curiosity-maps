package org.curiosity.concept;

import com.google.common.base.Preconditions;

import java.util.Date;

/**
 * The location of the Curiosity rover during a specific martian day,
 * relative to the planned MSL landing site.
 *
 * @author jherwitz
 */
public class RoverLocation {

    // primary key
    private final int sol; // TODO: caclulate this out with mars solar period, instead of just trusting spice
    private final double x; // kilometers
    private final double y; // kilometers
    private final double z; // kilometers
    private final String timestamp; // utc timestamp reported by spice

    public RoverLocation(int sol, double x, double y, double z, String timestamp) {
        this.sol = sol;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = Preconditions.checkNotNull(timestamp);
    }

    public int sol() {
        return sol;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public String timestamp() {
        return timestamp;
    }
}
