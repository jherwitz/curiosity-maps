package org.curiosity.publish;

import org.curiosity.concept.Image;
import org.curiosity.concept.RoverLocation;

import java.util.List;

/**
 *
 * @author jherwitz
 */
public interface Publisher {

    /**
     * Publishes a list of {@link Image}s to the durable store.
     */
    void publishImages(List<Image> images);

    /**
     * Publishes a list of {@link RoverLocation}s to the durable store.
     */
    void publishLocations(List<RoverLocation> locations);
}
