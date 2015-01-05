package org.curiosity.concept;

import com.google.common.base.Preconditions;

/**
 * The location of the Curiosity rover during a specific sol.
 *
 * @author jherwitz
 */
public class RoverLocation {

    // primary key
    private final int sol;
    private final double latitude;
    private final double longitude;
    private final String timestamp; // utc timestamp reported by spice

    public RoverLocation(int sol, double latitude, double longitude, String timestamp) {
        this.sol = sol;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = Preconditions.checkNotNull(timestamp);
    }

    public int sol() {
        return sol;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public String timestamp() {
        return timestamp;
    }
}
